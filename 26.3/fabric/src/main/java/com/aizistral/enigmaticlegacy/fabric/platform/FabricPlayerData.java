package com.aizistral.enigmaticlegacy.fabric.platform;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.api.capabilities.IPlaytimeCounter;
import com.aizistral.enigmaticlegacy.platform.data.PlayerDataPlatform;

import net.minecraft.world.entity.player.Player;

/**
 * Port-only. {@link PlayerDataPlatform} on Cardinal Components.
 */
public class FabricPlayerData implements PlayerDataPlatform {

	@Override
	@Nullable
	public IPlaytimeCounter getPlaytimeCounter(Player player) {
		return EnigmaticComponents.PLAYTIME_COUNTER.getNullable(player);
	}
}
