package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.EnderEyeItem;
import net.minecraft.world.item.context.UseOnContext;

/**
 * Port: upstream redirects the portal's globalLevelEvent call to post its EndPortalActivatedEvent;
 * the port injects at the same call and runs the handler directly.
 */
@Mixin(EnderEyeItem.class)
public class MixinEnderEyeItem {
	@Inject(method = "useOn", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;globalLevelEvent(ILnet/minecraft/core/BlockPos;I)V"))
	private void enigmaticlegacy$onPortalActivated(UseOnContext context, CallbackInfoReturnable<InteractionResult> info) {
		if (context.getPlayer() instanceof ServerPlayer player) {
			EnigmaticEventHandler.onEndPortal(player);
		}
	}
}
