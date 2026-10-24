package com.aizistral.enigmaticlegacy.fabric;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.platform.Services;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
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
	private static final ResourceLocation DESOLATION_ADVANCEMENT = new ResourceLocation("enigmaticlegacy", "main/desolation_ring");
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
				.filter(s -> !s.isEmpty() && !s.equals("all") && !s.equals("exit")).collect(Collectors.toSet());
		define();

		if (!filter.isEmpty()) {
			SCENARIOS.removeIf(s -> !filter.contains(s.id));
		}

		ServerTickEvents.END_SERVER_TICK.register(DevPlaytestScenarios::tick);
	}

	// ---------------------------------------------------------------- scenarios

	private static void define() {
		scenario("magnet_ring", "Magnetic Ring: pulls items in range")
		.then(c -> equip(c, EnigmaticItems.MAGNET_RING), 5)
		.then(c -> drop(c, Items.COBBLESTONE, 5), 80)
		.expect(c -> count(c, Items.COBBLESTONE) == 1 ? null : "item not picked up, distance " + itemDistance(c));

		scenario("magnet_ring_sneak", "Magnetic Ring: no pull while sneaking")
		.then(c -> equip(c, EnigmaticItems.MAGNET_RING), 5)
		.then(c -> c.player.setShiftKeyDown(true), 2)
		.then(c -> drop(c, Items.COBBLESTONE, 5), 60)
		.expect(c -> count(c, Items.COBBLESTONE) == 0 && itemDistance(c) > 4 ? null
				: "item moved while sneaking, distance " + itemDistance(c));

		scenario("magnet_conflict", "Magnetic Ring and Dislocation Ring can't be worn together")
		.then(c -> equip(c, EnigmaticItems.MAGNET_RING), 5)
		.expect(c -> Services.ACCESSORIES.tryForceEquip(c.player, new ItemStack(EnigmaticItems.SUPER_MAGNET_RING))
				? "Dislocation Ring equipped next to Magnetic Ring" : null);

		scenario("super_magnet_ring", "Dislocation Ring: items straight into inventory")
		.then(c -> equip(c, EnigmaticItems.SUPER_MAGNET_RING), 5)
		.then(c -> drop(c, Items.COBBLESTONE, 6), 20)
		.expect(c -> count(c, Items.COBBLESTONE) == 1 ? null : "item not in inventory, distance " + itemDistance(c));

		scenario("iron_ring", "Iron Ring: +1 armor")
		.then(c -> equip(c, EnigmaticItems.IRON_RING), 10)
		.expect(c -> attributeIs(c, "armor", c.player.getAttributeValue(Attributes.ARMOR), 1));

		scenario("golden_ring", "Exquisite Ring: +1 luck, piglins neutral")
		.then(c -> equip(c, EnigmaticItems.GOLDEN_RING), 10)
		.expect(c -> attributeIs(c, "luck", c.player.getAttributeValue(Attributes.LUCK), 1))
		.expect(c -> Services.ACCESSORIES.makesPiglinsNeutral(c.player) ? null : "piglins not neutral");

		scenario("etherium_ingot", "Etherium Ingot: fire resistant")
		.expect(c -> EnigmaticItems.ETHERIUM_INGOT.isFireResistant() ? null : "not fire resistant");

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
		.then(c -> equip(c, EnigmaticItems.VOID_PEARL), 5)
		.then(c -> c.player.addEffect(new MobEffectInstance(MobEffects.POISON, 400, 0)), 20)
		.expect(c -> c.player.hasEffect(MobEffects.POISON) ? "poison stayed" : null);

		scenario("enigmatic_item", "Heart of Creation: flight, fire and harmful effects blocked")
		.then(c -> equip(c, EnigmaticItems.ENIGMATIC_ITEM), 10)
		.expect(c -> c.player.getAbilities().mayfly ? null : "no flight")
		.then(c -> {
			c.player.addEffect(new MobEffectInstance(MobEffects.WITHER, 400, 0));
			c.player.setSecondsOnFire(10);
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
		.then(c -> equip(c, EnigmaticItems.HEAVEN_SCROLL), 45)
		.expect(c -> c.player.getAbilities().mayfly ? null : "no flight");

		scenario("golem_heart", "Heart of the Golem: armor with no armor worn")
		.then(c -> equip(c, EnigmaticItems.GOLEM_HEART), 10)
		.expect(c -> c.player.getAttributeValue(Attributes.ARMOR) > 0 ? null : "armor 0");

		// The playtest world is kept between runs, so the advancement is revoked first.
		scenario("desolation_advancement", "Ring of Desolation: equip advancement (Curios trigger replaced)")
		.then(c -> {
			Advancement adv = c.level.getServer().getAdvancements().getAdvancement(DESOLATION_ADVANCEMENT);
			if (adv != null)
				c.player.getAdvancements().revoke(adv, "infinitum");
		}, 2)
		.then(c -> equip(c, EnigmaticItems.DESOLATION_RING), 10)
		.expect(c -> {
			Advancement adv = c.level.getServer().getAdvancements().getAdvancement(DESOLATION_ADVANCEMENT);
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
			var recipe = c.level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, c.level);

			if (recipe.isEmpty())
				return "no recipe matched";

			ItemStack result = ((CraftingRecipe) recipe.get()).assemble(grid, c.level.registryAccess());
			return result.is(Items.IRON_PICKAXE) && result.getDamageValue() < 200 ? null : "result " + result
					+ " damage " + result.getDamageValue();
		});
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
			MobEffect effect = (MobEffect) pairs[i];
			int amplifier = (Integer) pairs[i + 1];
			MobEffectInstance instance = c.player.getEffect(effect);

			if (instance == null || (amplifier >= 0 && instance.getAmplifier() != amplifier)) {
				missing.add(effect.getDisplayName().getString() + (instance == null ? "" : " amp " + instance.getAmplifier()));
			}
		}

		return missing.isEmpty() ? null : "missing/wrong: " + String.join(", ", missing);
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
			SuperpositionHandler.unlockSpecialSlot("spellstone", ctx.player);
			SuperpositionHandler.unlockSpecialSlot("scroll", ctx.player);
			LOGGER.info("[playtest] Running {} scenarios", SCENARIOS.size());
			wait = 10;
			return;
		}

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
		lines.add("# Playtest results (" + RESULTS.size() + " scenarios, " + failures + " failed)");
		lines.add("");
		lines.add("| Scenario | Checks | Result |");
		lines.add("|---|---|---|");
		lines.addAll(RESULTS);

		try {
			Path file = FabricLoader.getInstance().getGameDir().resolve("../../test-output/playtest/results.md").normalize();
			Files.createDirectories(file.getParent());
			Files.write(file, lines);
		} catch (Exception ex) {
			LOGGER.error("[playtest] Could not write results", ex);
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
		final ServerPlayer player;
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

		/** Runs a check; a non-null message fails the scenario. */
		Scenario expect(Function<Ctx, String> check) {
			this.steps.add(new Step(check, 0));
			return this;
		}
	}
}
