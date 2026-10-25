package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;

import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;

/**
 * Port-only. Stands in for Caelus' {@code RenderCapeEvent}, which upstream cancels to hide
 * the cape while the Majestic Elytra is worn (chest or accessory slot). 26.3: the cape layer
 * reads {@code showCape} from the render state, so it is cleared there.
 */
@Mixin(AvatarRenderer.class)
public abstract class MixinAvatarRenderer {

	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
	private void enigmaticlegacy$onExtractRenderState(Avatar entity, AvatarRenderState state, float partialTicks, CallbackInfo info) {
		if (entity instanceof Player player && SuperpositionHandler.hasEnigmaticElytra(player)) {
			state.showCape = false;
		}
	}

}
