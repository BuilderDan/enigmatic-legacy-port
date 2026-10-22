package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.aizistral.enigmaticlegacy.client.BlazingCoreClient;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.items.ForbiddenFruit;
import com.aizistral.enigmaticlegacy.items.OceanStone;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Port-only. Stands in for Forge's {@code RenderGuiOverlayEvent.Pre} on the experience bar,
 * which upstream cancels to draw the Blazing Core's heat bar, and for its
 * {@code FOOD_LEVEL} overlay, which upstream replaces for players who ate the Forbidden Fruit,
 * and its {@code AIR_LEVEL} overlay, which upstream hides for Ocean Stone, Void Pearl and Cube wearers.
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

	@Unique
	private static final ResourceLocation ENIGMATIC_ICONS = new ResourceLocation("enigmaticlegacy", "textures/gui/generic_icons.png");

	/**
	 * In 1.20.1 the food bar is drawn inline in renderPlayerHealth, three blits per shank:
	 * the background, then the full and half overlays.
	 */
	@WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"),
			slice = @Slice(from = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=food"),
					to = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=air")))
	private void enigmaticlegacy$renderFoodIcon(GuiGraphics graphics, ResourceLocation texture, int x, int y,
			int u, int v, int width, int height, Operation<Void> original) {
		LocalPlayer player = Minecraft.getInstance().player;

		if (player != null && EnigmaticItems.FORBIDDEN_FRUIT.haveConsumedFruit(player)) {
			if (!ForbiddenFruit.renderHungerBar.getValue())
				return;

			if (ForbiddenFruit.replaceHungerBar.getValue() && !player.isCreative() && !player.isSpectator()) {
				// Upstream draws one fixed icon per shank. Vanilla backgrounds use u 16 (133 with
				// Hunger); the full and half overlays use other u values and are skipped.
				if (u == 16 || u == 133) {
					graphics.blit(ENIGMATIC_ICONS, x, y, 0, 0, 9, 9);
				}
				return;
			}
		}

		original.call(graphics, texture, x, y, u, v, width, height);
	}

	/** Every blit after the "air" section starts is an oxygen bubble. */
	@WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"),
			slice = @Slice(from = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=air")))
	private void enigmaticlegacy$renderAirBubble(GuiGraphics graphics, ResourceLocation texture, int x, int y,
			int u, int v, int width, int height, Operation<Void> original) {
		LocalPlayer player = Minecraft.getInstance().player;

		if (player != null && OceanStone.preventOxygenBarRender.getValue()
				&& (SuperpositionHandler.hasCurio(player, EnigmaticItems.OCEAN_STONE) || SuperpositionHandler.hasCurio(player, EnigmaticItems.VOID_PEARL)
						|| SuperpositionHandler.hasCurio(player, EnigmaticItems.THE_CUBE)))
			return;

		original.call(graphics, texture, x, y, u, v, width, height);
	}
}
