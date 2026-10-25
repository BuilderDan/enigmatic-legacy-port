package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.aizistral.enigmaticlegacy.client.InsigniaClient;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Entity;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

	// Port: in 1.21.11 the name tag text is picked in extractRenderState and drawn by submitNameTag, called from submit.
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V", at = @At("TAIL"))
	private void enigmaticlegacy$onExtractRenderState(Entity entity, EntityRenderState state, float partialTicks, CallbackInfo info) {
		InsigniaClient.extractInsigniaNameplate(entity, state);
	}

	// After the (possibly overridden) submitNameTag call, so the player renderer's own pose push is already popped.
	@Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;submitNameTag(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", shift = At.Shift.AFTER))
	private void enigmaticlegacy$onSubmitNameTag(EntityRenderState state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo info) {
		InsigniaClient.submitTrueName(state, stack, collector, camera, 0);
	}

}
