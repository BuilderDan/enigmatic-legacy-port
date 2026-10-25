package com.aizistral.enigmaticlegacy.client.renderers;

import com.aizistral.enigmaticlegacy.entities.UltimateWitherSkullEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.skull.SkullModel;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

// Port: 26.3 entity renderers extract a render state and submit models from it (vanilla
// WitherSkullRenderer); the inflated head and the two energy swirl shields are unchanged.
public class UltimateWitherSkullRenderer extends EntityRenderer<UltimateWitherSkullEntity, UltimateWitherSkullRenderer.State> {
	private static final Identifier INVULNERABLE_WITHER_TEXTURES = Identifier.parse("textures/entity/wither/wither_invulnerable.png");
	// Port: Etherium's ShieldAuraLayer texture and offset inlined here (same values).
	private static final Identifier AURA_TEXTURE = Identifier.fromNamespaceAndPath("enigmaticlegacy", "textures/models/misc/ultimate_wither_armor.png");
	private static final Identifier WITHER_TEXTURES = Identifier.parse("textures/entity/wither/wither.png");
	private final SkullModel skeletonHeadModel;

	public UltimateWitherSkullRenderer(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn);
		this.skeletonHeadModel = new SkullModel(renderManagerIn.bakeLayer(ModelLayers.WITHER_SKULL));
	}

	@Override
	protected int getBlockLightLevel(UltimateWitherSkullEntity entityIn, BlockPos partialTicks) {
		return 15;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(UltimateWitherSkullEntity entityIn, State state, float partialTicks) {
		super.extractRenderState(entityIn, state, partialTicks);
		state.invulnerable = entityIn.isSkullInvulnerable();
		state.fullTicks = entityIn.tickCount + partialTicks;
		state.modelState.animationPos = 0.0F;
		state.modelState.yRot = Mth.rotLerp(partialTicks, entityIn.yRotO, entityIn.getYRot());
		state.modelState.xRot = Mth.lerp(partialTicks, entityIn.xRotO, entityIn.getXRot());
	}

	@Override
	public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		poseStack.pushPose();

		float inflate = state.invulnerable ? 1.4F : 1.0F;

		poseStack.scale(-inflate, -inflate, inflate);
		collector.submitModel(this.skeletonHeadModel, state.modelState, poseStack, this.skeletonHeadModel.renderType(this.getTextureLocation(state)), state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
		poseStack.popPose();

		if (state.invulnerable) {
			this.renderShield(state, poseStack, collector, inflate + 0.1F);
			this.renderShield(state, poseStack, collector, inflate + 0.2F);
		}

		super.submit(state, poseStack, collector, camera);
	}

	private void renderShield(State state, PoseStack matrix, SubmitNodeCollector collector, float scale) {
		matrix.pushPose();
		matrix.scale(-scale, -scale, scale);

		float fullTicks = state.fullTicks;
		collector.submitModel(this.skeletonHeadModel, state.modelState, matrix, RenderTypes.energySwirl(AURA_TEXTURE, Mth.cos(fullTicks * 0.02F) * 2.0F, fullTicks * 0.01F),
				state.lightCoords, OverlayTexture.NO_OVERLAY, 0xFF7F7F7F, null, state.outlineColor, null);

		matrix.popPose();
	}

	/**
	 * Returns the location of an entity's texture.
	 */
	private Identifier getTextureLocation(State state) {
		//return state.invulnerable ? UltimateWitherSkullRenderer.INVULNERABLE_WITHER_TEXTURES : UltimateWitherSkullRenderer.WITHER_TEXTURES;
		return UltimateWitherSkullRenderer.WITHER_TEXTURES;
	}

	public static class State extends EntityRenderState {
		public boolean invulnerable;
		public float fullTicks;
		public final SkullModelBase.State modelState = new SkullModelBase.State();
	}
}
