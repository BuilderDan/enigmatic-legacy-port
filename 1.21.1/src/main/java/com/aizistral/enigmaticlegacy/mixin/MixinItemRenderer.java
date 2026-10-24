package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.aizistral.enigmaticlegacy.fabric.SeparateTransformsModel;

import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Port-only. Stands in for Forge's {@code BakedModel.applyTransform}, which lets a
 * {@code forge:separate_transforms} model show a different model per display context.
 */
@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

	@ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
	private BakedModel enigmaticlegacy$onRender(BakedModel model, ItemStack stack, ItemDisplayContext displayContext) {
		return SeparateTransformsModel.forContext(model, displayContext);
	}

}
