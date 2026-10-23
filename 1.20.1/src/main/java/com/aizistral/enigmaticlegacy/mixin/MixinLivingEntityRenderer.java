package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.client.InsigniaClient;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;

/**
 * Port-only. Stands in for upstream's {@code RenderNameTagEvent} handler, which forces the
 * local player's own nameplate to render (third person) while a Charming Insignia with
 * tag display enabled is worn.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer {

	@Inject(method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
	private void enigmaticlegacy$onShouldShowName(LivingEntity entity, CallbackInfoReturnable<Boolean> info) {
		if (InsigniaClient.shouldShowOwnName(entity)) {
			info.setReturnValue(true);
		}
	}

}
