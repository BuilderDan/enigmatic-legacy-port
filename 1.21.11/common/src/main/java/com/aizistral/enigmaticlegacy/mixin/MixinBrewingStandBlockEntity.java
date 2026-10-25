package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.brewing.BrewingRecipeRegistry;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

/**
 * Port-only. 1.21.11 brews from vanilla recipes only. These hooks add the mod's
 * {@link BrewingRecipeRegistry} recipes, as Forge's brewing stand does; vanilla recipes win.
 */
@Mixin(BrewingStandBlockEntity.class)
public abstract class MixinBrewingStandBlockEntity {
	@Shadow private NonNullList<ItemStack> items;

	@Inject(method = "canPlaceItem", at = @At("RETURN"), cancellable = true)
	private void enigmaticlegacy$canPlaceItem(int index, ItemStack stack, CallbackInfoReturnable<Boolean> info) {
		if (info.getReturnValueZ())
			return;

		if (index < 3 && this.items.get(index).isEmpty() && BrewingRecipeRegistry.isValidInput(stack)) {
			info.setReturnValue(true);
		} else if (index == 3 && BrewingRecipeRegistry.isValidIngredient(stack)) {
			info.setReturnValue(true);
		}
	}

	@Inject(method = "isBrewable", at = @At("RETURN"), cancellable = true)
	private static void enigmaticlegacy$isBrewable(PotionBrewing brewing, NonNullList<ItemStack> items, CallbackInfoReturnable<Boolean> info) {
		if (info.getReturnValueZ())
			return;

		ItemStack ingredient = items.get(3);

		if (ingredient.isEmpty())
			return;

		for (int i = 0; i < 3; i++) {
			if (!items.get(i).isEmpty() && BrewingRecipeRegistry.hasOutput(items.get(i), ingredient)) {
				info.setReturnValue(true);
				return;
			}
		}
	}

	// doBrew puts each bottle back unchanged when no vanilla recipe matches it.
	@WrapOperation(method = "doBrew", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;set(ILjava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
	private static Object enigmaticlegacy$doBrew(NonNullList<ItemStack> list, int index, Object value, Operation<Object> original) {
		ItemStack stack = (ItemStack) value;

		if (stack == list.get(index) && !stack.isEmpty()) {
			ItemStack output = BrewingRecipeRegistry.getOutput(stack, list.get(3));

			if (!output.isEmpty())
				return original.call(list, index, output);
		}

		return original.call(list, index, value);
	}
}
