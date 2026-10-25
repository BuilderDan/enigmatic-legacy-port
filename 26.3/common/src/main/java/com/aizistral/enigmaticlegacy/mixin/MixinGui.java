package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.aizistral.enigmaticlegacy.client.BlazingCoreClient;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.items.ForbiddenFruit;
import com.aizistral.enigmaticlegacy.items.OceanStone;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import net.minecraft.client.DeltaTracker;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;

/**
 * Port-only. Stands in for Forge's {@code RenderGuiOverlayEvent.Pre} on the experience bar,
 * which upstream cancels to draw the Blazing Core's heat bar, and for its
 * {@code FOOD_LEVEL} overlay, which upstream replaces for players who ate the Forbidden Fruit,
 * and its {@code AIR_LEVEL} overlay, which upstream hides for Ocean Stone, Void Pearl and Cube wearers.
 */
@Mixin(Hud.class)
public abstract class MixinGui {
	/**
	 * Port: 26.3 draws the experience bar as a ContextualBar background from Hud.
	 * NeoForge moves that call into its own layer method, extractContextualInfoBarBackground.
	 */
	@WrapOperation(method = { "extractHotbarAndDecorations", "extractContextualInfoBarBackground" }, at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
	private void enigmaticlegacy$renderHeatBar(ContextualBar bar, GuiGraphicsExtractor graphics, DeltaTracker delta, Operation<Void> original) {
		if (!BlazingCoreClient.renderHeatBar(graphics, graphics.guiWidth(), graphics.guiHeight())) {
			original.call(bar, graphics, delta);
		}
	}

	@Unique
	private static final Identifier ENIGMATIC_ICONS = Identifier.fromNamespaceAndPath("enigmaticlegacy", "textures/gui/generic_icons.png");

	/**
	 * Port: in 26.3 the food bar is drawn in Hud.extractFood with sprites, three per shank: the
	 * empty background (food_empty or food_empty_hunger), then the full and half overlays.
	 */
	@WrapOperation(method = "extractFood", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
	private void enigmaticlegacy$renderFoodIcon(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier sprite, int x, int y,
			int width, int height, Operation<Void> original) {
		LocalPlayer player = Minecraft.getInstance().player;

		if (player != null && EnigmaticItems.FORBIDDEN_FRUIT.haveConsumedFruit(player)) {
			if (!ForbiddenFruit.renderHungerBar.getValue())
				return;

			if (ForbiddenFruit.replaceHungerBar.getValue() && !player.isCreative() && !player.isSpectator()) {
				// Upstream draws one fixed icon per shank, in place of the background; the overlays are skipped.
				if (sprite.getPath().startsWith("hud/food_empty")) {
					graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, ENIGMATIC_ICONS, x, y, 0, 0, 9, 9, 256, 256);
				}
				return;
			}
		}

		original.call(graphics, pipeline, sprite, x, y, width, height);
	}

	/** Every sprite drawn by extractAirBubbles is an oxygen bubble. */
	@WrapOperation(method = "extractAirBubbles", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
	private void enigmaticlegacy$renderAirBubble(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier sprite, int x, int y,
			int width, int height, Operation<Void> original) {
		LocalPlayer player = Minecraft.getInstance().player;

		if (player != null && OceanStone.preventOxygenBarRender.getValue()
				&& (SuperpositionHandler.hasCurio(player, EnigmaticItems.OCEAN_STONE) || SuperpositionHandler.hasCurio(player, EnigmaticItems.VOID_PEARL)
						|| SuperpositionHandler.hasCurio(player, EnigmaticItems.THE_CUBE)))
			return;

		original.call(graphics, pipeline, sprite, x, y, width, height);
	}
}
