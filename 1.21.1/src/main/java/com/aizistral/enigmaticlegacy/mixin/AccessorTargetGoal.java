package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

/**
 * Port: Forge's access transformer makes {@code targetMob} public; Fabric reaches it through
 * this accessor instead.
 */
@Mixin(TargetGoal.class)
public interface AccessorTargetGoal {
	@Accessor("targetMob")
	LivingEntity enigmaticlegacy$getTargetMob();
}
