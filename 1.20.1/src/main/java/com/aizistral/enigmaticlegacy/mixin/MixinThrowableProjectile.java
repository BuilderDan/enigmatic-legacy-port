package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;
import com.llamalad7.mixinextras.injector.WrapWithCondition;

import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.phys.HitResult;

/** Port-only. Forge's ProjectileImpactEvent; see MixinAbstractArrow. */
@Mixin(ThrowableProjectile.class)
public abstract class MixinThrowableProjectile {
	@WrapWithCondition(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/projectile/ThrowableProjectile;onHit(Lnet/minecraft/world/phys/HitResult;)V"))
	private boolean enigmaticlegacy$onProjectileImpact(ThrowableProjectile self, HitResult result) {
		return !EnigmaticEventHandler.onProjectileImpact(self, result);
	}
}
