package com.aizistral.enigmaticlegacy.neoforge.platform;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.api.capabilities.IPlaytimeCounter;
import com.aizistral.enigmaticlegacy.platform.data.PlayerDataPlatform;

import net.minecraft.world.entity.player.Player;

/**
 * Port-only. {@link PlayerDataPlatform} on NeoForge data attachments.
 */
public class NeoForgePlayerData implements PlayerDataPlatform {
	@Override
	@Nullable
	public IPlaytimeCounter getPlaytimeCounter(Player player) {
		return player.getData(PlaytimeCounterAttachment.TYPE);
	}
}
