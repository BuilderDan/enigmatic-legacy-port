package com.aizistral.enigmaticlegacy.fabric.platform;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
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
	public void readData(ValueInput input) {
		this.deserializeNBT(input.read("data", CompoundTag.CODEC).orElseGet(CompoundTag::new));
	}

	@Override
	public void writeData(ValueOutput output) {
		output.store("data", CompoundTag.CODEC, this.serializeNBT());
	}
}
