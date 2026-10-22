package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;

/**
 * Port: Forge's access transformer makes {@code goalSelector} public; Fabric reaches it through
 * this accessor instead.
 */
@Mixin(Mob.class)
public interface AccessorMob {
	@Accessor("goalSelector")
	GoalSelector enigmaticlegacy$getGoalSelector();
}
