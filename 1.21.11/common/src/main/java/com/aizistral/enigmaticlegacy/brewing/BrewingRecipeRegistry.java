package com.aizistral.enigmaticlegacy.brewing;

import java.util.ArrayList;
import java.util.List;

import com.aizistral.enigmaticlegacy.registries.EnigmaticRecipes;

import net.minecraft.world.item.ItemStack;

/**
 * Port-only. Stands in for the parts of Forge's {@code BrewingRecipeRegistry} that upstream
 * uses. It holds only the mod's recipes; vanilla brewing stays in the recipe manager, and
 * the brewing mixins ({@code MixinBrewingStandBlockEntity}, {@code MixinBrewingStandPotionSlot},
 * {@code MixinBrewingStandIngredientsSlot}) fall back to this registry when vanilla has no match.
 */
public class BrewingRecipeRegistry {
	private static final List<IBrewingRecipe> RECIPES = new ArrayList<>();

	public static boolean addRecipe(IBrewingRecipe recipe) {
		return RECIPES.add(recipe);
	}

	/** Port: the mod's recipes are added on first use, once 26.3 has bound item components. */
	private static List<IBrewingRecipe> recipes() {
		EnigmaticRecipes.ensureLateRecipes();
		return RECIPES;
	}

	/** Same checks as Forge: one item in the bottle slot and a non-empty ingredient. */
	public static ItemStack getOutput(ItemStack input, ItemStack ingredient) {
		if (input.isEmpty() || input.getCount() != 1 || ingredient.isEmpty())
			return ItemStack.EMPTY;

		for (IBrewingRecipe recipe : recipes()) {
			ItemStack output = recipe.getOutput(input, ingredient);

			if (!output.isEmpty())
				return output;
		}

		return ItemStack.EMPTY;
	}

	public static boolean hasOutput(ItemStack input, ItemStack ingredient) {
		return !getOutput(input, ingredient).isEmpty();
	}

	public static boolean isValidInput(ItemStack stack) {
		for (IBrewingRecipe recipe : recipes()) {
			if (recipe.isInput(stack))
				return true;
		}

		return false;
	}

	public static boolean isValidIngredient(ItemStack stack) {
		if (stack.isEmpty())
			return false;

		for (IBrewingRecipe recipe : recipes()) {
			if (recipe.isIngredient(stack))
				return true;
		}

		return false;
	}

}
