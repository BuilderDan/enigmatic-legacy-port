package com.aizistral.enigmaticlegacy.brewing;

import java.util.HashMap;

import org.jetbrains.annotations.NotNull;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.helpers.PotionHelper;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;


/**
 * A recipe with one output and any amount of input-ingredient pairs.
 * @author Integral
 */

public class ComplexBrewingRecipe extends AbstractBrewingRecipe {
    @NotNull private final HashMap<BrewingIngredient, BrewingIngredient> processingMappings;
    @NotNull private final ItemStack output;

	public ComplexBrewingRecipe(HashMap<BrewingIngredient, BrewingIngredient> ingredientCompliances, ItemStack output) {
    	this(ingredientCompliances, output, Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, ItemNBTHelper.getString(output, "EnigmaticPotion", "unknown")));
    }

    public ComplexBrewingRecipe(HashMap<BrewingIngredient, BrewingIngredient> ingredientCompliances, ItemStack output, Identifier name) {
        super(name);
    	this.processingMappings = ingredientCompliances;
        this.output = output;
    }

    @Override
    public boolean isInput(@NotNull ItemStack stack) {
        for (BrewingIngredient ing : this.processingMappings.keySet()) {
        	if (this.isInput(stack, ing))
        		return true;
        }

        return false;
    }

    public boolean isInput(@NotNull ItemStack stack, BrewingIngredient ingredient) {
    	for (ItemStack testStack : ingredient.getItems()) {
    		if (testStack.getItem().equals(stack.getItem()) && testStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion().equals(stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion()) && PotionHelper.getAdvancedPotion(testStack).equals(PotionHelper.getAdvancedPotion(stack)))
    			return true;
    	}

    	return false;
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
    	for (BrewingIngredient ing : this.processingMappings.keySet()) {

    		if (this.isInput(input, ing)) {
    			if (this.processingMappings.get(ing).test(ingredient))
    				return this.output.copy();
    		}

    	}

    	return ItemStack.EMPTY;
    }

    @Override
    public boolean isIngredient(ItemStack stack) {
    	if (stack != null)
    	for (BrewingIngredient ing : this.processingMappings.values()) {
    		if (ing.test(stack))
    			return true;
    	}

        return false;
    }

    public HashMap<BrewingIngredient, BrewingIngredient> getProcessingMappings() {
		return this.processingMappings;
	}

	public ItemStack getOutput() {
		return this.output;
	}
}