package com.aizistral.enigmaticlegacy.client.renderers;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Port: 26.3 layers render from the render state (see vanilla WingsLayer); the worn elytra is
 * carried there by {@link EnigmaticLayerState}. The enchantment glint uses the glint variant of
 * the armor render type instead of the removed foil buffer.
 */
public class EnigmaticElytraLayer<S extends HumanoidRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, "textures/models/misc/elytra.png");
	private final ElytraModel elytraModel;
	private final ElytraModel elytraBabyModel;

	public EnigmaticElytraLayer(RenderLayerParent<S, M> layerParent, EntityModelSet modelSet) {
		super(layerParent);
		this.elytraModel = new ElytraModel(modelSet.bakeLayer(ModelLayers.ELYTRA));
		this.elytraBabyModel = new ElytraModel(modelSet.bakeLayer(ModelLayers.ELYTRA_BABY));
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, S state, float yRot, float xRot) {
		ItemStack stack = EnigmaticLayerState.of(state).enigmaticlegacy$getElytra();

		if (!stack.isEmpty()) {
			ElytraModel model = state.isBaby ? this.elytraBabyModel : this.elytraModel;

			poseStack.pushPose();
			poseStack.translate(0.0F, 0.0F, 0.125F);
			collector.submitModel(model, state, poseStack, stack.hasFoil() ? RenderTypes.armorCutoutNoCullGlint(TEXTURE) : RenderTypes.armorCutoutNoCull(TEXTURE),
					light, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor);
			poseStack.popPose();
		}
	}
}
