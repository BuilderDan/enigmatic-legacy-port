package com.aizistral.enigmaticlegacy.crafting;

import java.util.ArrayList;
import java.util.List;

import com.aizistral.enigmaticlegacy.items.OblivionStone;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Special recipe type for adding items to list of Keystone of The Oblivion.
 * @author Integral
 */

// Port: ForgeRegistries.ITEMS -> BuiltInRegistries.ITEM.

public class OblivionStoneCombineRecipe extends CustomRecipe {
	// Port: these custom recipes have no data, so the serializer always returns one shared instance.
	public static final OblivionStoneCombineRecipe INSTANCE = new OblivionStoneCombineRecipe();
	public static final RecipeSerializer<OblivionStoneCombineRecipe> SERIALIZER = new CustomRecipe.Serializer<>(category -> INSTANCE);

	public OblivionStoneCombineRecipe() {
		super(CraftingBookCategory.MISC);
	}

	@Override
	public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registries) {
		List<ItemStack> stackList = new ArrayList<ItemStack>();
		ItemStack voidStone = null;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack checkedItemStack = inv.getItem(i);

			if (!checkedItemStack.isEmpty()) {
				if (checkedItemStack.getItem() == EnigmaticItems.VOID_STONE) {
					if (voidStone == null) {
						voidStone = checkedItemStack;
					} else
						return ItemStack.EMPTY;
				} else {
					stackList.add(checkedItemStack);
				}

			}

		}

		if (voidStone != null && stackList.size() == 1) {
			ItemStack savedStack = stackList.get(0).copy();

			CompoundTag nbt = ItemNBTHelper.getNBT(voidStone);

			ListTag arr = nbt.getListOrEmpty("SupersolidID");
			int counter = 0;

			// Port: upstream returns null here; EMPTY avoids a crash if the hardcap is hit.
			if (arr.size() >= OblivionStone.itemHardcap.getValue())
				return ItemStack.EMPTY;

			for (Tag s_uncast : arr) {
				counter++;

				String s = ((StringTag)s_uncast).value();

				if (s.equals(BuiltInRegistries.ITEM.getKey(savedStack.getItem()).toString()))
					return ItemStack.EMPTY;
			}

			ListTag arrCopy = arr.copy();
			CompoundTag nbtCopy = nbt.copy();

			arrCopy.add(StringTag.valueOf(BuiltInRegistries.ITEM.getKey(savedStack.getItem()).toString()));

			nbtCopy.put("SupersolidID", arrCopy);

			ItemStack returnedStack = voidStone.copy();
			ItemNBTHelper.injectNBT(returnedStack, nbtCopy);

			return returnedStack;

		} else if (voidStone != null && stackList.size() == 0) {
			// Port: copies the stone's components (1.20's NBT) instead of only its tag.
			ItemStack returnedStack = voidStone.copyWithCount(1);
			ItemNBTHelper.updateNBT(returnedStack, tag -> tag.remove("SupersolidID"));
			return returnedStack;
		} else
			return ItemStack.EMPTY;
	}

	@Override
	public boolean matches(CraftingInput inv, Level world) {
		List<ItemStack> stackList = new ArrayList<ItemStack>();
		ItemStack voidStone = null;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack checkedItemStack = inv.getItem(i);

			if (!checkedItemStack.isEmpty()) {
				if (checkedItemStack.getItem() == EnigmaticItems.VOID_STONE) {
					if (voidStone == null) {
						voidStone = checkedItemStack;
					} else
						return false;
				} else {
					stackList.add(checkedItemStack);
				}

			}

		}

		if (voidStone != null && stackList.size() == 1) {
			ItemStack savedStack = stackList.get(0).copy();

			CompoundTag nbt = ItemNBTHelper.getNBT(voidStone);

			ListTag arr = nbt.getListOrEmpty("SupersolidID");
			int counter = 0;

			if (arr.size() >= OblivionStone.itemHardcap.getValue())
				return false;

			for (Tag s_uncast : arr) {
				counter++;

				String s = ((StringTag)s_uncast).value();

				if (s.equals(BuiltInRegistries.ITEM.getKey(savedStack.getItem()).toString()))
					return false;
			}

			return true;

		} else if (voidStone != null && stackList.size() == 0)
			return true;
		else
			return false;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
		NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.size(), ItemStack.EMPTY);
		return nonnulllist;
	}

	@Override
	public RecipeSerializer<OblivionStoneCombineRecipe> getSerializer() {
		return SERIALIZER;
	}

}
