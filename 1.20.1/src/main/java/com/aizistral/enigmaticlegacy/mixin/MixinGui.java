package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.aizistral.enigmaticlegacy.client.BlazingCoreClient;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Port-only. Stands in for Forge's {@code RenderGuiOverlayEvent.Pre} on the experience bar,
 * which upstream cancels to draw the Blazing Core's heat bar.
 */
@Mixin(Gui.class)
public abstract class MixinGui {
	@Shadow
	protected int screenWidth;
	@Shadow
	protected int screenHeight;

	@Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
	private void enigmaticlegacy$renderHeatBar(GuiGraphics graphics, int x, CallbackInfo info) {
		if (BlazingCoreClient.renderHeatBar(graphics, this.screenWidth, this.screenHeight)) {
			info.cancel();
		}
	}
}
