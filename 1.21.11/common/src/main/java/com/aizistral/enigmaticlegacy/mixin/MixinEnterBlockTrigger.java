package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;

import net.minecraft.advancements.criterion.EnterBlockTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port: upstream posts its EnterBlockEvent here; the port runs the handler directly.
 */
@Mixin(EnterBlockTrigger.class)
public class MixinEnterBlockTrigger {
	@Inject(method = "trigger", at = @At("HEAD"))
	private void enigmaticlegacy$onTrigger(ServerPlayer player, BlockState state, CallbackInfo info) {
		EnigmaticEventHandler.onEnteredBlock(player, state);
	}
}
