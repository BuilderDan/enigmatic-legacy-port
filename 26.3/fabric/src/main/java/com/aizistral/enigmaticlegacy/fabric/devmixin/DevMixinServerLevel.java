package com.aizistral.enigmaticlegacy.fabric.devmixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.aizistral.enigmaticlegacy.dev.DevHooks;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

/** Port-only, dev runs only: logs server-side sounds into {@link DevHooks#SOUNDS}. */
@Mixin(ServerLevel.class)
public class DevMixinServerLevel {
	@Inject(method = "playSeededSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V", at = @At("HEAD"))
	private void onPlaySound(Entity except, double x, double y, double z, Holder<SoundEvent> sound, SoundSource source,
			float volume, float pitch, long seed, CallbackInfo info) {
		DevHooks.SOUNDS.add(sound.value().location().toString());
	}

	@Inject(method = "playSeededSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V", at = @At("HEAD"))
	private void onPlayEntitySound(Entity except, Entity entity, Holder<SoundEvent> sound, SoundSource source,
			float volume, float pitch, long seed, CallbackInfo info) {
		DevHooks.SOUNDS.add(sound.value().location().toString());
	}
}
