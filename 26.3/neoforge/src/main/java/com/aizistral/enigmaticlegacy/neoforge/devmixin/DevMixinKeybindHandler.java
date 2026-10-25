package com.aizistral.enigmaticlegacy.neoforge.devmixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticKeybindHandler;

import net.minecraft.client.Minecraft;

/**
 * Port-only, dev runs only: the dev client hands focus back right after launch, so the keybind
 * handler would ignore the playtest's key presses. This skips only that focus check; the system
 * mouse is never grabbed.
 */
@Mixin(value = EnigmaticKeybindHandler.class, remap = false)
public class DevMixinKeybindHandler {
	@Redirect(method = "onKeyInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isWindowActive()Z", remap = true))
	private boolean isWindowActive(Minecraft minecraft) {
		return true;
	}
}
