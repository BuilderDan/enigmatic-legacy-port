package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.aizistral.enigmaticlegacy.client.BlazingCoreClient;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;

/**
 * Port-only. Stands in for Forge's {@code ViewportEvent.RenderFog}, which Forge fires at the
 * end of setupFog.
 */
@Mixin(FogRenderer.class)
public abstract class MixinFogRenderer {
	@Inject(method = "setupFog", at = @At("TAIL"))
	private static void enigmaticlegacy$onSetupFog(Camera camera, FogRenderer.FogMode mode, float farPlaneDistance, boolean thickFog, float partialTick, CallbackInfo info) {
		BlazingCoreClient.onSetupFog(camera);
	}
}
