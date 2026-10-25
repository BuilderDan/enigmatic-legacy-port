package com.aizistral.enigmaticlegacy.crafting;

import java.util.ArrayList;
import java.util.List;

import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Special recipe type for repairing of damageable items with Mending Mixture.
 * @author Integral
 */

public class MendingMixtureRepairRecipe extends CustomRecipe {
	// Port: these custom recipes have no data, so the serializer always returns one shared instance.
	public static final MendingMixtureRepairRecipe INSTANCE = new MendingMixtureRepairRecipe();
	public static final RecipeSerializer<MendingMixtureRepairRecipe> SERIALIZER = new CustomRecipe.Serializer<>(category -> INSTANCE);

	public MendingMixtureRepairRecipe() {
		super(CraftingBookCategory.MISC);
	}

	@Override
	public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registries) {
		List<ItemStack> stackList = new ArrayList<ItemStack>();

		for (int i = 0; i < inv.size(); i++) {
			ItemStack slotStack = inv.getItem(i);

			if (!slotStack.isEmpty()) {
				stackList.add(slotStack);
			}
		}

		if (stackList.size() == 2)
			if (stackList.get(0).isDamageableItem() || stackList.get(1).isDamageableItem())
				if (stackList.get(0).getItem() == EnigmaticItems.MENDING_MIXTURE || stackList.get(1).getItem() == EnigmaticItems.MENDING_MIXTURE) {
					ItemStack tool = stackList.get(0).isDamageableItem() ? stackList.get(0).copy() : stackList.get(1).copy();

					tool.setDamageValue(0);
					return tool;
				}

		return ItemStack.EMPTY;
	}

	@Override
	public boolean matches(CraftingInput inv, Level world) {
		List<ItemStack> stackList = new ArrayList<ItemStack>();

		for (int i = 0; i < inv.size(); i++) {
			ItemStack slotStack = inv.getItem(i);

			if (!slotStack.isEmpty()) {
				stackList.add(slotStack);
			}
		}

		if (stackList.size() == 2)
			if (stackList.get(0).isDamageableItem() || stackList.get(1).isDamageableItem())
				if (stackList.get(0).getItem() == EnigmaticItems.MENDING_MIXTURE || stackList.get(1).getItem() == EnigmaticItems.MENDING_MIXTURE)
					return true;

		return false;
	}

	@Override
	public RecipeSerializer<MendingMixtureRepairRecipe> getSerializer() {
		return SERIALIZER;
	}
}
