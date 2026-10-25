package com.aizistral.enigmaticlegacy.brewing;

import org.jetbrains.annotations.NotNull;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;


/**
 * Variation of brewing recipe sensitive to NBT of input stack.
 * @author Integral
 */

public class SpecialBrewingRecipe extends AbstractBrewingRecipe {
	@NotNull private final BrewingIngredient input;
	@NotNull private final BrewingIngredient ingredient;
	@NotNull private final ItemStack output;

	public SpecialBrewingRecipe(BrewingIngredient input, BrewingIngredient ingredient, ItemStack output) {
		this(input, ingredient, output, Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, ItemNBTHelper.getString(output, "EnigmaticPotion", "unknown")));
	}

	public SpecialBrewingRecipe(BrewingIngredient input, BrewingIngredient ingredient, ItemStack output, Identifier name) {
		super(name);
		this.input = input;
		this.ingredient = ingredient;
		this.output = output;
	}

	@Override
	public boolean isInput(@NotNull ItemStack stack) {

		if (stack != null) {
			for (ItemStack testStack : this.getInput().getItems()) {
				if (testStack.getItem() == stack.getItem() && testStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion().equals(stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion()))
					return true;
			}
		}

		return false;
	}

	@Override
	public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
		return this.isInput(input) && this.isIngredient(ingredient) ? this.getOutput().copy() : ItemStack.EMPTY;
	}

	public BrewingIngredient getInput() {
		return this.input;
	}

	public BrewingIngredient getIngredient() {
		return this.ingredient;
	}

	public ItemStack getOutput() {
		return this.output;
	}

	@Override
	public boolean isIngredient(ItemStack ingredient) {
		if (ingredient != null)
			return this.ingredient.test(ingredient);
		else
			return false;
	}

}
