package com.aizistral.enigmaticlegacy.fabric;

import net.minecraft.core.Holder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.aizistral.enigmaticlegacy.entities.PermanentItemEntity;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ExperienceHelper;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.platform.Services;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.enigmaticlegacy.registries.EnigmaticSounds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Port-only, dev runs only. The server half of {@link DevPlaytest}: runs scripted in-world
 * checks on the single player, one scenario at a time, on a stone platform at y=200.
 * Each scenario is a list of steps with tick delays; a step that returns a message fails
 * the scenario. Results go to the log ("[playtest]") and test-output/playtest/results.md.
 */
public final class DevPlaytestScenarios {
	private static final Logger LOGGER = LoggerFactory.getLogger("EnigmaticLegacy/playtest");
	private static final BlockPos ORIGIN = new BlockPos(0, 200, 0);
	private static final ResourceLocation DESOLATION_ADVANCEMENT = ResourceLocation.fromNamespaceAndPath("enigmaticlegacy", "main/desolation_ring");
	private static final List<Scenario> SCENARIOS = new ArrayList<>();
	private static final List<String> RESULTS = new ArrayList<>();

	private static Set<String> filter = Set.of();
	private static volatile boolean started = false, finished = false;
	private static int scenario = 0, step = -1, wait = 0, failures = 0;
	private static Ctx ctx;

	private DevPlaytestScenarios() {}

	public static boolean isFinished() {
		return finished;
	}

	public static void start() {
		started = true;
	}

	public static void register(String mode) {
		filter = Arrays.stream(mode.split(",")).map(String::trim)
				.filter(s -> !s.isEmpty() && !s.equals("all") && !s.equals("exit") && !s.equals("remote")).collect(Collectors.toSet());
		define();

		if (!filter.isEmpty()) {
			SCENARIOS.removeIf(s -> !filter.contains(s.id));
		}

		ServerTickEvents.END_SERVER_TICK.register(DevPlaytestScenarios::tick);
	}

	// ---------------------------------------------------------------- scenarios

	private static void define() {
		scenario("magnet_ring", "Magnetic Ring: pulls items in range")
		.step(c -> equip(c, EnigmaticItems.MAGNET_RING), 5)
		.then(c -> drop(c, Items.COBBLESTONE, 5), 80)
		.expect(c -> count(c, Items.COBBLESTONE) == 1 ? null : "item not picked up, distance " + itemDistance(c));

		scenario("magnet_ring_sneak", "Magnetic Ring: no pull while sneaking")
		.step(c -> equip(c, EnigmaticItems.MAGNET_RING), 5)
		.then(c -> c.player.setShiftKeyDown(true), 2)
		.then(c -> drop(c, Items.COBBLESTONE, 5), 60)
		.expect(c -> count(c, Items.COBBLESTONE) == 0 && itemDistance(c) > 4 ? null
				: "item moved while sneaking, distance " + itemDistance(c));

		scenario("magnet_conflict", "Magnetic Ring and Dislocation Ring can't be worn together")
		.step(c -> equip(c, EnigmaticItems.MAGNET_RING), 5)
		.expect(c -> Services.ACCESSORIES.tryForceEquip(c.player, new ItemStack(EnigmaticItems.SUPER_MAGNET_RING))
				? "Dislocation Ring equipped next to Magnetic Ring" : null);

		scenario("super_magnet_ring", "Dislocation Ring: items straight into inventory")
		.step(c -> equip(c, EnigmaticItems.SUPER_MAGNET_RING), 5)
		.then(c -> drop(c, Items.COBBLESTONE, 6), 20)
		.expect(c -> count(c, Items.COBBLESTONE) == 1 ? null : "item not in inventory, distance " + itemDistance(c));

		scenario("iron_ring", "Iron Ring: +1 armor")
		.step(c -> equip(c, EnigmaticItems.IRON_RING), 10)
		.expect(c -> attributeIs(c, "armor", c.player.getAttributeValue(Attributes.ARMOR), 1));

		scenario("golden_ring", "Exquisite Ring: +1 luck, piglins neutral")
		.step(c -> equip(c, EnigmaticItems.GOLDEN_RING), 10)
		.expect(c -> attributeIs(c, "luck", c.player.getAttributeValue(Attributes.LUCK), 1))
		.expect(c -> Services.ACCESSORIES.makesPiglinsNeutral(c.player) ? null : "piglins not neutral");

		scenario("etherium_ingot", "Etherium Ingot: fire resistant")
		.expect(c -> !new ItemStack(EnigmaticItems.ETHERIUM_INGOT).canBeHurtBy(c.level.damageSources().inFire()) ? null : "not fire resistant");

		scenario("astral_potato", "Astral Potato: food 8 and a short teleport")
		.then(c -> {
			c.player.getFoodData().setFoodLevel(0);
			c.player.getFoodData().setSaturation(0);
			c.data.put("pos", c.player.position());
			new ItemStack(EnigmaticItems.ASTRAL_POTATO).finishUsingItem(c.level, c.player);
		}, 5)
		.expect(c -> c.player.getFoodData().getFoodLevel() == 8 ? null : "food " + c.player.getFoodData().getFoodLevel())
		.expect(c -> c.player.position().distanceTo((Vec3) c.data.get("pos")) > 0.01 ? null : "no teleport");

		scenario("infinimeal", "Essence of Raging Life: grows a crop, not consumed")
		.then(c -> {
			BlockPos crop = ORIGIN.east(3);
			c.level.setBlockAndUpdate(crop.below(), Blocks.FARMLAND.defaultBlockState());
			c.level.setBlockAndUpdate(crop, Blocks.WHEAT.defaultBlockState());
			c.player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(EnigmaticItems.INFINIMEAL));
			c.player.getMainHandItem().useOn(new UseOnContext(c.player, InteractionHand.MAIN_HAND,
					new BlockHitResult(Vec3.atCenterOf(crop), Direction.UP, crop, false)));
		}, 5)
		.expect(c -> c.level.getBlockState(ORIGIN.east(3)).getValue(CropBlock.AGE) > 0 ? null : "wheat did not grow")
		.expect(c -> c.player.getMainHandItem().is(EnigmaticItems.INFINIMEAL) && c.player.getMainHandItem().getCount() == 1
				? null : "consumed, hand holds " + c.player.getMainHandItem());

