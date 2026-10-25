package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.client.InsigniaClient;
import com.aizistral.enigmaticlegacy.client.renderers.EnigmaticLayerState;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;

/**
 * Port-only. Stands in for upstream's {@code RenderNameTagEvent} handler, which forces the
 * local player's own nameplate to render (third person) while a Charming Insignia with
 * tag display enabled is worn. Also copies the elytra/shield layer data onto the 26.3 render state.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer {

	@Inject(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z", at = @At("HEAD"), cancellable = true)
	private void enigmaticlegacy$onShouldShowName(LivingEntity entity, double distanceToCameraSq, CallbackInfoReturnable<Boolean> info) {
		if (InsigniaClient.shouldShowOwnName(entity)) {
			info.setReturnValue(true);
		}
	}

	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
	private void enigmaticlegacy$onExtractRenderState(LivingEntity entity, LivingEntityRenderState state, float partialTicks, CallbackInfo info) {
		EnigmaticLayerState.extract(entity, state);
	}

}
