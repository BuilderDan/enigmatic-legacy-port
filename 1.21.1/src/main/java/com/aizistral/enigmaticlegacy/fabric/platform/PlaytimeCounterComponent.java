package com.aizistral.enigmaticlegacy.fabric.platform;

import net.minecraft.core.HolderLookup;
import com.aizistral.enigmaticlegacy.api.capabilities.PlayerPlaytimeCounter;

import org.ladysnake.cca.api.v3.component.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Port-only. Attaches {@link PlayerPlaytimeCounter} to players through Cardinal Components.
 */
public class PlaytimeCounterComponent extends PlayerPlaytimeCounter implements Component {

	public PlaytimeCounterComponent(Player player) {
		super(player);
	}

	@Override
	public void readFromNbt(CompoundTag tag, HolderLookup.Provider registries) {
		this.deserializeNBT(tag);
	}

	@Override
	public void writeToNbt(CompoundTag tag, HolderLookup.Provider registries) {
		tag.merge(this.serializeNBT());
	}
}
