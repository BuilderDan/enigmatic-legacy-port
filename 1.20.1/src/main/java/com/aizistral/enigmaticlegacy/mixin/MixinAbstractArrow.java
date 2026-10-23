package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;
import com.llamalad7.mixinextras.injector.WrapWithCondition;

import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.HitResult;

/**
 * Port-only. Stands in for Forge's ProjectileImpactEvent, which projectiles fire in tick before
 * calling onHit; canceling it skips onHit. Same hook in MixinAbstractHurtingProjectile and
 * MixinThrowableProjectile.
 * <p>
 * Port: Forge also fires the event for fishing hooks, llama spit and shulker bullets; those are
 * not hooked yet (TODO), so the Violet amulet does not deflect them. Rockets: MixinFireworkRocketEntity.
 */
@Mixin(AbstractArrow.class)
public abstract class MixinAbstractArrow {
	@WrapWithCondition(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;onHit(Lnet/minecraft/world/phys/HitResult;)V"))
	private boolean enigmaticlegacy$onProjectileImpact(AbstractArrow self, HitResult result) {
		return !EnigmaticEventHandler.onProjectileImpact(self, result);
	}
}
