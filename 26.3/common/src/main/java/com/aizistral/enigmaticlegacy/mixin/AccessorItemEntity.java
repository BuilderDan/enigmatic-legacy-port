package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.item.ItemEntity;

/**
 * Port: Forge's access transformer makes {@code ItemEntity.thrower} public; Fabric reads it
 * through this accessor instead. 26.3 stores the thrower as an EntityReference.
 */
@Mixin(ItemEntity.class)
public interface AccessorItemEntity {
	@Accessor("thrower")
	EntityReference<Entity> enigmaticlegacy$getThrower();
}
