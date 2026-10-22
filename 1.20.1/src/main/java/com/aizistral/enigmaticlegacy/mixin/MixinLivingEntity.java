package com.aizistral.enigmaticlegacy.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.aizistral.enigmaticlegacy.items.MonsterCharm;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Port-only. Stands in for the Forge events LivingHurtEvent and LivingExperienceDropEvent
 * (see docs/event-hooks-1.20.1.md). Each handler keeps upstream's logic in the item class.
 */
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
	@Shadow @Nullable protected Player lastHurtByPlayer;

	/**
	 * Forge fires LivingHurtEvent at the head of actuallyHurt, before armor and absorption.
	 * Player overrides actuallyHurt without calling super, so this does not cover players
	 * as targets; add a Player mixin when an item needs that.
	 */
	@ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true)
	private float enigmaticlegacy$onHurt(float amount, DamageSource source) {
		LivingEntity self = (LivingEntity) (Object) this;
		return MonsterCharm.modifyHurtAmount(self, source, amount);
	}

	/** Forge's LivingExperienceDropEvent wraps the amount passed to ExperienceOrb.award. */
	@ModifyArg(method = "dropExperience", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V"),
			index = 2)
	private int enigmaticlegacy$onExperienceDrop(int experience) {
		LivingEntity self = (LivingEntity) (Object) this;
		return MonsterCharm.modifyExperience(self, this.lastHurtByPlayer, experience);
	}
}
