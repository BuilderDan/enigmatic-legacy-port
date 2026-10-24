package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.phys.HitResult;

/**
 * Port-only. Stands in for Forge's ProjectileImpactEvent, which projectiles fire before handling a
 * hit; canceling it skips the hit.
 * <p>
 * Port: in 1.21 every projectile's tick (the fishing hook's checkCollision) reaches onHit through
 * hitTargetOrDeflectSelf, so this one hook replaces the 1.20.1 port's per-class tick wraps
 * (arrows, hurting projectiles, throwables, rockets, fishing hooks, llama spit, shulker bullets).
 */
@Mixin(Projectile.class)
public abstract class MixinProjectile {
	@Inject(method = "hitTargetOrDeflectSelf", at = @At("HEAD"), cancellable = true)
	private void enigmaticlegacy$onProjectileImpact(HitResult result, CallbackInfoReturnable<ProjectileDeflection> info) {
		if (EnigmaticEventHandler.onProjectileImpact((Projectile) (Object) this, result)) {
			info.setReturnValue(ProjectileDeflection.NONE);
		}
	}
}
