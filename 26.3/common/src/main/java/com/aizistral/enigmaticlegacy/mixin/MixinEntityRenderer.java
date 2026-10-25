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
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

	// Port: renderNameTag was split into extractNameTags (picks the text) and submitNameDisplay (draws it) in 26.x.
	@Inject(method = "extractNameTags(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;FDD)V", at = @At("TAIL"))
	private void enigmaticlegacy$onExtractNameTags(Entity entity, EntityRenderState state, float partialTicks, double nameTagDistance, double belowNameDistance, CallbackInfo info) {
		InsigniaClient.extractInsigniaNameplate(entity, state);
	}

	@Inject(method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V", at = @At("TAIL"))
	private void enigmaticlegacy$onSubmitNameDisplay(EntityRenderState state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera, int offset, CallbackInfo info) {
		InsigniaClient.submitTrueName(state, stack, collector, camera, offset);
	}

}
