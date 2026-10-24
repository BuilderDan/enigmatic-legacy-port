package com.aizistral.enigmaticlegacy.fabric;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticKeybindHandler;
import com.aizistral.enigmaticlegacy.registries.EnigmaticTabs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.impl.client.itemgroup.CreativeGuiExtensions;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
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
 * <p>
 * With {@code remote} in the list ({@code -Pplaytest=remote,exit}) it joins a dedicated server on
 * localhost instead (started with {@code ./gradlew runServer -Pplaytest=all,exit}), which runs the
 * scenarios and sends its key and screenshot requests as chat lines; screenshots get an mp_ prefix.
 */
public final class DevPlaytest {
	private static final Logger LOGGER = LoggerFactory.getLogger("EnigmaticLegacy/playtest");
	private static final String MODE = DevHooks.playtestMode();
	private static final String WORLD = "playtest";
	private static final float[] SCROLLS = { 0F, 0.25F, 0.5F, 0.75F, 1F };

	private enum Stage { TITLE, LOADING, SHOTS, SCENARIOS, DONE }

	private static Stage stage = Stage.TITLE;
	private static int wait = 0;
	private static int shot = 0;
	private static final int SHOT_DELAY = 10;
	private static int shotTimer = 0;
	private static KeyMapping pressed = null;
	private static final boolean REMOTE = MODE.contains("remote");
	private static boolean joined = false;

	private DevPlaytest() {}

	public static void register() {
		if (MODE.isEmpty() || MODE.equals("false"))
			return;

		if (REMOTE) {
			ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> !request(message.getString()));
		} else {
			DevPlaytestScenarios.register(MODE);
		}

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

				if (REMOTE) {
					LOGGER.info("[playtest] Joining the dedicated server on localhost");
					ConnectScreen.startConnecting(client.screen, client, ServerAddress.parseString("localhost"),
							new ServerData(WORLD, "localhost", false), false);
				} else if (client.getLevelSource().levelExists(WORLD)) {
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
				if (REMOTE && client.screen instanceof DisconnectedScreen) {
					// The server is still starting: try again in a few seconds.
					client.setScreen(new TitleScreen());
					stage = Stage.TITLE;
					wait = 100;
					return;
				}

				if (REMOTE && client.player != null && client.level != null) {
					client.setScreen(null);
					joined = true;
					stage = Stage.SCENARIOS;
					return;
				}

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
				if (REMOTE) {
					handleRequests(client);

					// The server stops itself once its results are written.
					if (joined && client.screen instanceof DisconnectedScreen) {
						stage = Stage.DONE;
						LOGGER.info("[playtest] Disconnected, finished");
						if (DevHooks.exitAfter())
							client.stop();
					}
					return;
				}

				handleRequests(client);

				if (!DevPlaytestScenarios.isFinished())
					return;

				stage = Stage.DONE;
				DevPlaytestTooltips.dump(client);
				LOGGER.info("[playtest] Finished");

				if (DevHooks.exitAfter()) {
					client.level.disconnect();
					client.clearLevel();
					client.stop();
				}
			}
			case DONE -> {}
		}
	}

	/** Queues a request a dedicated server sent as a chat line; true when the line was one. */
	private static boolean request(String line) {
		if (!line.startsWith(DevHooks.REQUEST))
			return false;

		String[] parts = line.substring(DevHooks.REQUEST.length()).split(" ", 2);

		if (parts.length == 2 && parts[0].equals("key")) {
			DevHooks.KEYS.add(parts[1]);
		} else if (parts.length == 2 && parts[0].equals("shot")) {
			DevHooks.SHOTS.add("mp_" + parts[1]);
		} else
			return false;

		return true;
	}

	/**
	 * Serves the scenarios' requests, which only the client can carry out: one-tick key presses
	 * (released on the next tick) and screenshots, taken {@link #SHOT_DELAY} ticks after the request
	 * so the screen or HUD the scenario set up has reached the client and rendered.
	 */
	private static void handleRequests(Minecraft client) {
		if (client.screen instanceof DeathScreen && client.player != null) {
			client.player.respawn();
			client.setScreen(null);
		}

		if (pressed != null) {
			pressed.setDown(false);
			pressed = null;
		}

		String key = DevHooks.KEYS.poll();

		if (key != null) {
			EnigmaticKeybindHandler keys = EnigmaticLegacyFabricClient.KEYBIND_HANDLER;
			pressed = switch (key) {
				case "ender_ring" -> keys.enderRingKey;
				case "xp_scroll" -> keys.xpScrollKey;
				default -> keys.spellstoneAbilityKey;
			};
			KeyMapping.click(KeyBindingHelper.getBoundKeyOf(pressed));
			pressed.setDown(true);
		}

		if (DevHooks.SHOTS.isEmpty()) {
			shotTimer = 0;
		} else if (++shotTimer >= SHOT_DELAY) {
			shotTimer = 0;
			String name = DevHooks.SHOTS.peek();

			try {
				parkCursor(client);
				new File(client.gameDirectory, "../../test-output/playtest/screenshots").mkdirs();
				Screenshot.grab(new File(client.gameDirectory, "../../test-output/playtest"), name + ".png",
						client.getMainRenderTarget(), message -> {});
			} catch (Exception ex) {
				LOGGER.warn("[playtest] Screenshot {} failed", name, ex);
			}

			DevHooks.SHOTS.poll();
		}
	}

	// Moves the game's cursor (not the system one) to a corner so no tooltip covers the screen.
	private static void parkCursor(Minecraft client) throws ReflectiveOperationException {
		for (String axis : new String[] { "xpos", "ypos" }) {
			Field pos = MouseHandler.class.getDeclaredField(axis);
			pos.setAccessible(true);
			pos.setDouble(client.mouseHandler, 0D);
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

			parkCursor(client);
		} catch (Exception ex) {
			LOGGER.warn("[playtest] Screenshot step {} failed", shot, ex);
		}

		shot++;
		wait = 10;
		return false;
	}
}
