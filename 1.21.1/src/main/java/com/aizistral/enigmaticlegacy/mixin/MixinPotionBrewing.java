package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.brewing.BrewingRecipeRegistry;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;

/**
 * Port-only. Forge routes brewing through its {@code BrewingRecipeRegistry}, where vanilla
 * recipes come first and mod recipes after. These hooks do the same: vanilla answers first,
 * and the port's {@link BrewingRecipeRegistry} is asked only when vanilla has no match.
 * <p>
 * Port: PotionBrewing is a per-level instance in 1.21, so these hooks are no longer static.
 */
@Mixin(PotionBrewing.class)
public abstract class MixinPotionBrewing {

	@Inject(method = "isIngredient", at = @At("RETURN"), cancellable = true)
	private void enigmaticlegacy$isIngredient(ItemStack ingredient, CallbackInfoReturnable<Boolean> info) {
		if (!info.getReturnValueZ() && BrewingRecipeRegistry.isValidIngredient(ingredient)) {
			info.setReturnValue(true);
		}
	}

	@Inject(method = "hasMix", at = @At("RETURN"), cancellable = true)
	private void enigmaticlegacy$hasMix(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<Boolean> info) {
		if (!info.getReturnValueZ() && BrewingRecipeRegistry.hasOutput(input, ingredient)) {
			info.setReturnValue(true);
		}
	}

	@Inject(method = "mix", at = @At("HEAD"), cancellable = true)
	private void enigmaticlegacy$mix(ItemStack ingredient, ItemStack potion, CallbackInfoReturnable<ItemStack> info) {
		// Vanilla's own mixes win; the registry only fills in when vanilla would leave the stack as is.
		if (hasContainerMix(potion, ingredient) || hasPotionMix(potion, ingredient))
			return;

		ItemStack output = BrewingRecipeRegistry.getOutput(potion, ingredient);

		if (!output.isEmpty()) {
			info.setReturnValue(output);
		}
	}

	@Shadow
	public abstract boolean hasContainerMix(ItemStack input, ItemStack ingredient);

	@Shadow
	public abstract boolean hasPotionMix(ItemStack input, ItemStack ingredient);

}
