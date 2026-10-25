package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.block.entity.BeaconBlockEntity;

/**
 * Port: upstream reads {@code BeaconBlockEntity.levels} through its access transformer.
 */
@Mixin(BeaconBlockEntity.class)
public interface AccessorBeaconBlockEntity {
	@Accessor("levels")
	int enigmaticlegacy$getLevels();
}
