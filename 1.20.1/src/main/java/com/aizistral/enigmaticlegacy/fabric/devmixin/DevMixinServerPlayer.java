package com.aizistral.enigmaticlegacy.fabric.devmixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.aizistral.enigmaticlegacy.fabric.DevHooks;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/** Port-only, dev runs only: logs sounds sent to one player into {@link DevHooks#SOUNDS}. */
@Mixin(ServerPlayer.class)
public class DevMixinServerPlayer {
	@Inject(method = "playNotifySound", at = @At("HEAD"))
	private void onPlayNotifySound(SoundEvent sound, SoundSource source, float volume, float pitch, CallbackInfo info) {
		DevHooks.SOUNDS.add(sound.getLocation().toString());
	}
}
