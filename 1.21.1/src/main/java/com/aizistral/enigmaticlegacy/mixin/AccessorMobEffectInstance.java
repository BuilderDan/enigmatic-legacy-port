package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.effect.MobEffectInstance;

/**
 * Port: Forge's access transformer makes {@code duration} public; Fabric reaches it through
 * this accessor instead.
 */
@Mixin(MobEffectInstance.class)
public interface AccessorMobEffectInstance {
	@Accessor("duration")
	int enigmaticlegacy$getDuration();

	@Accessor("duration")
	void enigmaticlegacy$setDuration(int duration);
}
