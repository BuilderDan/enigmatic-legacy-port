package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;

/**
 * Port: Forge's access transformer makes {@code explosionPower} public; Fabric reaches it through
 * this accessor instead.
 */
@Mixin(LargeFireball.class)
public interface AccessorLargeFireball {
	@Accessor("explosionPower")
	void enigmaticlegacy$setExplosionPower(int explosionPower);
}
