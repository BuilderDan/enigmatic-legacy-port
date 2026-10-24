package com.aizistral.enigmaticlegacy.mixin;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.item.ItemEntity;

/**
 * Port: Forge's access transformer makes {@code ItemEntity.thrower} public; Fabric reads it
 * through this accessor instead.
 */
@Mixin(ItemEntity.class)
public interface AccessorItemEntity {
	@Accessor("thrower")
	UUID enigmaticlegacy$getThrower();
}
