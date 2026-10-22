package com.aizistral.enigmaticlegacy.fabric.platform;

import com.aizistral.enigmaticlegacy.api.capabilities.PlayerPlaytimeCounter;

import dev.onyxstudios.cca.api.v3.component.Component;
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
	public void readFromNbt(CompoundTag tag) {
		this.deserializeNBT(tag);
	}

	@Override
	public void writeToNbt(CompoundTag tag) {
		tag.merge(this.serializeNBT());
	}
}
