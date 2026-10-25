package com.aizistral.enigmaticlegacy.handlers;

import net.minecraft.core.HolderLookup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.aizistral.enigmaticlegacy.brewing.AbstractBrewingRecipe;
import com.aizistral.enigmaticlegacy.brewing.ComplexBrewingRecipe;
import com.aizistral.enigmaticlegacy.brewing.SpecialBrewingRecipe;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import com.aizistral.enigmaticlegacy.brewing.BrewingIngredient;
import net.minecraft.world.level.Level;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

public class AdvancedBrewingRecipeProcessor implements IComponentProcessor {
	private AbstractBrewingRecipe recipe;

	public static List<IVariable> wrapStackList(HolderLookup.Provider registries, ItemStack... stackList) {
		List<IVariable> variableList = new ArrayList<IVariable>();

		for (ItemStack catalyst : stackList) {
			variableList.add(IVariable.from(catalyst, registries));
		}

		return variableList;
	}

	public static List<IVariable> wrapIngredientSet(HolderLookup.Provider registries, Iterable<BrewingIngredient> ingredientSet) {
		List<IVariable> variableList = new ArrayList<IVariable>();

		for (BrewingIngredient ingredient : ingredientSet) {
			variableList.addAll(AdvancedBrewingRecipeProcessor.wrapStackList(registries, ingredient.getItems()));
		}

		return variableList;
	}

	@Override
	public void setup(Level level, IVariableProvider variables) {
		Identifier recipeId = Identifier.parse(variables.get("recipe", level.registryAccess()).asString());
		int index = variables.get("index", level.registryAccess()).asNumber().intValue();

		this.recipe = AbstractBrewingRecipe.recipeMap.containsKey(recipeId) ? AbstractBrewingRecipe.recipeMap.get(recipeId).get(index) : AbstractBrewingRecipe.EMPTY_RECIPE;
	}

	@Override
	public IVariable process(Level level, String key) {
		if (this.recipe instanceof SpecialBrewingRecipe) {
			SpecialBrewingRecipe special = (SpecialBrewingRecipe) this.recipe;

			if (key.startsWith("catalyst"))
				return IVariable.wrapList(AdvancedBrewingRecipeProcessor.wrapStackList(level.registryAccess(), special.getIngredient().getItems()), level.registryAccess());
			else if (key.startsWith("input"))
				return IVariable.wrapList(AdvancedBrewingRecipeProcessor.wrapStackList(level.registryAccess(), special.getInput().getItems()), level.registryAccess());
			else if (key.startsWith("output"))
				return IVariable.from(special.getOutput(), level.registryAccess());

		} else if (this.recipe instanceof ComplexBrewingRecipe) {
			ComplexBrewingRecipe complex = (ComplexBrewingRecipe) this.recipe;
			HashMap<BrewingIngredient, BrewingIngredient> processingMappings = complex.getProcessingMappings();

			if (key.startsWith("catalyst"))
				return IVariable.wrapList(AdvancedBrewingRecipeProcessor.wrapIngredientSet(level.registryAccess(), processingMappings.values()), level.registryAccess());
			else if (key.startsWith("input"))
				return IVariable.wrapList(AdvancedBrewingRecipeProcessor.wrapIngredientSet(level.registryAccess(), processingMappings.keySet()), level.registryAccess());
			else if (key.startsWith("output"))
				return IVariable.from(complex.getOutput(), level.registryAccess());

		}
		return null;
	}

}
