package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;
import com.llamalad7.mixinextras.injector.WrapWithCondition;

import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.phys.HitResult;

/**
 * Port-only. Same ProjectileImpactEvent stand-in as MixinAbstractArrow, for firework rockets,
 * so the Violet amulet (and Angel's Blessing, Cube) can deflect them.
 */
@Mixin(FireworkRocketEntity.class)
public abstract class MixinFireworkRocketEntity {
	@WrapWithCondition(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/projectile/FireworkRocketEntity;onHit(Lnet/minecraft/world/phys/HitResult;)V"))
	private boolean enigmaticlegacy$onProjectileImpact(FireworkRocketEntity self, HitResult result) {
		return !EnigmaticEventHandler.onProjectileImpact(self, result);
	}
}
