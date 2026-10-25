package com.aizistral.enigmaticlegacy.neoforge.platform;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.api.capabilities.PlayerPlaytimeCounter;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/**
 * Port-only. Attaches {@link PlayerPlaytimeCounter} to players through a NeoForge data
 * attachment instead of upstream's Forge capability.
 */
public class PlaytimeCounterAttachment extends PlayerPlaytimeCounter implements ValueIOSerializable {
	public static final Identifier ID = Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, "playtime_counter");
	public static final AttachmentType<PlaytimeCounterAttachment> TYPE = AttachmentType
			.serializable(holder -> new PlaytimeCounterAttachment((Player) holder)).copyOnDeath().build();

	public PlaytimeCounterAttachment(Player player) {
		super(player);
	}

	@Override
	public void deserialize(ValueInput input) {
		this.deserializeNBT(input.read("data", CompoundTag.CODEC).orElseGet(CompoundTag::new));
	}

	@Override
	public void serialize(ValueOutput output) {
		output.store("data", CompoundTag.CODEC, this.serializeNBT());
	}
}
