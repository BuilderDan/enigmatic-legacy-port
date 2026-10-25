package com.aizistral.enigmaticlegacy.brewing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;



public abstract class AbstractBrewingRecipe implements IBrewingRecipe {

	public static HashMap<Identifier, List<AbstractBrewingRecipe>> recipeMap = new HashMap<Identifier, List<AbstractBrewingRecipe>>();
	public static final AbstractBrewingRecipe EMPTY_RECIPE = new SpecialBrewingRecipe(BrewingIngredient.of(ItemStack.EMPTY), BrewingIngredient.of(ItemStack.EMPTY), ItemStack.EMPTY, Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, "empty_recipe"));


	public AbstractBrewingRecipe(Identifier registryName) {
		if (AbstractBrewingRecipe.recipeMap.containsKey(registryName)) {
			List<AbstractBrewingRecipe> list = AbstractBrewingRecipe.recipeMap.get(registryName);
			list.add(this);
			AbstractBrewingRecipe.recipeMap.put(registryName, list);
		} else {
			List<AbstractBrewingRecipe> list = new ArrayList<AbstractBrewingRecipe>();
			list.add(this);
			AbstractBrewingRecipe.recipeMap.put(registryName, list);
		}
	}

}
