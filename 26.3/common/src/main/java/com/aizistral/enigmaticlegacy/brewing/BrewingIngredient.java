package com.aizistral.enigmaticlegacy.brewing;

import java.util.Arrays;
import java.util.function.Predicate;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Port-only. 26.3's {@code Ingredient} can no longer be built from stacks, but the mod's
 * brewing recipes need the example stacks (potion contents, EnigmaticPotion data) for input
 * matching and the Patchouli pages. Like the 1.20 {@code Ingredient.of(ItemStack...)} this
 * replaces, {@link #test} matches the item only; the recipes compare the rest themselves.
 * Equality is identity, so two equal-looking entries of one recipe map never merge.
 */
public final class BrewingIngredient implements Predicate<ItemStack> {
	private final ItemStack[] items;

	private BrewingIngredient(ItemStack[] items) {
		this.items = items;
	}

	public static BrewingIngredient of(ItemStack... stacks) {
		return new BrewingIngredient(Arrays.stream(stacks).filter(stack -> !stack.isEmpty()).map(ItemStack::copy).toArray(ItemStack[]::new));
	}

	public static BrewingIngredient of(ItemLike... items) {
		return new BrewingIngredient(Arrays.stream(items).map(ItemStack::new).toArray(ItemStack[]::new));
	}

	public ItemStack[] getItems() {
		return this.items;
	}

	@Override
	public boolean test(ItemStack stack) {
		if (stack == null || stack.isEmpty())
			return false;

		for (ItemStack item : this.items) {
			if (item.is(stack.getItem()))
				return true;
		}

		return false;
	}
}
