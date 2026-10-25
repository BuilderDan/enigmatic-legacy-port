package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.aizistral.enigmaticlegacy.client.BlazingCoreClient;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.LavaFogEnvironment;

/**
 * Port-only. Stands in for Forge's {@code ViewportEvent.RenderFog}. 26.3 computes lava fog in
 * LavaFogEnvironment, so the Blazing Core / Molten Heart fog distance is applied after it.
 */
@Mixin(LavaFogEnvironment.class)
public abstract class MixinFogRenderer {
	@Inject(method = "setupFog", at = @At("TAIL"))
	private void enigmaticlegacy$onSetupFog(FogData fog, Camera camera, ClientLevel level, float renderDistance, DeltaTracker deltaTracker, CallbackInfo info) {
		BlazingCoreClient.onSetupFog(fog, camera);
	}
}