		scenario("ichor_bottle", "Bottle of Ichor: Regeneration III, Absorption V, Resistance III, Fire Resistance")
		.then(c -> new ItemStack(EnigmaticItems.ICHOR_BOTTLE).finishUsingItem(c.level, c.player), 2)
		.expect(c -> hasEffects(c, MobEffects.REGENERATION, 2, MobEffects.ABSORPTION, 4, MobEffects.DAMAGE_RESISTANCE, 2,
				MobEffects.FIRE_RESISTANCE, 0));

		scenario("unholy_grail", "Unholy Grail: Wither, Poison, Nausea, Weakness, Hunger, Slowness")
		.then(c -> new ItemStack(EnigmaticItems.UNHOLY_GRAIL).finishUsingItem(c.level, c.player), 2)
		.expect(c -> hasEffects(c, MobEffects.WITHER, -1, MobEffects.POISON, -1, MobEffects.CONFUSION, -1,
				MobEffects.WEAKNESS, -1, MobEffects.HUNGER, -1, MobEffects.MOVEMENT_SLOWDOWN, -1));

		scenario("void_pearl", "Pearl of the Void: effect immunity")
		.step(c -> equip(c, EnigmaticItems.VOID_PEARL), 5)
		.then(c -> c.player.addEffect(new MobEffectInstance(MobEffects.POISON, 400, 0)), 20)
		.expect(c -> c.player.hasEffect(MobEffects.POISON) ? "poison stayed" : null);

		scenario("enigmatic_item", "Heart of Creation: flight, fire and harmful effects blocked")
		.step(c -> equip(c, EnigmaticItems.ENIGMATIC_ITEM), 10)
		.expect(c -> c.player.getAbilities().mayfly ? null : "no flight")
		.then(c -> {
			c.player.addEffect(new MobEffectInstance(MobEffects.WITHER, 400, 0));
			c.player.igniteForSeconds(10);
		}, 10)
		.expect(c -> c.player.hasEffect(MobEffects.WITHER) ? "wither stayed" : null)
		.expect(c -> c.player.isOnFire() ? "still burning" : null);

		// The scroll only works in beacon range: a level 1 beacon, which updates its level every 80 ticks.
		scenario("heaven_scroll", "Gift of the Heaven: flight in survival")
		.then(c -> {
			BlockPos beacon = ORIGIN.offset(3, 1, 3);
			for (BlockPos pos : BlockPos.betweenClosed(beacon.offset(-1, -1, -1), beacon.offset(1, -1, 1))) {
				c.level.setBlockAndUpdate(pos, Blocks.IRON_BLOCK.defaultBlockState());
			}
			c.level.setBlockAndUpdate(beacon, Blocks.BEACON.defaultBlockState());
			c.player.setExperienceLevels(30);
		}, 100)
		.step(c -> equip(c, EnigmaticItems.HEAVEN_SCROLL), 45)
		.expect(c -> c.player.getAbilities().mayfly ? null : "no flight");

