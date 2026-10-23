package com.aizistral.enigmaticlegacy.client;

import org.joml.Matrix4f;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Port: the client parts of the Charming Insignia. Holds upstream's
 * {@code SuperpositionHandler.renderInsigniaNameplate} and the {@code RenderNameTagEvent}
 * handler, moved here so the common handler does not touch client-only classes.
 */
public class InsigniaClient {

	/**
	 * Port: upstream's {@code EnigmaticEventHandler.renderNameplate}, which sets the
	 * RenderNameTagEvent result to ALLOW. Called from MixinLivingEntityRenderer.
	 */
	public static boolean shouldShowOwnName(Entity entity) {
		if (entity == Minecraft.getInstance().player) {
			Player player = Minecraft.getInstance().player;
			ItemStack insignia = SuperpositionHandler.getCurioStack(player, EnigmaticItems.INSIGNIA);

			if (insignia != null && ItemNBTHelper.getBoolean(insignia, "tagDisplayEnabled", true))
				return true;
		}

		return false;
	}

	public static void renderInsigniaNameplate(Entity entity, Component name, PoseStack stack, MultiBufferSource buffer,
			int packedLight, EntityRenderDispatcher entityRenderDispatcher, Font font) {
		double d0 = entityRenderDispatcher.distanceToSqr(entity);

		// Port: ForgeHooksClient.isNameplateInRenderDistance -> vanilla's 64-block check.
		if (d0 <= 4096.0D) {
			boolean render = font.width(name) > 0;
			boolean override = false;

			if (!render && entity != Minecraft.getInstance().player
					&& EnigmaticItems.INSIGNIA.canSeeTrueName(Minecraft.getInstance().player)) {
				name = entity.getDisplayName();
				render = override = true;
			}

			boolean flag = !entity.isDiscrete();
			float f = entity.getBbHeight() + 0.5F;
			int i = 0;
			stack.pushPose();
			stack.translate(0.0D, f, 0.0D);
			stack.mulPose(entityRenderDispatcher.cameraOrientation());
			stack.scale(-0.025F, -0.025F, 0.025F);
			Matrix4f matrix4f = stack.last().pose();
			float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
			int j = (int)(f1 * 255.0F) << 24;
			float f2 = -font.width(name) / 2;

			if (render) {
				font.drawInBatch(name, f2, i, 553648127, false, matrix4f, buffer, flag ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, j, packedLight);

				if (flag) {
					font.drawInBatch(name, f2, i, -1, false, matrix4f, buffer, Font.DisplayMode.NORMAL, 0, packedLight);
				}
			}

			if (!override && entity != Minecraft.getInstance().player
					&& EnigmaticItems.INSIGNIA.canSeeTrueName(Minecraft.getInstance().player)) {
				stack.pushPose();
				name = Component.literal("(" + entity.getDisplayName().getString() + ")");
				f2 = -font.width(name) / 2;

				float scale = 0.4F;
				int offset = (int) (-10 * (1.0F / scale));
				stack.scale(scale, scale, scale);
				matrix4f = stack.last().pose();

				font.drawInBatch(name, f2, i - offset, 553648127, false, matrix4f, buffer, flag ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, j, packedLight);

				if (flag) {
					font.drawInBatch(name, f2, i - offset, -1, false, matrix4f, buffer, Font.DisplayMode.NORMAL, 0, packedLight);
				}
				stack.popPose();
			}

			stack.popPose();
		}
	}

}
