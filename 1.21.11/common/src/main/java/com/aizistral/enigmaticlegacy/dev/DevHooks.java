package com.aizistral.enigmaticlegacy.dev;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.aizistral.enigmaticlegacy.platform.Services;

/**
 * Port-only, dev runs only. Shared state between the playtest scenarios (server thread) and
 * {@link DevPlaytest} (client thread), filled by each loader's dev mixins, which its
 * dev mixin plugin applies only when a playtest or devcheck property is set.
 */
public final class DevHooks {
	/** Ids of every sound played since the last clear, on the server or the client. */
	public static final Set<String> SOUNDS = ConcurrentHashMap.newKeySet();
	/** Key mappings the client presses for one tick: ender_ring, xp_scroll, spellstone. */
	public static final Queue<String> KEYS = new ConcurrentLinkedQueue<>();
	/** Screenshot names the client grabs into test-output/playtest/screenshots. */
	public static final Queue<String> SHOTS = new ConcurrentLinkedQueue<>();
	/** Chat prefix of a key or screenshot request a dedicated server sends to the client. */
	public static final String REQUEST = "[playtest] ";
	/** Makes Minecraft.hasShiftDown / hasControlDown report true, for tooltip dumps. */
	public static volatile boolean shiftDown = false, controlDown = false;

	private DevHooks() {}

	public static boolean enabled() {
		return isSet("enigmaticlegacy.playtest") || isSet("enigmaticlegacy.devcheck");
	}

	public static String playtestMode() {
		return System.getProperty("enigmaticlegacy.playtest", "");
	}

	public static boolean exitAfter() {
		return playtestMode().contains("exit");
	}

	/** The loader's dev hooks; loaded on first use, never during mixin setup. */
	public static DevPlatform platform() {
		return Holder.PLATFORM;
	}

	public static DevClientPlatform client() {
		return ClientHolder.CLIENT;
	}

	/** A file in test-output/playtest/&lt;loader&gt;, next to the loader's run folders. */
	public static Path output(String name) {
		return Services.PLATFORM.getGameDir().resolve("../../test-output/playtest/"
				+ Services.PLATFORM.getLoaderName().toLowerCase(Locale.ROOT) + "/" + name).normalize();
	}

	private static final class Holder {
		static final DevPlatform PLATFORM = Services.load(DevPlatform.class);
	}

	private static final class ClientHolder {
		static final DevClientPlatform CLIENT = Services.load(DevClientPlatform.class);
	}

	private static boolean isSet(String property) {
		String value = System.getProperty(property, "");
		return !value.isEmpty() && !value.equals("false");
	}
}
