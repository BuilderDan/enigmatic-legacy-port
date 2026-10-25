package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

// Port: poison damage moved from MobEffect.applyEffectTick to the package-private PoisonMobEffect in 1.21.
@Mixin(targets = "net.minecraft.world.effect.PoisonMobEffect")
public class MixinMobEffect {

	@Redirect(method = "applyEffectTick", at = @At(value = "INVOKE", target =
			"Lnet/minecraft/world/entity/LivingEntity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
			ordinal = 0))
	private boolean onHurt(LivingEntity entity, ServerLevel level, DamageSource source, float amount) {
		EnigmaticEventHandler.isPoisonHurt = true;
		boolean result = entity.hurtServer(level, source, amount);
		EnigmaticEventHandler.isPoisonHurt = false;
		return result;
	}

}
