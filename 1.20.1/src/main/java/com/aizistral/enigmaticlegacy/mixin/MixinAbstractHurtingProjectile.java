package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;
import com.llamalad7.mixinextras.injector.WrapWithCondition;

import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.phys.HitResult;

/** Port-only. Forge's ProjectileImpactEvent; see MixinAbstractArrow. */
@Mixin(AbstractHurtingProjectile.class)
public abstract class MixinAbstractHurtingProjectile {
	@WrapWithCondition(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/projectile/AbstractHurtingProjectile;onHit(Lnet/minecraft/world/phys/HitResult;)V"))
	private boolean enigmaticlegacy$onProjectileImpact(AbstractHurtingProjectile self, HitResult result) {
		return !EnigmaticEventHandler.onProjectileImpact(self, result);
	}
}
