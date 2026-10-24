package com.aizistral.enigmaticlegacy.fabric.devmixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.fabric.DevHooks;

import net.minecraft.client.gui.screens.Screen;

/** Port-only, dev runs only: lets the tooltip dump read the Shift and Ctrl tooltip pages. */
@Mixin(Screen.class)
public class DevMixinScreen {
	@Inject(method = "hasShiftDown", at = @At("HEAD"), cancellable = true)
	private static void onHasShiftDown(CallbackInfoReturnable<Boolean> info) {
		if (DevHooks.shiftDown)
			info.setReturnValue(true);
	}

	@Inject(method = "hasControlDown", at = @At("HEAD"), cancellable = true)
	private static void onHasControlDown(CallbackInfoReturnable<Boolean> info) {
		if (DevHooks.controlDown)
			info.setReturnValue(true);
	}
}
