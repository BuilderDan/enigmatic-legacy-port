package com.aizistral.enigmaticlegacy.neoforge.devmixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MouseHandler;

/**
 * Port-only, playtest and devcheck clients only: the game never grabs the system mouse, even if
 * Dan clicks the test window while it runs.
 */
@Mixin(MouseHandler.class)
public class DevMixinMouseHandler {
	@Inject(method = "grabMouse", at = @At("HEAD"), cancellable = true)
	private void neverGrab(CallbackInfo info) {
		info.cancel();
	}
}
