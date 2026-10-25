package com.aizistral.enigmaticlegacy.client.renderers;

import java.util.EnumSet;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.aizistral.enigmaticlegacy.blocks.TileEndAnchor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

// Port: @OnlyIn and its Forge imports removed. 26.3 block entity renderers extract a render state
// on the main thread and submit geometry from it; the cube's faces are unchanged.
public class EndAnchorRenderer implements BlockEntityRenderer<TileEndAnchor, EndAnchorRenderer.State> {

	public EndAnchorRenderer(BlockEntityRendererProvider.Context pContext) {
		// NO-OP
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(TileEndAnchor pBlockEntity, State state, float pPartialTick, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(pBlockEntity, state, pPartialTick, cameraPos, breakProgress);
		state.facesToShow.clear();

		if (pBlockEntity.getBlockState().getValue(BlockStateProperties.RESPAWN_ANCHOR_CHARGES) > 0) {
			for (Direction direction : Direction.values())
				if (pBlockEntity.shouldRenderFace(direction)) {
					state.facesToShow.add(direction);
				}
		}
	}

	@Override
	public void submit(State state, PoseStack pPoseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		if (!state.facesToShow.isEmpty()) {
			collector.submitCustomGeometry(pPoseStack, this.renderType(), (pose, consumer) -> this.renderCube(state.facesToShow, pose, consumer));
		}
	}

	private void renderCube(Set<Direction> faces, PoseStack.Pose pPose, VertexConsumer pConsumer) {
		float f = this.getOffsetDown();
		float f1 = this.getOffsetUp();
		this.renderFace(faces, pPose, pConsumer, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, Direction.SOUTH);
		this.renderFace(faces, pPose, pConsumer, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, Direction.NORTH);
		this.renderFace(faces, pPose, pConsumer, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, Direction.EAST);
		this.renderFace(faces, pPose, pConsumer, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, Direction.WEST);
		this.renderFace(faces, pPose, pConsumer, 0.0F, 1.0F, f, f, 0.0F, 0.0F, 1.0F, 1.0F, Direction.DOWN);
		this.renderFace(faces, pPose, pConsumer, 0.1F, 0.9F, f1, f1, 0.9F, 0.9F, 0.1F, 0.1F, Direction.UP);
	}

	private void renderFace(Set<Direction> faces, PoseStack.Pose pPose, VertexConsumer pConsumer, float pX0, float pX1, float pY0, float pY1, float pZ0, float pZ1, float pZ2, float pZ3, Direction pDirection) {
		if (faces.contains(pDirection)) {
			pConsumer.addVertex(pPose, pX0, pY0, pZ0);
			pConsumer.addVertex(pPose, pX1, pY0, pZ1);
			pConsumer.addVertex(pPose, pX1, pY1, pZ2);
			pConsumer.addVertex(pPose, pX0, pY1, pZ3);
		}

	}

	protected float getOffsetUp() {
		return 1.0F - (1F / 16F);
	}

	protected float getOffsetDown() {
		return 0.375F;
	}

	protected RenderType renderType() {
		return RenderTypes.endPortal();
	}

	public static class State extends BlockEntityRenderState {
		public final Set<Direction> facesToShow = EnumSet.noneOf(Direction.class);
	}
}
