package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.projectile.AbstractArrow;

/**
 * Port: was upstream's AccessorAbstractArrowEntity, whose clearHitEntities invoked the same
 * private method.
 */
@Mixin(AbstractArrow.class)
public interface AccessorAbstractArrow {
	@Invoker("resetPiercedEntities")
	void enigmaticlegacy$resetPiercedEntities();
}
