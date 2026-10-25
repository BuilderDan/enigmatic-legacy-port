package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.brewing.BrewingRecipeRegistry;

import net.minecraft.world.item.ItemStack;

/**
 * Port-only. Lets players put the mod's brewing reagents into the brewing stand's ingredient
 * slot, as Forge's BrewingRecipeRegistry.isValidIngredient does.
 */
@Mixin(targets = "net.minecraft.world.inventory.BrewingStandMenu$IngredientsSlot")
public abstract class MixinBrewingStandIngredientsSlot {

	@Inject(method = "mayPlace", at = @At("RETURN"), cancellable = true)
	private void enigmaticlegacy$mayPlace(ItemStack stack, CallbackInfoReturnable<Boolean> info) {
		if (!info.getReturnValueZ() && BrewingRecipeRegistry.isValidIngredient(stack)) {
			info.setReturnValue(true);
		}
	}

}
