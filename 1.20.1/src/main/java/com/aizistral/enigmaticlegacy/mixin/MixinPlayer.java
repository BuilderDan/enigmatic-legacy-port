package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;
import com.aizistral.enigmaticlegacy.registries.EnigmaticAttributes;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port-only. Gives players the port's block reach attribute, and stands in for Forge's
 * {@code PlayerEvent.BreakSpeed}, which Forge fires at the end of getDestroySpeed.
 */
@Mixin(Player.class)
public abstract class MixinPlayer {
	@Inject(method = "createAttributes", at = @At("RETURN"))
	private static void enigmaticlegacy$addAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> info) {
		info.getReturnValue().add(EnigmaticAttributes.BLOCK_REACH);
	}

	@Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
	private void enigmaticlegacy$onBreakSpeed(BlockState state, CallbackInfoReturnable<Float> info) {
		info.setReturnValue(EnigmaticEventHandler.onBreakSpeed((Player) (Object) this, info.getReturnValueF()));
	}
}
