package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;
import com.llamalad7.mixinextras.injector.WrapWithCondition;

import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.phys.HitResult;

/**
 * Port-only. Same ProjectileImpactEvent stand-in as MixinAbstractArrow, for shulker bullets,
 * so the Violet amulet (and Angel's Blessing, Cube) can deflect them.
 */
@Mixin(ShulkerBullet.class)
public abstract class MixinShulkerBullet {
	@WrapWithCondition(method = "tick", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/projectile/ShulkerBullet;onHit(Lnet/minecraft/world/phys/HitResult;)V"))
	private boolean enigmaticlegacy$onProjectileImpact(ShulkerBullet self, HitResult result) {
		return !EnigmaticEventHandler.onProjectileImpact(self, result);
	}
}
