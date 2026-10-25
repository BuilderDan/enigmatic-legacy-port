package com.aizistral.enigmaticlegacy.client.renderers;

import com.aizistral.enigmaticlegacy.api.items.IPermanentCrystal;
import com.aizistral.enigmaticlegacy.entities.PermanentItemEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;


/**
 * Copy of default ItemRenderer for PermanentItemEntity.
 * @author Integral
 */

// Port: @OnlyIn removed. 26.3 item entities extract an item cluster render state and submit it
// (vanilla ItemEntityRenderer); the crystal scale, hover, spin and dead-player check are unchanged.
public class PermanentItemRenderer extends EntityRenderer<PermanentItemEntity, PermanentItemRenderer.State> {
	private final ItemModelResolver itemModelResolver;
	private final RandomSource random = RandomSource.create();

	public PermanentItemRenderer(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn);
		this.itemModelResolver = renderManagerIn.getItemModelResolver();
		this.shadowRadius = 0.15F;
		this.shadowStrength = 0.75F;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(PermanentItemEntity entityIn, State state, float partialTicks) {
		super.extractRenderState(entityIn, state, partialTicks);
		ItemStack itemstack = entityIn.getItem();
		Player player = Minecraft.getInstance().player;

		state.hidden = player != null && !player.isAlive() && Math.sqrt(entityIn.distanceToSqr(player.getX(), player.getEyeY(), player.getZ())) <= 1.0;
		state.crystal = itemstack.getItem() instanceof IPermanentCrystal;
		state.hover = Mth.sin((entityIn.getAge() + partialTicks) / 10.0F + entityIn.hoverStart) * 0.1F + 0.1F;
		state.spin = entityIn.getItemHover(partialTicks);
		state.extractItemGroupRenderState(entityIn, itemstack, this.itemModelResolver);
	}

	@Override
	public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.hidden || state.item.isEmpty())
			return;

		poseStack.pushPose();

		if (state.crystal) {
			poseStack.scale(1.25f, 1.25f, 1.25f);
			poseStack.translate(0, -0.1125d, 0);
		}

		AABB boundingBox = state.item.getModelBoundingBox();
		float minOffsetY = this.shouldBob() ? -((float) boundingBox.minY) + 0.0625F : 0;
		poseStack.translate(0.0F, state.hover + minOffsetY, 0.0F);
		poseStack.rotate(Axis.YP, state.spin);
		ItemEntityRenderer.submitMultipleFromCount(poseStack, collector, state.lightCoords, state, this.random, boundingBox);
		poseStack.popPose();
		super.submit(state, poseStack, collector, camera);
	}

	/*==================================== FORGE START ===========================================*/

	/**
	 * @return If items should have a bob effect
	 */
	public boolean shouldBob() {
		return true;
	}
	/*==================================== FORGE END =============================================*/

	public static class State extends ItemClusterRenderState {
		public boolean hidden, crystal;
		public float hover, spin;
	}
}
