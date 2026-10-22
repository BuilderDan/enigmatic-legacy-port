package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;

/**
 * Port: Forge's access transformer makes {@code PiglinAi.wasHurtBy} public; Fabric reaches it
 * through this invoker instead.
 */
@Mixin(PiglinAi.class)
public interface AccessorPiglinAi {
	@Invoker("wasHurtBy")
	static void enigmaticlegacy$wasHurtBy(Piglin piglin, LivingEntity entity) {
		throw new AssertionError();
	}
}
