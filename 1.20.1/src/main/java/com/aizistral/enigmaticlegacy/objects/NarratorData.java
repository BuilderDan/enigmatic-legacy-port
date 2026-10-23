package com.aizistral.enigmaticlegacy.objects;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;

import net.minecraft.world.entity.player.Player;

/**
 * Port-only: the narrator unlock flag. Upstream keeps it in {@code TransientPlayerData}
 * (getUnlockedNarrator/setUnlockedNarrator), backed by the same persistent boolean,
 * which is not ported. Only the server reads it.
 */
public class NarratorData {
	private static final String TAG = "ELUnlockedNarrator";

	public static boolean isUnlocked(Player player) {
		return SuperpositionHandler.getPersistentBoolean(player, TAG, false);
	}

	public static void setUnlocked(Player player, boolean unlocked) {
		SuperpositionHandler.setPersistentBoolean(player, TAG, unlocked);
	}
}
