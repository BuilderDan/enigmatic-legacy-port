package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.brewing.BrewingRecipeRegistry;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

/**
 * Port-only. Lets hoppers put the mod's brewing inputs into empty bottle slots (0-2),
 * as Forge's patched canPlaceItem does through BrewingRecipeRegistry.isValidInput.
 */
@Mixin(BrewingStandBlockEntity.class)
public abstract class MixinBrewingStandBlockEntity {

	@Shadow
	private NonNullList<ItemStack> items;

	@Inject(method = "canPlaceItem", at = @At("RETURN"), cancellable = true)
	private void enigmaticlegacy$canPlaceItem(int index, ItemStack stack, CallbackInfoReturnable<Boolean> info) {
		if (!info.getReturnValueZ() && index < 3 && this.items.get(index).isEmpty()
				&& BrewingRecipeRegistry.isValidInput(stack)) {
			info.setReturnValue(true);
		}
	}

}
