package com.aizistral.enigmaticlegacy.brewing;
import net.minecraft.world.item.ItemStack;



/**
 * Stub brewing recipe used to validate some potions as valid inputs
 * for brewing stand, even though they cannot actually be processed
 * or upgraded any further.
 * @author Integral
 */

public class ValidationBrewingRecipe implements IBrewingRecipe {

	    private final BrewingIngredient input;
	    private final BrewingIngredient ingredient;

	    public ValidationBrewingRecipe(BrewingIngredient input, BrewingIngredient ingredient) {
	        this.input = input;
	        this.ingredient = ingredient;
	    }

	    @Override
	    public boolean isInput(ItemStack stack) {

			if (stack != null && this.input != null)
				if (this.input.test(stack))
					return true;


			return false;
	    }

	    @Override
	    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
	        return ItemStack.EMPTY;
	    }

	    public BrewingIngredient getInput() {
	        return this.input;
	    }

	    public BrewingIngredient getIngredient() {
	        return this.ingredient;
	    }

	    public ItemStack getOutput() {
	        return ItemStack.EMPTY;
	    }

	    @Override
	    public boolean isIngredient(ItemStack ingredient) {
	    	if (ingredient != null && this.ingredient != null)
	    		return this.ingredient.test(ingredient);
	    	else
	    		return false;
	    }


}