		scenario("golem_heart", "Heart of the Golem: armor with no armor worn")
		.step(c -> equip(c, EnigmaticItems.GOLEM_HEART), 10)
		.expect(c -> c.player.getAttributeValue(Attributes.ARMOR) > 0 ? null : "armor 0");

		// The playtest world is kept between runs, so the advancement is revoked first.
		scenario("desolation_advancement", "Ring of Desolation: equip advancement (Curios trigger replaced)")
		.then(c -> {
			AdvancementHolder adv = c.level.getServer().getAdvancements().get(DESOLATION_ADVANCEMENT);
			if (adv != null)
				c.player.getAdvancements().revoke(adv, "infinitum");
		}, 2)
		.step(c -> equip(c, EnigmaticItems.DESOLATION_RING), 10)
		.expect(c -> {
			AdvancementHolder adv = c.level.getServer().getAdvancements().get(DESOLATION_ADVANCEMENT);
			if (adv == null)
				return "advancement not loaded";
			return c.player.getAdvancements().getOrStartProgress(adv).isDone() ? null : "not granted";
		});

		scenario("mending_mixture", "Mending Mixture: repair recipe")
		.expect(c -> {
			TransientCraftingContainer grid = new TransientCraftingContainer(c.player.inventoryMenu, 3, 3);
			ItemStack pick = new ItemStack(Items.IRON_PICKAXE);
			pick.setDamageValue(200);
			grid.setItem(0, pick);
			grid.setItem(1, new ItemStack(EnigmaticItems.MENDING_MIXTURE));
			CraftingInput input = grid.asCraftInput();
			var recipe = c.level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, c.level);

			if (recipe.isEmpty())
				return "no recipe matched";

			ItemStack result = recipe.get().value().assemble(input, c.level.registryAccess());
			return result.is(Items.IRON_PICKAXE) && result.getDamageValue() < 200 ? null : "result " + result
					+ " damage " + result.getDamageValue();
		});

		scenario("recall_potion", "Potion of Recall: back to spawn")
		.then(c -> new ItemStack(EnigmaticItems.RECALL_POTION).finishUsingItem(c.level, c.player), 5)
		.expect(c -> c.player.position().distanceTo(Vec3.atBottomCenterOf(ORIGIN)) > 10 ? null : "not teleported");

		scenario("monster_charm", "Emblem of Monster Slayer: +25% damage to undead")
		.step(c -> equip(c, EnigmaticItems.MONSTER_CHARM), 5)
		.then(c -> spawn(c, EntityType.SKELETON, 3), 2)
		.expect(c -> {
			Mob mob = (Mob) c.data.get("mob");
			float before = mob.getHealth();
			mob.invulnerableTime = 0;
			mob.hurt(c.player.damageSources().playerAttack(c.player), 4);
			float ratio = (before - mob.getHealth()) / 4;
			return ratio > 1.2 && ratio < 1.3 ? null : "damage ratio " + ratio;
		});

		scenario("xp_scroll", "Scroll of Ageless Wisdom: key toggles, absorbs XP")
		.step(c -> equip(c, EnigmaticItems.XP_SCROLL), 5)
		.then(c -> key("xp_scroll"), 8)
		.expect(c -> heard(c, "entity.experience_orb.pickup"))
		.expect(c -> ItemNBTHelper.getBoolean(scroll(c), "AbsorptionMode", true) ? "absorption mode not toggled" : null)
		.then(c -> key("xp_scroll"), 8)
		.then(c -> c.player.setShiftKeyDown(true), 4)
		.then(c -> key("xp_scroll"), 8)
		.expect(c -> heard(c, EnigmaticSounds.CHARGED_ON.getLocation().toString()))
		.then(c -> {
			c.player.setShiftKeyDown(false);
			c.player.giveExperiencePoints(100);
		}, 40)
		.expect(c -> ItemNBTHelper.getInt(scroll(c), "XPStored", 0) > 0 && ExperienceHelper.getPlayerXP(c.player) < 100 ? null
				: "stored " + ItemNBTHelper.getInt(scroll(c), "XPStored", 0) + ", player XP " + ExperienceHelper.getPlayerXP(c.player));

		scenario("ender_ring", "Ring of Ender: key opens the ender chest")
		.step(c -> equip(c, EnigmaticItems.ENDER_RING), 5)
		.then(c -> key("ender_ring"), 10)
		.expect(c -> c.player.containerMenu instanceof ChestMenu ? null : "menu " + c.player.containerMenu)
		.expect(c -> heard(c, "block.ender_chest.open"))
		.then(c -> shot("ender_ring"), 30)
		.expect(DevPlaytestScenarios::shotTaken);

		scenario("blazing_core", "Blazing Core: fire and lava immunity, lava HUD")
		.step(c -> equip(c, EnigmaticItems.BLAZING_CORE), 5)
		.then(c -> c.player.igniteForSeconds(5), 20)
		.expect(c -> c.player.getHealth() >= c.player.getMaxHealth() - 0.01 ? null : "fire damage, health " + c.player.getHealth())
		.then(c -> {
			c.level.setBlockAndUpdate(ORIGIN, Blocks.LAVA.defaultBlockState());
			c.level.setBlockAndUpdate(ORIGIN.above(), Blocks.LAVA.defaultBlockState());
		}, 30)
		.expect(c -> c.player.getHealth() >= c.player.getMaxHealth() - 0.01 ? null : "lava damage, health " + c.player.getHealth())
		.then(c -> shot("blazing_core_lava"), 30)
		.expect(DevPlaytestScenarios::shotTaken);

		scenario("angel_blessing", "Angel's Blessing: key launches the player")
		.step(c -> equip(c, EnigmaticItems.ANGEL_BLESSING), 5)
		.then(c -> look(c, 0, -90), 5)
		.then(c -> key("spellstone"), 8)
		.expect(c -> c.player.getY() > ORIGIN.getY() + 2 ? null : "y " + c.player.getY())
		.expect(c -> heard(c, "entity.ender_eye.launch"));

		scenario("ocean_stone", "Will of the Ocean: key starts a thunderstorm")
		.step(c -> equip(c, EnigmaticItems.OCEAN_STONE), 5)
		.then(c -> {
			c.player.giveExperiencePoints(1000);
			key("spellstone");
		}, 10)
		.expect(c -> c.level.getLevelData().isThundering() ? null : "no thunder");

		scenario("eye_of_nebula", "Eye of the Nebula: key teleports past the target")
		.step(c -> equip(c, EnigmaticItems.EYE_OF_NEBULA), 5)
		.then(c -> {
			spawn(c, EntityType.PIG, 0);
			Mob mob = (Mob) c.data.get("mob");
			mob.teleportTo(ORIGIN.getX() + 0.5, ORIGIN.getY(), ORIGIN.getZ() + 4.5);
			look(c, 0, 0);
		}, 5)
		.then(c -> key("spellstone"), 10)
		.expect(c -> c.player.getZ() > ORIGIN.getZ() + 4.5 ? null : "z " + c.player.getZ())
		.expect(c -> heard(c, "entity.enderman.teleport"));

		scenario("the_cube", "The Cube: key teleports somewhere random")
		.step(c -> equip(c, EnigmaticItems.THE_CUBE), 5)
		.then(c -> key("spellstone"), 40)
		.expect(c -> c.player.position().distanceTo(Vec3.atBottomCenterOf(ORIGIN)) > 10 ? null : "not teleported");

		cursedOnly("cursed_scroll", EnigmaticItems.CURSED_SCROLL, "Scroll of a Thousand Curses");
		cursedOnly("avarice_scroll", EnigmaticItems.AVARICE_SCROLL, "Pact of Infinite Avarice");
		scenario("infernal_shield", "Infernal Shield: only the cursed can raise it")
		.then(c -> c.player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(EnigmaticItems.INFERNAL_SHIELD)), 1)
		.expect(c -> c.player.getMainHandItem().use(c.level, c.player, InteractionHand.MAIN_HAND).getResult().consumesAction()
				? "raised without the ring" : null)
		.step(c -> equip(c, EnigmaticItems.CURSED_RING), 5)
		.expect(c -> c.player.getMainHandItem().use(c.level, c.player, InteractionHand.MAIN_HAND).getResult().consumesAction()
				&& c.player.isUsingItem() ? null : "not raised with the ring")
		.then(c -> c.player.stopUsingItem(), 1);

		scenario("eldritch_amulet", "Eldritch Amulet: only for the worthy")
		.step(c -> equip(c, EnigmaticItems.CURSED_RING), 5)
		.expect(c -> equip(c, EnigmaticItems.ELDRITCH_AMULET) == null ? "equipped while not worthy" : null)
		.then(DevPlaytestScenarios::worthy, 5)
		.expect(c -> SuperpositionHandler.isTheWorthyOne(c.player) ? null : "not worthy after setup")
		.expect(c -> equip(c, EnigmaticItems.ELDRITCH_AMULET));

		scenario("enigmatic_eye", "Enigmatic Eye: can't be worn while dormant")
		.expect(c -> equip(c, EnigmaticItems.ENIGMATIC_EYE) == null ? "dormant eye equipped" : null);

		scenario("forbidden_fruit", "Forbidden Fruit: marks the fruit as eaten")
		.then(c -> new ItemStack(EnigmaticItems.FORBIDDEN_FRUIT).finishUsingItem(c.level, c.player), 5)
		.expect(c -> EnigmaticItems.FORBIDDEN_FRUIT.haveConsumedFruit(c.player) ? null : "not marked");

		scenario("redemption_potion", "Redemption Potion: undoes the Forbidden Fruit")
		.then(c -> EnigmaticItems.FORBIDDEN_FRUIT.defineConsumedFruit(c.player, true), 2)
		.then(c -> new ItemStack(EnigmaticItems.REDEMPTION_POTION).finishUsingItem(c.level, c.player), 5)
		.expect(c -> EnigmaticItems.FORBIDDEN_FRUIT.haveConsumedFruit(c.player) ? "still marked" : null);

		scenario("astral_fruit", "Astral Fruit: effects, ring slot")
		.then(c -> new ItemStack(EnigmaticItems.ASTRAL_FRUIT).finishUsingItem(c.level, c.player), 5)
		.expect(c -> hasEffects(c, MobEffects.REGENERATION, 3, MobEffects.DAMAGE_RESISTANCE, 2, MobEffects.DAMAGE_BOOST, 3,
				MobEffects.FIRE_RESISTANCE, 0))
		.expect(c -> SuperpositionHandler.getPersistentBoolean(c.player, "ConsumedAstralFruit", false) ? null : "not marked");

		scenario("twisted_potion", "Twisted Potion: cursed only, back to the last death spot at 1 health")
		.expect(c -> EnigmaticItems.TWISTED_POTION.canDrink(c.level, c.player, new ItemStack(EnigmaticItems.TWISTED_POTION))
				? "drinkable without the ring and a death" : null)
		.step(c -> equip(c, EnigmaticItems.CURSED_RING), 5)
		.then(c -> {
			CompoundTag location = new CompoundTag();
			location.putDouble("x", ORIGIN.getX() + 4.5);
			location.putDouble("y", ORIGIN.getY());
			location.putDouble("z", ORIGIN.getZ() + 4.5);
			location.putString("dimension", "minecraft:overworld");
			SuperpositionHandler.setPersistentTag(c.player, "LastDeathLocation", location);
		}, 2)
		.expect(c -> EnigmaticItems.TWISTED_POTION.canDrink(c.level, c.player, new ItemStack(EnigmaticItems.TWISTED_POTION))
				? null : "not drinkable")
		.then(c -> new ItemStack(EnigmaticItems.TWISTED_POTION).finishUsingItem(c.level, c.player), 5)
		.expect(c -> c.player.position().distanceTo(new Vec3(ORIGIN.getX() + 4.5, ORIGIN.getY(), ORIGIN.getZ() + 4.5)) < 1.5
				? null : "at " + c.player.position())
		.expect(c -> c.player.getHealth() <= 1.01 ? null : "health " + c.player.getHealth());

		scenario("astral_breaker", "Astral Breaker: mines 3x3")
		.then(c -> {
			c.player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(EnigmaticItems.ASTRAL_BREAKER));
			look(c, 0, 90);
		}, 5)
		.expect(c -> {
			c.player.gameMode.destroyBlock(ORIGIN.below());
			int left = 0;
			for (BlockPos pos : BlockPos.betweenClosed(ORIGIN.offset(-1, -1, -1), ORIGIN.offset(1, -1, 1))) {
				if (!c.level.getBlockState(pos).isAir())
					left++;
			}
			return left == 0 ? null : left + " of 9 blocks left";
		});

		scenario("lore_inscriber", "Lore Inscriber: opens its GUI")
		.then(c -> {
			c.player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(EnigmaticItems.LORE_INSCRIBER));
			c.player.getMainHandItem().use(c.level, c.player, InteractionHand.MAIN_HAND);
		}, 10)
		.expect(c -> c.player.containerMenu != c.player.inventoryMenu ? null : "no menu opened")
		.then(c -> shot("lore_inscriber"), 30)
		.expect(DevPlaytestScenarios::shotTaken);

		// Deaths last: the client respawns on its own (DevPlaytest), and later steps use the new player.
		scenario("escape_scroll", "Scroll of Postmortal Recall: scroll stays, drops go to spawn")
		.step(c -> equip(c, EnigmaticItems.ESCAPE_SCROLL), 5)
		.then(c -> {
			c.level.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(false, c.level.getServer());
			c.player.getInventory().add(new ItemStack(Items.COBBLESTONE));
			c.player.kill();
		}, 40)
		.expect(c -> c.player.isAlive() ? null : "not respawned")
		.expect(c -> nearOrigin(c, e -> e instanceof PermanentItemEntity p && p.getItem().is(EnigmaticItems.ESCAPE_SCROLL))
				? null : "no scroll at the death spot")
		.expect(c -> nearOrigin(c, e -> e instanceof ItemEntity i && i.getItem().is(Items.COBBLESTONE))
				? "cobblestone dropped at the death spot" : null);

		scenario("soul_crystal", "Soul Crystal: lost on death with the ring, dropped at the death spot")
		.step(c -> equip(c, EnigmaticItems.CURSED_RING), 5)
		.then(c -> {
			c.level.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(false, c.level.getServer());
			c.player.kill();
		}, 40)
		.expect(c -> c.player.isAlive() ? null : "not respawned")
		.expect(c -> EnigmaticItems.SOUL_CRYSTAL.getLostCrystals(c.player) == 1 ? null
				: "lost crystals " + EnigmaticItems.SOUL_CRYSTAL.getLostCrystals(c.player))
		.expect(c -> nearOrigin(c, e -> e instanceof PermanentItemEntity p && p.getItem().is(EnigmaticItems.SOUL_CRYSTAL))
				? null : "no crystal at the death spot");
	}

	// ---------------------------------------------------------------- helpers

	private static String equip(Ctx c, Item item) {
		return Services.ACCESSORIES.tryForceEquip(c.player, new ItemStack(item)) ? null : "could not equip " + item;
	}

	private static void drop(Ctx c, Item item, double distance) {
		ItemEntity entity = new ItemEntity(c.level, c.player.getX() + distance, c.player.getY() + 0.2, c.player.getZ(),
				new ItemStack(item), 0, 0, 0);
		entity.setNoPickUpDelay();
		c.level.addFreshEntity(entity);
		c.data.put("item", entity);
	}

	private static double itemDistance(Ctx c) {
		Entity entity = (Entity) c.data.get("item");
		return entity == null || entity.isRemoved() ? -1 : entity.distanceTo(c.player);
	}

	private static int count(Ctx c, Item item) {
		return c.player.getInventory().countItem(item);
	}

	private static String attributeIs(Ctx c, String name, double value, double expected) {
		return Math.abs(value - expected) < 0.001 ? null : name + " " + value + ", expected " + expected;
	}

	/** Pairs of effect and amplifier; amplifier -1 means any. */
	private static String hasEffects(Ctx c, Object... pairs) {
		List<String> missing = new ArrayList<>();

		for (int i = 0; i < pairs.length; i += 2) {
			@SuppressWarnings("unchecked")
			Holder<MobEffect> effect = (Holder<MobEffect>) pairs[i];
			int amplifier = (Integer) pairs[i + 1];
			MobEffectInstance instance = c.player.getEffect(effect);

			if (instance == null || (amplifier >= 0 && instance.getAmplifier() != amplifier)) {
				missing.add(effect.value().getDisplayName().getString() + (instance == null ? "" : " amp " + instance.getAmplifier()));
			}
		}

		return missing.isEmpty() ? null : "missing/wrong: " + String.join(", ", missing);
	}


	/** The item can be worn only with the Seven Curses ring on. */
	private static void cursedOnly(String id, Item item, String name) {
		scenario(id, name + ": only for the cursed")
		.expect(c -> equip(c, item) == null ? "equipped without the ring" : null)
		.step(c -> equip(c, EnigmaticItems.CURSED_RING), 5)
		.expect(c -> equip(c, item));
	}

	/** Makes the cursed player worthy: all playtime spent with curses. */
	private static void worthy(Ctx c) {
		var counter = Services.PLAYER_DATA.getPlaytimeCounter(c.player);
		counter.setTimeWithCurses(100000);
		counter.setTimeWithoutCurses(0);
	}

	/** Spawns a mob without AI the given number of blocks east, stored as "mob". */
	private static void spawn(Ctx c, EntityType<? extends Mob> type, double east) {
		Mob mob = type.create(c.level);
		mob.setNoAi(true);
		mob.moveTo(c.player.getX() + east, c.player.getY(), c.player.getZ(), 0, 0);
		c.level.addFreshEntity(mob);
		c.data.put("mob", mob);
	}

	private static void look(Ctx c, float yaw, float pitch) {
		c.player.teleportTo(c.level, c.player.getX(), c.player.getY(), c.player.getZ(), yaw, pitch);
	}

	private static ItemStack scroll(Ctx c) {
		return SuperpositionHandler.getCurioStack(c.player, EnigmaticItems.XP_SCROLL);
	}

	private static boolean nearOrigin(Ctx c, Predicate<Entity> test) {
		return !c.level.getEntities((Entity) null, new AABB(ORIGIN).inflate(8), test).isEmpty();
	}

	/** Asks the client to press a mod key for one tick (see DevPlaytest). */
	private static void key(String name) {
		request("key", name, DevHooks.KEYS);
	}

	/** Asks the client for a screenshot; wait at least 15 ticks before {@link #shotTaken}. */
	private static void shot(String name) {
		request("shot", name, DevHooks.SHOTS);
	}

	/** On a dedicated server the client is another process: the request goes as a chat line. */
	private static void request(String kind, String name, Queue<String> local) {
		if (dedicated()) {
			ctx.player.sendSystemMessage(Component.literal(DevHooks.REQUEST + kind + " " + name));
		} else {
			local.add(name);
		}
	}

	private static boolean dedicated() {
		return ctx.level.getServer().isDedicatedServer();
	}

	// The dedicated server cannot see the client's screenshots or sounds; those checks pass there.
	private static String shotTaken(Ctx c) {
		return dedicated() || DevHooks.SHOTS.isEmpty() ? null : "screenshot not taken";
	}

	private static String heard(Ctx c, String sound) {
		return dedicated() || DevHooks.SOUNDS.stream().anyMatch(s -> s.endsWith(sound)) ? null : "sound " + sound + " not played";
	}

	// ---------------------------------------------------------------- runner

	private static void tick(MinecraftServer server) {
		if (!started || finished)
			return;

		if (wait > 0) {
			wait--;
			return;
		}

		List<ServerPlayer> players = server.getPlayerList().getPlayers();

		if (players.isEmpty())
			return;

		if (ctx == null) {
			ctx = new Ctx(players.get(0), server.overworld());
			GameRules rules = ctx.level.getGameRules();
			rules.getRule(GameRules.RULE_DAYLIGHT).set(false, server);
			rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
			rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, server);
			LOGGER.info("[playtest] Running {} scenarios", SCENARIOS.size());
			// A client joining a dedicated server is still loading the terrain.
			wait = dedicated() ? 100 : 10;
			return;
		}

		// A death replaces the player object.
		ctx.player = players.get(0);

		if (scenario >= SCENARIOS.size()) {
			finish();
			return;
		}

		Scenario current = SCENARIOS.get(scenario);

		try {
			if (step < 0) {
				reset(ctx);
				step = 0;
				wait = 5;
				return;
			}

			if (step < current.steps.size()) {
				Step next = current.steps.get(step++);
				String problem = next.run.apply(ctx);
				wait = next.delay;

				if (problem != null) {
					report(current, "BUG " + problem);
				}

				return;
			}

			report(current, "ok");
		} catch (Exception ex) {
			LOGGER.error("[playtest] {} threw", current.id, ex);
			report(current, "BUG exception " + ex);
		}
	}

	private static void report(Scenario current, String result) {
		if (!result.equals("ok")) {
			failures++;
		}

		LOGGER.info("[playtest] {}: {}", current.id, result);
		RESULTS.add("| " + current.id + " | " + current.row + " | " + result.replace("|", "/") + " |");
		scenario++;
		step = -1;
		wait = 0;
	}

	private static void reset(Ctx c) {
		ServerPlayer player = c.player;
		player.closeContainer();
		List<Item> worn = new ArrayList<>();
		Services.ACCESSORIES.forEachEquipped(player, (slot, stack) -> worn.add(stack.getItem()));
		worn.forEach(item -> Services.ACCESSORIES.destroy(player, item));
		player.getInventory().clearContent();
		player.removeAllEffects();
		player.setShiftKeyDown(false);
		player.setGameMode(GameType.SURVIVAL);
		GameType.SURVIVAL.updatePlayerAbilities(player.getAbilities());
		player.onUpdateAbilities();
		player.setExperienceLevels(0);
		player.setExperiencePoints(0);
		player.setHealth(player.getMaxHealth());
		player.getFoodData().setFoodLevel(20);
		player.getFoodData().setSaturation(5);
		player.clearFire();
		player.setAirSupply(player.getMaxAirSupply());
		SuperpositionHandler.unlockSpecialSlot("spellstone", player);
		SuperpositionHandler.unlockSpecialSlot("scroll", player);
		SuperpositionHandler.clearSpellstoneCooldowns();
		SuperpositionHandler.setPersistentBoolean(player, "ConsumedAstralFruit", false);
		EnigmaticItems.FORBIDDEN_FRUIT.defineConsumedFruit(player, false);
		EnigmaticItems.SOUL_CRYSTAL.setLostCrystals(player, 0);
		EnigmaticItems.SOUL_CRYSTAL.updatePlayerSoulMap(player);
		var counter = Services.PLAYER_DATA.getPlaytimeCounter(player);
		counter.setTimeWithCurses(0);
		counter.setTimeWithoutCurses(0);
		c.level.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(true, c.level.getServer());
		c.level.setWeatherParameters(6000, 0, false, false);
		DevHooks.SOUNDS.clear();

		for (BlockPos pos : BlockPos.betweenClosed(ORIGIN.offset(-6, -1, -6), ORIGIN.offset(6, 4, 6))) {
			c.level.setBlockAndUpdate(pos, pos.getY() < ORIGIN.getY() ? Blocks.STONE.defaultBlockState()
					: Blocks.AIR.defaultBlockState());
		}

		c.level.getEntities(player, new AABB(ORIGIN).inflate(32), e -> !(e instanceof Player)).forEach(Entity::discard);
		player.teleportTo(c.level, ORIGIN.getX() + 0.5, ORIGIN.getY(), ORIGIN.getZ() + 0.5, 0, 0);
		player.setDeltaMovement(Vec3.ZERO);
		player.fallDistance = 0;
		c.data.clear();
	}

	private static void finish() {
		finished = true;
		reset(ctx);
		LOGGER.info("[playtest] Done: {} scenarios, {} failed", RESULTS.size(), failures);

		List<String> lines = new ArrayList<>();
		lines.add("# Playtest results" + (dedicated() ? ", dedicated server" : "") + " (" + RESULTS.size() + " scenarios, " + failures + " failed)");
		lines.add("");
		lines.add("| Scenario | Checks | Result |");
		lines.add("|---|---|---|");
		lines.addAll(RESULTS);

		try {
			Path file = FabricLoader.getInstance().getGameDir().resolve("../../test-output/playtest/" + (dedicated() ? "results-server.md" : "results.md")).normalize();
			Files.createDirectories(file.getParent());
			Files.write(file, lines);
		} catch (Exception ex) {
			LOGGER.error("[playtest] Could not write results", ex);
		}

		// The client sees the disconnect and closes itself.
		if (dedicated() && DevHooks.exitAfter()) {
			ctx.level.getServer().halt(false);
		}
	}

	// ---------------------------------------------------------------- plumbing

	private static Scenario scenario(String id, String row) {
		Scenario scenario = new Scenario(id, row);
		SCENARIOS.add(scenario);
		return scenario;
	}

	private record Step(Function<Ctx, String> run, int delay) {}

	private static final class Ctx {
		ServerPlayer player;
		final ServerLevel level;
		final Map<String, Object> data = new HashMap<>();

		Ctx(ServerPlayer player, ServerLevel level) {
			this.player = player;
			this.level = level;
		}
	}

	private static final class Scenario {
		final String id, row;
		final List<Step> steps = new ArrayList<>();

		Scenario(String id, String row) {
			this.id = id;
			this.row = row;
		}

		/** Runs an action, then waits the given number of ticks. */
		Scenario then(Consumer<Ctx> action, int delay) {
			this.steps.add(new Step(c -> { action.accept(c); return null; }, delay));
			return this;
		}

		/** Runs a step that may fail, then waits the given number of ticks. */
		Scenario step(Function<Ctx, String> run, int delay) {
			this.steps.add(new Step(run, delay));
			return this;
		}

		/** Runs a check; a non-null message fails the scenario. */
		Scenario expect(Function<Ctx, String> check) {
			this.steps.add(new Step(check, 0));
			return this;
		}
	}
}
