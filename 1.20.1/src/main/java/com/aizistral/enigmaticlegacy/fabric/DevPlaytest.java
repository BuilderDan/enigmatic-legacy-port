package com.aizistral.enigmaticlegacy.fabric;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

import com.aizistral.enigmaticlegacy.registries.EnigmaticTabs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.impl.client.itemgroup.CreativeGuiExtensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/**
 * Port-only, dev runs only. Enabled with {@code -Denigmaticlegacy.playtest=all} (a comma list of
 * scenario ids instead of {@code all} runs only those; add {@code ,exit} to close the client
 * afterwards); see {@code -Pplaytest} in build.gradle. From the title screen it opens (or creates)
 * the world "playtest", screenshots the creative tabs into test-output/playtest/screenshots, then
 * hands over to {@link DevPlaytestScenarios}, which runs the in-world checks on the server side.
 */
public final class DevPlaytest {
	private static final Logger LOGGER = LoggerFactory.getLogger("EnigmaticLegacy/playtest");
	private static final String MODE = System.getProperty("enigmaticlegacy.playtest", "");
	private static final String WORLD = "playtest";
	private static final float[] SCROLLS = { 0F, 0.25F, 0.5F, 0.75F, 1F };

	private enum Stage { TITLE, LOADING, SHOTS, SCENARIOS, DONE }

	private static Stage stage = Stage.TITLE;
	private static int wait = 0;
	private static int shot = 0;

	private DevPlaytest() {}

	public static boolean exitAfter() {
		return MODE.contains("exit");
	}

	public static void register() {
		if (MODE.isEmpty() || MODE.equals("false"))
			return;

		DevPlaytestScenarios.register(MODE);
		ClientTickEvents.END_CLIENT_TICK.register(DevPlaytest::tick);
	}

	private static void tick(Minecraft client) {
		if (wait > 0) {
			wait--;
			return;
		}

		switch (stage) {
			case TITLE -> {
				if (client.getOverlay() != null || !(client.screen instanceof TitleScreen))
					return;

				stage = Stage.LOADING;

				if (client.getLevelSource().levelExists(WORLD)) {
					LOGGER.info("[playtest] Loading world {}", WORLD);
					client.createWorldOpenFlows().loadLevel(client.screen, WORLD);
				} else {
					LOGGER.info("[playtest] Creating world {}", WORLD);
					GameRules rules = new GameRules();
					rules.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
					rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null);
					rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, null);
					LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE, false, Difficulty.NORMAL, true,
							rules, WorldDataConfiguration.DEFAULT);
					client.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(20261023L, false, false),
							WorldPresets::createNormalWorldDimensions);
				}
			}
			case LOADING -> {
				if (client.player == null || client.level == null || client.screen != null)
					return;

				// The scenarios leave the player in survival, where the creative screen falls back to the inventory.
				MinecraftServer server = client.getSingleplayerServer();
				UUID id = client.player.getUUID();
				server.execute(() -> {
					ServerPlayer player = server.getPlayerList().getPlayer(id);
					if (player != null)
						player.setGameMode(GameType.CREATIVE);
				});

				stage = Stage.SHOTS;
				wait = 60;
			}
			case SHOTS -> {
				if (takeNextShot(client)) {
					client.setScreen(null);
					stage = Stage.SCENARIOS;
					DevPlaytestScenarios.start();
				}
			}
			case SCENARIOS -> {
				if (!DevPlaytestScenarios.isFinished())
					return;

				stage = Stage.DONE;
				LOGGER.info("[playtest] Finished");

				if (exitAfter()) {
					client.level.disconnect();
					client.clearLevel();
					client.stop();
				}
			}
			case DONE -> {}
		}
	}

	/**
	 * Takes one creative tab screenshot per call, a few ticks apart so the screen has rendered.
	 * @return true once every tab and scroll position has been captured.
	 */
	private static boolean takeNextShot(Minecraft client) {
		CreativeModeTab[] tabs = { EnigmaticTabs.MAIN, EnigmaticTabs.POTIONS };
		int total = tabs.length * SCROLLS.length;

		if (shot > total)
			return true;

		try {
			if (shot > 0 && client.screen instanceof CreativeModeInventoryScreen) {
				new File(client.gameDirectory, "../../test-output/playtest/screenshots").mkdirs();
				String name = (shot - 1 < SCROLLS.length ? "main_" : "potions_") + ((shot - 1) % SCROLLS.length) + ".png";
				Screenshot.grab(new File(client.gameDirectory, "../../test-output/playtest"), name,
						client.getMainRenderTarget(), message -> {});
			}

			if (shot == total) {
				shot++;
				return true;
			}

			CreativeModeTab tab = tabs[shot / SCROLLS.length];

			if (!(client.screen instanceof CreativeModeInventoryScreen)) {
				CreativeModeTabs.tryRebuildTabContents(client.player.connection.enabledFeatures(), true,
						client.level.registryAccess());
				client.setScreen(new CreativeModeInventoryScreen(client.player, client.player.connection.enabledFeatures(), true));
			}

			CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen) client.screen;
			Method select = CreativeModeInventoryScreen.class.getDeclaredMethod("selectTab", CreativeModeTab.class);
			select.setAccessible(true);
			Field selected = CreativeModeInventoryScreen.class.getDeclaredField("selectedTab");
			selected.setAccessible(true);

			// Fabric puts mod tabs on later creative pages and ignores selectTab for a tab on another page.
			CreativeGuiExtensions pages = (CreativeGuiExtensions) screen;
			while (pages.fabric_currentPage() > 0)
				pages.fabric_previousPage();

			for (int page = 0; page < 10 && selected.get(null) != tab; page++) {
				select.invoke(screen, tab);
				if (selected.get(null) != tab)
					pages.fabric_nextPage();
			}

			screen.getMenu().scrollTo(SCROLLS[shot % SCROLLS.length]);

			// Park the game's cursor (not the system one) in a corner so no tooltip covers the items.
			for (String axis : new String[] { "xpos", "ypos" }) {
				Field pos = MouseHandler.class.getDeclaredField(axis);
				pos.setAccessible(true);
				pos.setDouble(client.mouseHandler, 0D);
			}
		} catch (Exception ex) {
			LOGGER.warn("[playtest] Screenshot step {} failed", shot, ex);
		}

		shot++;
		wait = 10;
		return false;
	}
}
