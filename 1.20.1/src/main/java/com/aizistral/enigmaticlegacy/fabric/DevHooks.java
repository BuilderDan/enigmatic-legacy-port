package com.aizistral.enigmaticlegacy.fabric;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Port-only, dev runs only. Shared state between the playtest scenarios (server thread) and
 * {@link DevPlaytest} (client thread), filled by the mixins in {@code fabric.devmixin}, which
 * {@link DevMixinPlugin} applies only when a playtest or devcheck property is set.
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
	/** Makes Screen.hasShiftDown / hasControlDown report true, for tooltip dumps. */
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

	private static boolean isSet(String property) {
		String value = System.getProperty(property, "");
		return !value.isEmpty() && !value.equals("false");
	}
}
