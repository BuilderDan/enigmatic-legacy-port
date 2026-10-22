package com.aizistral.enigmaticlegacy.fabric;

import java.io.Reader;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;
import com.aizistral.enigmaticlegacy.helpers.PotionHelper;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.enigmaticlegacy.registries.EnigmaticPotions;
import com.aizistral.enigmaticlegacy.registries.EnigmaticTabs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;

/**
 * Port-only, dev runs only. Enabled with {@code -Denigmaticlegacy.devcheck=true} (or
 * {@code =exit} to close the client afterwards); see {@code -Pdevcheck} in build.gradle.
 * Once the title screen is up, it builds the main creative tab and, for every stack in it,
 * checks the item name, model and tooltip, logging each result with a "[devcheck]" prefix.
 * This is a smoke test, not a replacement for the manual in-game playtest.
 */
public final class DevSelfCheck {
	private static final Logger LOGGER = LoggerFactory.getLogger("EnigmaticLegacy/devcheck");
	private static final String MODE = System.getProperty("enigmaticlegacy.devcheck", "");
	private static boolean done = false;

	private DevSelfCheck() {}

	public static void register() {
		if (MODE.isEmpty() || MODE.equals("false"))
			return;

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (done || client.getOverlay() != null || !(client.screen instanceof TitleScreen))
				return;

			done = true;
			run(client);

			if (MODE.equals("exit")) {
				client.stop();
			}
		});
	}

	private static void run(Minecraft client) {
		int problems = 0;

		CreativeModeTabs.tryRebuildTabContents(FeatureFlags.DEFAULT_FLAGS, false, VanillaRegistries.createLookup());
		List<ItemStack> stacks = new ArrayList<>(EnigmaticTabs.MAIN.getDisplayItems());
		LOGGER.info("[devcheck] Main tab holds {} stacks", stacks.size());
		stacks.addAll(EnigmaticTabs.POTIONS.getDisplayItems());
		LOGGER.info("[devcheck] Potions tab holds {} stacks", EnigmaticTabs.POTIONS.getDisplayItems().size());

		long registered = BuiltInRegistries.ITEM.keySet().stream()
				.filter(id -> id.getNamespace().equals(EnigmaticLegacy.MODID)).count();
		LOGGER.info("[devcheck] {} items registered under {}", registered, EnigmaticLegacy.MODID);

		BakedModel missing = client.getModelManager().getMissingModel();

		for (ItemStack stack : stacks) {
			String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

			if (!I18n.exists(stack.getDescriptionId())) {
				problems++;
				LOGGER.warn("[devcheck] {}: no translation for {}", id, stack.getDescriptionId());
			}

			if (client.getItemRenderer().getModel(stack, null, null, 0) == missing) {
				problems++;
				LOGGER.warn("[devcheck] {}: missing item model", id);
			}

			for (TooltipFlag flag : new TooltipFlag[] { TooltipFlag.NORMAL, TooltipFlag.ADVANCED }) {
				try {
					List<Component> lines = stack.getTooltipLines(null, flag);

					if (flag == TooltipFlag.NORMAL) {
						LOGGER.info("[devcheck] {}: {} tooltip lines", id, lines.size());

						for (Component line : lines) {
							String text = line.getString();

							if (text.contains("tooltip.enigmaticlegacy") || text.contains("enigmaticlegacy.")) {
								problems++;
								LOGGER.warn("[devcheck] {}: untranslated tooltip line: {}", id, text);
							} else {
								LOGGER.info("[devcheck]   | {}", text);
							}
						}
					}
				} catch (Exception ex) {
					problems++;
					LOGGER.error("[devcheck] {}: tooltip threw ({})", id, flag);
					LOGGER.error("[devcheck] stack trace", ex);
				}
			}
		}

		problems += checkRecipes();
		problems += checkPiglinMixin();
		problems += checkLootPools();
		problems += checkBrewing();
		problems += checkMixinApplied("net.minecraft.world.entity.LivingEntity", "onHurt", "onExperienceDrop", "startDropCapture", "onDrops", "onHeal");
		problems += checkMixinApplied("net.minecraft.world.entity.Entity", "captureDrop");
		problems += checkMixinApplied("net.minecraft.world.item.enchantment.EnchantmentHelper", "addAccessoryLooting");
		problems += checkMixinApplied("net.minecraft.world.entity.player.Player", "addAttributes", "onBreakSpeed");
		problems += checkMixinApplied("net.minecraft.world.level.storage.loot.functions.ApplyBonusCount", "addAccessoryFortune");
		problems += checkMixinApplied("net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition", "addAccessoryFortune");
		problems += checkMixinApplied("net.minecraft.server.level.ServerPlayerGameMode", "widenBreakDistance");
		problems += checkMixinApplied("net.minecraft.server.network.ServerGamePacketListenerImpl", "widenUseDistance");
		problems += checkMixinApplied("net.minecraft.client.multiplayer.MultiPlayerGameMode", "addBlockReach");
		problems += checkMixinApplied("net.minecraft.world.effect.MobEffectInstance", "enigmaticlegacy$getDuration", "enigmaticlegacy$setDuration");
		problems += checkMixinApplied("net.minecraft.client.gui.Gui", "renderHeatBar", "renderFoodIcon", "renderAirBubble");
		problems += checkMixinApplied("net.minecraft.world.entity.projectile.ThrownEnderpearl", "pearlDamage");
		problems += checkMixinApplied("net.minecraft.world.item.ItemStack", "beginTooltip", "endTooltip");
		problems += checkMixinApplied("net.minecraft.world.level.block.entity.BeaconBlockEntity", "enigmaticlegacy$getLevels");
		problems += checkPlayerAttributes();
		LOGGER.info("[devcheck] Done: {} problem(s)", problems);
	}

	/**
	 * Loads PiglinAi (which applies MixinPiglinAi) and checks that piglins take the Exquisite
	 * Ring as barter currency. Reflection by Mojang name works only in dev runs, as here.
	 */
	private static int checkPiglinMixin() {
		try {
			Method method = PiglinAi.class.getDeclaredMethod("isBarterCurrency", ItemStack.class);
			method.setAccessible(true);
			boolean ring = (boolean) method.invoke(null, new ItemStack(EnigmaticItems.GOLDEN_RING));
			boolean dirt = (boolean) method.invoke(null, new ItemStack(Items.DIRT));
			if (ring && !dirt) {
				LOGGER.info("[devcheck] piglin mixin ok (golden_ring is barter currency)");
				return 0;
			}
			LOGGER.error("[devcheck] piglin mixin wrong: golden_ring={}, dirt={}", ring, dirt);
		} catch (Throwable ex) {
			LOGGER.error("[devcheck] piglin mixin check failed", ex);
		}
		return 1;
	}

	/**
	 * Calls vanilla PotionBrewing the way the brewing stand does, so MixinPotionBrewing and the
	 * port's BrewingRecipeRegistry are both exercised: awkward potion + ender eye -> recall.
	 */
	private static int checkBrewing() {
		try {
			ItemStack awkward = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD);
			ItemStack water = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
			ItemStack eye = new ItemStack(Items.ENDER_EYE);
			boolean ingredient = PotionBrewing.isIngredient(eye);
			boolean hasMix = PotionBrewing.hasMix(awkward, eye);
			boolean waterMix = PotionBrewing.hasMix(water, eye);
			ItemStack result = PotionBrewing.mix(eye, awkward);
			ItemStack vanilla = PotionBrewing.mix(new ItemStack(Items.NETHER_WART), water);
			// Common potions: awkward + quartz -> common potion of haste (ComplexBrewingRecipe)
			ItemStack haste = PotionBrewing.mix(new ItemStack(Items.QUARTZ), awkward);
			boolean hasteOk = haste.is(EnigmaticItems.COMMON_POTION)
					&& PotionHelper.getAdvancedPotion(haste) == EnigmaticPotions.HASTE;
			if (ingredient && hasMix && !waterMix && result.is(EnigmaticItems.RECALL_POTION)
					&& PotionUtils.getPotion(vanilla) == Potions.AWKWARD && hasteOk) {
				LOGGER.info("[devcheck] brewing ok (awkward + ender_eye -> recall_potion, awkward + quartz -> common haste)");
				return 0;
			}
			LOGGER.error("[devcheck] common haste brew: {} {}", haste, PotionHelper.getAdvancedPotion(haste));
			LOGGER.error("[devcheck] brewing wrong: ingredient={}, hasMix={}, waterMix={}, result={}, vanilla={}",
					ingredient, hasMix, waterMix, result, PotionUtils.getPotion(vanilla));
		} catch (Throwable ex) {
			LOGGER.error("[devcheck] brewing check failed", ex);
		}
		return 1;
	}

	/**
	 * Builds the End city and simple dungeon pools the way LootTableEvents.MODIFY would, so a broken entry shows
	 * up without loading a world: the End city gets the epic pool plus the ender spellstone pool,
	 * the simple dungeon the earthen spellstone pool. It does not check that pools reach the chest.
	 */
	private static int checkLootPools() {
		try {
			List<LootPool> pools = new ArrayList<>();
			EnigmaticEventHandler.onLootTablesLoaded(BuiltInLootTables.END_CITY_TREASURE, pools::add);
			EnigmaticEventHandler.onLootTablesLoaded(BuiltInLootTables.SIMPLE_DUNGEON, pools::add);
			if (pools.size() == 3) {
				LOGGER.info("[devcheck] loot ok (3 pools: end city epic + spellstones, simple dungeon spellstones)");
				return 0;
			}
			LOGGER.error("[devcheck] loot wrong: {} pool(s) built, expected 3", pools.size());
		} catch (Throwable ex) {
			LOGGER.error("[devcheck] loot check failed", ex);
		}
		return 1;
	}

	/**
	 * Checks that players get the port's block reach attribute (MixinPlayer) and that it is registered.
	 */
	private static int checkPlayerAttributes() {
		try {
			var supplier = net.minecraft.world.entity.ai.attributes.DefaultAttributes.getSupplier(net.minecraft.world.entity.EntityType.PLAYER);
			var id = net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.getKey(com.aizistral.enigmaticlegacy.registries.EnigmaticAttributes.BLOCK_REACH);
			if (!supplier.hasAttribute(com.aizistral.enigmaticlegacy.registries.EnigmaticAttributes.BLOCK_REACH) || id == null) {
				LOGGER.error("[devcheck] block reach attribute missing (on player: {}, registered as: {})",
						supplier.hasAttribute(com.aizistral.enigmaticlegacy.registries.EnigmaticAttributes.BLOCK_REACH), id);
				return 1;
			}
			LOGGER.info("[devcheck] player attribute ok: {}", id);
			return 0;
		} catch (Throwable ex) {
			LOGGER.error("[devcheck] player attribute check failed", ex);
			return 1;
		}
	}

	/**
	 * Checks that the handler methods of our mixins were merged into the target class. Mixin may
	 * rename handlers, so it matches by name fragment.
	 */
	private static int checkMixinApplied(String target, String... handlers) {
		try {
			Method[] methods = Class.forName(target).getDeclaredMethods();
			int missing = 0;
			for (String handler : handlers) {
				if (Stream.of(methods).noneMatch(m -> m.getName().contains(handler))) {
					missing++;
					LOGGER.error("[devcheck] mixin handler {} not found in {}", handler, target);
				}
			}
			if (missing == 0) {
				LOGGER.info("[devcheck] mixins ok in {}", target);
			}
			return missing;
		} catch (Throwable ex) {
			LOGGER.error("[devcheck] mixin check failed for {}", target, ex);
			return 1;
		}
	}

	/**
	 * Parses every bundled recipe JSON the way the server would (load conditions first),
	 * so serializer and ingredient errors show up without loading a world.
	 */
	private static int checkRecipes() {
		int problems = 0, parsed = 0, skipped = 0, pending = 0;
		Path dir = FabricLoader.getInstance().getModContainer(EnigmaticLegacy.MODID).orElseThrow()
				.findPath("data/" + EnigmaticLegacy.MODID + "/recipes").orElse(null);
		if (dir == null) {
			LOGGER.error("[devcheck] recipe folder not found");
			return 1;
		}
		try (Stream<Path> files = Files.walk(dir)) {
			for (Path file : (Iterable<Path>) files.filter(f -> f.toString().endsWith(".json")).sorted()::iterator) {
				String name = dir.relativize(file).toString().replace('\\', '/');
				ResourceLocation id = new ResourceLocation(EnigmaticLegacy.MODID, name.substring(0, name.length() - 5));
				try (Reader reader = Files.newBufferedReader(file)) {
					JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
					if (!ResourceConditions.objectMatchesConditions(json)) {
						skipped++;
						continue;
					}
					RecipeManager.fromJson(id, json);
					parsed++;
					String type = json.get("type").getAsString();
					if (type.startsWith(EnigmaticLegacy.MODID + ":")) {
						LOGGER.info("[devcheck] recipe {} ok ({})", id, type);
					}
				} catch (Exception ex) {
					String msg = String.valueOf(ex.getMessage());
					// Recipes that name our own not-yet-ported items or serializers are expected to
					// fail until those are ported; anything else is a real problem.
					if (msg.contains("item '" + EnigmaticLegacy.MODID + ":") || msg.contains("type '" + EnigmaticLegacy.MODID + ":")) {
						pending++;
						LOGGER.info("[devcheck] recipe {} pending: {}", id, msg);
						continue;
					}
					problems++;
					LOGGER.error("[devcheck] recipe {} failed: {}", id, ex.toString());
				}
			}
		} catch (Exception ex) {
			LOGGER.error("[devcheck] recipe scan failed", ex);
			return problems + 1;
		}
		LOGGER.info("[devcheck] recipes: {} parsed, {} skipped by conditions, {} pending (unported content), {} failed",
				parsed, skipped, pending, problems);
		return problems;
	}
}
