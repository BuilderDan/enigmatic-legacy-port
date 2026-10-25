package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.piglin.PiglinBruteAi;

/**
 * Port: Forge's access transformer makes {@code PiglinBruteAi.wasHurtBy} public; Fabric reaches it
 * through this invoker instead.
 */
@Mixin(PiglinBruteAi.class)
public interface AccessorPiglinBruteAi {
	@Invoker("wasHurtBy")
	static void enigmaticlegacy$wasHurtBy(ServerLevel level, PiglinBrute brute, LivingEntity entity) {
		throw new AssertionError();
	}
}
