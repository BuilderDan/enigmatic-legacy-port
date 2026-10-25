package com.aizistral.enigmaticlegacy.fabric.devmixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.dev.DevHooks;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;

/** Port-only, dev runs only: logs client-side sounds into {@link DevHooks#SOUNDS}. */
@Mixin(SoundManager.class)
public class DevMixinSoundManager {
	@Inject(method = "play", at = @At("HEAD"))
	private void onPlay(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> info) {
		DevHooks.SOUNDS.add(sound.getIdentifier().toString());
	}
}
