package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;
import com.llamalad7.mixinextras.injector.WrapWithCondition;

import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.HitResult;

/**
 * Port-only. Same ProjectileImpactEvent stand-in as MixinAbstractArrow, for fishing hooks,
 * so the Violet amulet (and Angel's Blessing, Cube) can deflect them.
 */
@Mixin(FishingHook.class)
public abstract class MixinFishingHook {
	@WrapWithCondition(method = "checkCollision", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/projectile/FishingHook;onHit(Lnet/minecraft/world/phys/HitResult;)V"))
	private boolean enigmaticlegacy$onProjectileImpact(FishingHook self, HitResult result) {
		return !EnigmaticEventHandler.onProjectileImpact(self, result);
	}
}
