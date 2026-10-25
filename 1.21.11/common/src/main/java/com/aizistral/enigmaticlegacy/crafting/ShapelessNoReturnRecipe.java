package com.aizistral.enigmaticlegacy.crafting;

import java.util.List;

import com.aizistral.enigmaticlegacy.registries.EnigmaticRecipes;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

/**
 * A copy of regular Shapeless Recipe, but any container items
 * are destroyed instead of remaining in a crafting grid.
 * @author Integral
 */

public class ShapelessNoReturnRecipe extends ShapelessRecipe {
	/**
	 * Port: upstream registers the inherited vanilla shapeless serializer under this name, so
	 * recipes of this type load as plain shapeless recipes and keep their container items. This
	 * is a separate instance of vanilla's serializer, since one serializer can't be registered twice.
	 */
	public static final RecipeSerializer<ShapelessRecipe> SERIALIZER = new ShapelessRecipe.Serializer();

	public ShapelessNoReturnRecipe(String group, CraftingBookCategory category, ItemStack output, List<Ingredient> inputs) {
		super(group, category, output, inputs);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
		NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.size(), ItemStack.EMPTY);
		return nonnulllist;
	}

	@Override
	public RecipeSerializer<ShapelessRecipe> getSerializer() {
		return EnigmaticRecipes.SHAPELESS_NO_RETURN;
	}

}
