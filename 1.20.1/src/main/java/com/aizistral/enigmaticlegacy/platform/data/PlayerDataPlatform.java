package com.aizistral.enigmaticlegacy.platform.data;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.api.capabilities.IPlaytimeCounter;

import net.minecraft.world.entity.player.Player;

/**
 * Port-only. Replaces Forge capabilities attached to players
 * (Cardinal Components API on Fabric).
 */
public interface PlayerDataPlatform {
	/** The player's playtime counter, or null if the player has none. */
	@Nullable
	IPlaytimeCounter getPlaytimeCounter(Player player);
}
