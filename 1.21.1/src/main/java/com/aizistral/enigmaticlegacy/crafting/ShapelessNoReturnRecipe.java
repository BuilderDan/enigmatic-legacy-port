package com.aizistral.enigmaticlegacy.crafting;

import com.aizistral.enigmaticlegacy.registries.EnigmaticRecipes;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

/**
 * A copy of regular Shapeless Recipe, but any container items
 * are destroyed instead of remaining in a crafting grid.
 * @author Integral
 */

public class ShapelessNoReturnRecipe extends ShapelessRecipe {

	private final String group;
	private final ItemStack recipeOutput;
	private final NonNullList<Ingredient> recipeItems;

	public ShapelessNoReturnRecipe(String group, CraftingBookCategory category, ItemStack output, NonNullList<Ingredient> inputs) {
		super(group, category, output, inputs);

		this.group = group;
		this.recipeOutput = output;
		this.recipeItems = inputs;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
		NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.size(), ItemStack.EMPTY);
		return nonnulllist;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return EnigmaticRecipes.SHAPELESS_NO_RETURN;
	}

	// Port: upstream's unused Serialize class (registration uses the vanilla shapeless serializer) is dropped;
	// 1.21 serializers are codec-based.
}
