package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;

/** Port: 26.3 keeps the inventory's recipe book private in AbstractRecipeBookScreen. */
@Mixin(AbstractRecipeBookScreen.class)
public interface AccessorAbstractRecipeBookScreen {
	@Accessor("recipeBookComponent")
	RecipeBookComponent<?> enigmaticlegacy$getRecipeBookComponent();
}
