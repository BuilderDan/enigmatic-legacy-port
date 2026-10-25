package com.aizistral.enigmaticlegacy.fabric.devmixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.dev.DevHooks;

import net.minecraft.client.Minecraft;

/**
 * Port-only, dev runs only: lets the tooltip dump read the Shift and Ctrl tooltip pages.
 * 26.3 moved the static Screen.hasShiftDown / hasControlDown to instance methods on Minecraft.
 */
@Mixin(Minecraft.class)
public class DevMixinMinecraft {
	@Inject(method = "hasShiftDown", at = @At("HEAD"), cancellable = true)
	private void onHasShiftDown(CallbackInfoReturnable<Boolean> info) {
		if (DevHooks.shiftDown)
			info.setReturnValue(true);
	}

	@Inject(method = "hasControlDown", at = @At("HEAD"), cancellable = true)
	private void onHasControlDown(CallbackInfoReturnable<Boolean> info) {
		if (DevHooks.controlDown)
			info.setReturnValue(true);
	}
}
