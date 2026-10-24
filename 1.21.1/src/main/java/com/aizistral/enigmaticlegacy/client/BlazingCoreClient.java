package com.aizistral.enigmaticlegacy.client;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.items.BlazingCore;
import com.aizistral.enigmaticlegacy.objects.FireImmunityData;
import com.aizistral.enigmaticlegacy.effects.MoltenHeartEffect;
import com.aizistral.enigmaticlegacy.registries.EnigmaticEffects;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FogType;

/**
 * Port: the Blazing Core's client parts of upstream's EnigmaticEventHandler (the heat bar from
 * {@code RenderGuiOverlayEvent.Pre}, lava fog from {@code ViewportEvent.RenderFog}, and the
 * client half of the effect speedup from {@code LivingTickEvent}). Called from MixinGui,
 * MixinFogRenderer and the Fabric client tick.
 */
public class BlazingCoreClient {
	public static final ResourceLocation FIREBAR_LOCATION = ResourceLocation.fromNamespaceAndPath(EnigmaticLegacy.MODID, "textures/gui/firebar.png");

	// Port: only the local player; upstream LivingTickEvent also sped up other players' effects
	// on the client, which has no visible result.
	public static void onClientTick(Minecraft mc) {
		if (mc.player != null && !mc.isPaused()) {
			EnigmaticEventHandler.tickBlazingCoreEffects(mc.player);
		}
	}

	public static void onDisconnect() {
		FireImmunityData.setClient(0, 0);
	}

	/** @return true if the heat bar was drawn in place of the experience bar. */
	public static boolean renderHeatBar(GuiGraphics graphics, int screenWidth, int screenHeight) {
		Minecraft mc = Minecraft.getInstance();

		if (mc.player == null || FireImmunityData.getClientTimer() <= 0 || !SuperpositionHandler.hasCurio(mc.player, EnigmaticItems.BLAZING_CORE))
			return false;

		float barFiller = FireImmunityData.getClientFraction();
		int x = screenWidth / 2 - 91;

		int k = (int)(barFiller * 183.0F);
		int l = screenHeight - 32 + 3;
		graphics.blit(FIREBAR_LOCATION, x, l, 0, 0, 182, 5, 256, 256);
		if (k > 0) {
			graphics.blit(FIREBAR_LOCATION, x, l, 0, 5, k, 5, 256, 256);
		}

		String title = I18n.get("gui.enigmaticlegacy.blazing_core_bar_title");
		int i1 = (screenWidth - mc.font.width(title)) / 2;
		int j1 = screenHeight - 31 - 4;

		try {
			i1 += Integer.parseInt(I18n.get("gui.enigmaticlegacy.blazing_core_bar_offsetX"));
			j1 += Integer.parseInt(I18n.get("gui.enigmaticlegacy.blazing_core_bar_offsetY"));
		} catch (Exception ex) {
			// NO-OP
		}

		int boundaryColor = 5832704;

		graphics.drawString(mc.font, title, i1 + 1, j1, boundaryColor);
		graphics.drawString(mc.font, title, i1 - 1, j1, boundaryColor);
		graphics.drawString(mc.font, title, i1, j1 + 1, boundaryColor);
		graphics.drawString(mc.font, title, i1, j1 - 1, boundaryColor);
		graphics.drawString(mc.font, title, i1, j1, 16770638);
		return true;
	}

	public static void onSetupFog(Camera camera) {
		Minecraft mc = Minecraft.getInstance();

		if (camera.getFluidInCamera() == FogType.LAVA && mc.player != null)
			if (mc.player.hasEffect(EnigmaticEffects.MOLTEN_HEART)) {
				RenderSystem.setShaderFogStart(0.0F);
				RenderSystem.setShaderFogEnd((float) MoltenHeartEffect.lavafogDensity.getValue());
			} else if (SuperpositionHandler.hasCurio(mc.player, EnigmaticItems.BLAZING_CORE)) {
				RenderSystem.setShaderFogStart(0.0F);
				RenderSystem.setShaderFogEnd((float) BlazingCore.lavafogDensity.getValue());
			}
	}
}
