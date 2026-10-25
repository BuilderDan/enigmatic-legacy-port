package com.aizistral.enigmaticlegacy.objects;

import java.util.Map;
import java.util.WeakHashMap;

import com.aizistral.enigmaticlegacy.items.BlazingCore;

import net.minecraft.world.entity.player.Player;

/**
 * Port-only: holds the Blazing Core's lava immunity timer. Upstream keeps it in
 * {@code TransientPlayerData} (fireImmunityTimer, its cap and last raw value), which
 * is not ported. The server keeps one entry per player; the client only needs its own
 * player's values, which {@code PacketSyncFireImmunity} sets.
 */
public class FireImmunityData {
	public static final int FIRE_DIFF = 20 * 100;
	public static final int TIMER_CAP = 300 * 100 + FIRE_DIFF;

	private static final Map<Player, FireImmunityData> SERVER_DATA = new WeakHashMap<>();

	private static int clientTimer = 0;
	private static int clientLast = 0;

	private int timer = 0;
	private int last = 0;
	// Sync once per new ServerPlayer (join, respawn, leaving the End) so the client never keeps
	// a stale bar from the old player object.
	private boolean needsSync = true;

	public static FireImmunityData get(Player player) {
		return SERVER_DATA.computeIfAbsent(player, p -> new FireImmunityData());
	}

	public static void clear() {
		SERVER_DATA.clear();
	}

	public int getTimer() {
		return this.timer;
	}

	public int getLast() {
		return this.last;
	}

	public void setTimer(int value) {
		int newValue = Math.min(Math.max(value, 0), TIMER_CAP);

		if (newValue != this.timer) {
			this.needsSync = true;
		}

		this.last = value;
		this.timer = newValue;
	}

	/** @return true once if the timer changed since the last call. */
	public boolean pollSync() {
		boolean sync = this.needsSync;
		this.needsSync = false;
		return sync;
	}

	public static void setClient(int timer, int last) {
		clientTimer = timer;
		clientLast = last;
	}

	public static int getClientTimer() {
		return clientTimer;
	}

	/** Upstream's bar fraction (TransientPlayerData.getFireImmunityFraction). */
	public static float getClientFraction() {
		float x = (float) clientLast / (TIMER_CAP - FIRE_DIFF);

		if (BlazingCore.traitorBar.getValue())
			return x * x;
		else
			return 1F - (x - 1F) * (x - 1F);
	}
}
