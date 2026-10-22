package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.brewing.BrewingRecipeRegistry;

import net.minecraft.world.item.ItemStack;

/**
 * Port-only. Lets players put the mod's brewing inputs (such as the Potion of Recall) into
 * the brewing stand's bottle slots, as Forge's BrewingRecipeRegistry.isValidInput does.
 */
@Mixin(targets = "net.minecraft.world.inventory.BrewingStandMenu$PotionSlot")
public abstract class MixinBrewingStandPotionSlot {

	@Inject(method = "mayPlaceItem", at = @At("RETURN"), cancellable = true)
	private static void enigmaticlegacy$mayPlaceItem(ItemStack stack, CallbackInfoReturnable<Boolean> info) {
		if (!info.getReturnValueZ() && BrewingRecipeRegistry.isValidInput(stack)) {
			info.setReturnValue(true);
		}
	}

}
