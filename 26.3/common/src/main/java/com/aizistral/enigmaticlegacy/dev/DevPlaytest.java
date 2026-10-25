package com.aizistral.enigmaticlegacy.dev;

import java.lang.reflect.Method;
import net.minecraft.network.chat.Component;
import java.lang.reflect.Field;
import java.util.UUID;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticKeybindHandler;
import com.aizistral.enigmaticlegacy.registries.EnigmaticTabs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/**
 * Port-only, dev runs only. Enabled with {@code -Denigmaticlegacy.playtest=all} (a comma list of
 * scenario ids instead of {@code all} runs only those; add {@code ,exit} to close the client
 * afterwards); see {@code -Pplaytest} in build.gradle. From the title screen it opens (or creates)
 * the world "playtest", screenshots the creative tabs into test-output/playtest/<loader>/screenshots, then
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
			DevHooks.client().hideGameMessages(DevPlaytest::request);
		} else {
			DevPlaytestScenarios.register(MODE);
		}

		DevHooks.client().onClientTick(DevPlaytest::tick);
	}

	private static void tick(Minecraft client) {
		if (wait > 0) {
			wait--;
			return;
		}

		switch (stage) {
			case TITLE -> {
				if (client.gui.overlay() != null || !(client.gui.screen() instanceof TitleScreen))
					return;

				stage = Stage.LOADING;

				if (REMOTE) {
					LOGGER.info("[playtest] Joining the dedicated server on localhost");
					ConnectScreen.startConnecting(client.gui.screen(), client, ServerAddress.parseString("localhost"),
							new ServerData(WORLD, "localhost", ServerData.Type.OTHER), false, null);
				} else if (client.getLevelSource().levelExists(WORLD)) {
					LOGGER.info("[playtest] Loading world {}", WORLD);
					client.createWorldOpenFlows().openWorld(WORLD, () -> client.gui.setScreen(new TitleScreen()));
				} else {
					LOGGER.info("[playtest] Creating world {}", WORLD);
					LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE,
							new LevelSettings.DifficultySettings(Difficulty.NORMAL, false, false), true, WorldDataConfiguration.DEFAULT);
					client.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(20261023L, false, false),
							WorldPresets::createNormalWorldDimensions, client.gui.screen());
				}
			}
			case LOADING -> {
				if (REMOTE && client.gui.screen() instanceof DisconnectedScreen) {
					// The server is still starting: try again in a few seconds.
					client.gui.setScreen(new TitleScreen());
					stage = Stage.TITLE;
					wait = 100;
					return;
				}

				if (REMOTE && client.player != null && client.level != null) {
					client.gui.setScreen(null);
					joined = true;
					stage = Stage.SCENARIOS;
					return;
				}

				if (client.player == null || client.level == null || client.gui.screen() != null)
					return;

				// The scenarios leave the player in survival, where the creative screen falls back to the inventory.
				MinecraftServer server = client.getSingleplayerServer();
				UUID id = client.player.getUUID();
				server.execute(() -> {
					// 26.3 world settings no longer carry game rules: set them on the running server.
					server.getGameRules().set(GameRules.ADVANCE_TIME, false, server);
					server.getGameRules().set(GameRules.SPAWN_MOBS, false, server);
					server.getGameRules().set(GameRules.ADVANCE_WEATHER, false, server);
					ServerPlayer player = server.getPlayerList().getPlayer(id);
					if (player != null)
						player.setGameMode(GameType.CREATIVE);
				});

				stage = Stage.SHOTS;
				wait = 60;
			}
			case SHOTS -> {
				if (takeNextShot(client)) {
					client.gui.setScreen(null);
					stage = Stage.SCENARIOS;
					DevPlaytestScenarios.start();
				}
			}
			case SCENARIOS -> {
				if (REMOTE) {
					handleRequests(client);

					// The server stops itself once its results are written.
					if (joined && client.gui.screen() instanceof DisconnectedScreen) {
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
					client.level.disconnect(Component.empty());
					client.disconnect(new TitleScreen(), false);
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
		if (client.gui.screen() instanceof DeathScreen && client.player != null) {
			client.player.respawn();
			client.gui.setScreen(null);
		}

		if (pressed != null) {
			pressed.setDown(false);
			pressed = null;
		}

		String key = DevHooks.KEYS.poll();

		if (key != null) {
			EnigmaticKeybindHandler keys = DevHooks.client().keybindHandler();
			pressed = switch (key) {
				case "ender_ring" -> keys.enderRingKey;
				case "xp_scroll" -> keys.xpScrollKey;
				default -> keys.spellstoneAbilityKey;
			};
			KeyMapping.click(DevHooks.client().boundKey(pressed));
			pressed.setDown(true);
		}

		if (DevHooks.SHOTS.isEmpty()) {
			shotTimer = 0;
		} else if (++shotTimer >= SHOT_DELAY) {
			shotTimer = 0;
			String name = DevHooks.SHOTS.peek();

			try {
				parkCursor(client);
				DevHooks.output("screenshots").toFile().mkdirs();
				Screenshot.grab(DevHooks.output("").toFile(), name + ".png",
						client.gameRenderer.mainRenderTarget(), 1, message -> {});
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
			if (shot > 0 && client.gui.screen() instanceof CreativeModeInventoryScreen) {
				DevHooks.output("screenshots").toFile().mkdirs();
				String name = (shot - 1 < SCROLLS.length ? "main_" : "potions_") + ((shot - 1) % SCROLLS.length) + ".png";
				Screenshot.grab(DevHooks.output("").toFile(), name,
						client.gameRenderer.mainRenderTarget(), 1, message -> {});
			}

			if (shot == total) {
				shot++;
				return true;
			}

			CreativeModeTab tab = tabs[shot / SCROLLS.length];

			if (!(client.gui.screen() instanceof CreativeModeInventoryScreen)) {
				CreativeModeTabs.tryRebuildTabContents(client.player.connection.enabledFeatures(), true,
						client.level.registryAccess());
				client.gui.setScreen(new CreativeModeInventoryScreen(client.player, client.player.connection.enabledFeatures(), true));
			}

			CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen) client.gui.screen();
			// Loaders put mod tabs on later creative pages. The loader shows the page, then the tab is
			// selected through vanilla's private selectTab (names are stable: 26.3 is unobfuscated).
			DevHooks.client().showCreativePage(screen, tab);
			try {
				Method select = CreativeModeInventoryScreen.class.getDeclaredMethod("selectTab", CreativeModeTab.class);
				select.setAccessible(true);
				select.invoke(screen, tab);
			} catch (ReflectiveOperationException e) {
				throw new IllegalStateException(e);
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
