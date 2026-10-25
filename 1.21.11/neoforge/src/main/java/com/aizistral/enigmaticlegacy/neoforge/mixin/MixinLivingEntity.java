package com.aizistral.enigmaticlegacy.neoforge.mixin;

import java.util.Stack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.damagesource.DamageContainer;

/**
 * Port-only. NeoForge's actuallyHurt reads the damage from its DamageContainer and ignores the
 * parameter, so the amount the common hurt hook returns (already in the parameter by this point)
 * is copied into the container here.
 */
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
	@Shadow
	protected Stack<DamageContainer> damageContainers;

	@Inject(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isInvulnerableTo(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;)Z"))
	private void enigmaticlegacy$syncDamage(ServerLevel level, DamageSource source, float amount, CallbackInfo info) {
		if (this.damageContainers != null && !this.damageContainers.isEmpty()) {
			this.damageContainers.peek().setNewDamage(amount);
		}
	}
}
