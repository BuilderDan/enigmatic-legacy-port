package com.aizistral.enigmaticlegacy.brewing;

import net.minecraft.world.item.ItemStack;

/**
 * Port-only. Same contract as Forge's {@code IBrewingRecipe}, which upstream's brewing
 * recipes implement. Fabric has no equivalent, so the port keeps its own copy.
 */
public interface IBrewingRecipe {

	/** Whether the stack in a bottle slot can be brewed by this recipe. */
	boolean isInput(ItemStack input);

	/** Whether the stack in the ingredient slot belongs to this recipe. */
	boolean isIngredient(ItemStack ingredient);

	/** The brewed result, or an empty stack if this recipe does not apply. */
	ItemStack getOutput(ItemStack input, ItemStack ingredient);

}
