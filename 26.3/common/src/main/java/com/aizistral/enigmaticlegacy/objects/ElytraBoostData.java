package com.aizistral.enigmaticlegacy.objects;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import net.minecraft.world.entity.player.Player;

/**
 * Port-only: holds the Majestic Elytra boosting flag. Upstream keeps it in
 * {@code TransientPlayerData} (elytraBoosting), which is not ported. The server keeps one
 * flag per player; clients keep the UUIDs of boosting players, which
 * {@code PacketSyncElytraBoosting} updates, so they can draw the boost particles.
 */
public class ElytraBoostData {
	private static final Map<Player, Boolean> SERVER_DATA = new WeakHashMap<>();
	private static final Set<UUID> CLIENT_DATA = new HashSet<>();

	public static boolean isBoosting(Player player) {
		if (player.level().isClientSide())
			return CLIENT_DATA.contains(player.getUUID());
		else
			return SERVER_DATA.getOrDefault(player, false);
	}

	/**
	 * Sets the server-side flag and returns whether it changed.
	 */
	public static boolean setServer(Player player, boolean boosting) {
		Boolean old = SERVER_DATA.put(player, boosting);
		return (old != null && old) != boosting;
	}

	public static void setClient(UUID id, boolean boosting) {
		if (boosting) {
			CLIENT_DATA.add(id);
		} else {
			CLIENT_DATA.remove(id);
		}
	}

	public static void clear() {
		SERVER_DATA.clear();
	}

	public static void clearClient() {
		CLIENT_DATA.clear();
	}
}
