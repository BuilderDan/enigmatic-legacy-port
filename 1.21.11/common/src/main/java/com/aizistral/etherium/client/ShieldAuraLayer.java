package com.aizistral.etherium.client;

import com.aizistral.enigmaticlegacy.client.renderers.EnigmaticLayerState;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Port: 26.3 layers render from the render state (see vanilla EnergySwirlLayer). The shield flag
 * is carried there by {@link EnigmaticLayerState}; the arm poses come from the parent player
 * model's state, so upstream's own arm-pose copy is gone. The tint -8355712 is vanilla's
 * 0.5 gray, as upstream's (0.5, 0.5, 0.5) color.
 */
public class ShieldAuraLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
	private static final Identifier WITHER_ARMOR = Identifier.fromNamespaceAndPath("enigmaticlegacy", "textures/models/misc/ultimate_wither_armor.png");

	public ShieldAuraLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
		super(renderer);
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
		if (EnigmaticLayerState.of(state).enigmaticlegacy$hasShield()) {
			float f = state.ageInTicks;
			float scale = 1.05F;

			poseStack.pushPose();
			poseStack.scale(scale, scale, scale);
			poseStack.translate(0, -(scale - 1F) / 2F, 0);
			collector.order(1).submitModel(this.getParentModel(), state, poseStack,
					RenderTypes.energySwirl(getTextureLocation(), xOffset(f) % 1.0F, f * 0.01F % 1.0F),
					light, OverlayTexture.NO_OVERLAY, -8355712, null, state.outlineColor, null);
			poseStack.popPose();
		}
	}

	public static float xOffset(float p_225634_1_) {
		return Mth.cos(p_225634_1_ * 0.02F) * 2.0F;
	}

	public static Identifier getTextureLocation() {
		return ShieldAuraLayer.WITHER_ARMOR;
	}
}
