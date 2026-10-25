package com.aizistral.enigmaticlegacy.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aizistral.enigmaticlegacy.items.EnchantmentTransposer;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.core.NonNullList;
import com.aizistral.enigmaticlegacy.api.generic.Tuple;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

/**
 * Special recipe type for transposing enchantments.
 * @author Integral
 */

public class EnchantmentTransposingRecipe extends CustomRecipe {
	// Port: these custom recipes have no data, so the serializer always returns one shared instance.
	public static final EnchantmentTransposingRecipe INSTANCE = new EnchantmentTransposingRecipe();
	public static final RecipeSerializer<EnchantmentTransposingRecipe> SERIALIZER = new CustomRecipe.Serializer<>(category -> INSTANCE);

	public EnchantmentTransposingRecipe() {
		super(CraftingBookCategory.MISC);
	}

	@Override
	public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registries) {
		List<ItemStack> stackList = new ArrayList<ItemStack>();
		ItemStack transposer = null;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack checkedItemStack = inv.getItem(i);

			if (!checkedItemStack.isEmpty()) {
				if (checkedItemStack.getItem() instanceof EnchantmentTransposer) {
					if (transposer == null) {
						transposer = checkedItemStack.copy();
					} else
						return ItemStack.EMPTY;
				} else {
					stackList.add(checkedItemStack);
				}
			}
		}

		if (transposer != null && stackList.size() == 1 && stackList.get(0).isEnchanted()
				&& this.canDisenchant(transposer, stackList.get(0)))
			return this.disenchant(transposer, stackList.get(0)).getA();

		return ItemStack.EMPTY;
	}

	@Override
	public boolean matches(CraftingInput inv, Level world) {
		List<ItemStack> stackList = new ArrayList<ItemStack>();
		ItemStack transposer = null;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack checkedItemStack = inv.getItem(i);

			if (!checkedItemStack.isEmpty()) {
				if (checkedItemStack.getItem() instanceof EnchantmentTransposer) {
					if (transposer == null) {
						transposer = checkedItemStack.copy();
					} else
						return false;
				} else {
					stackList.add(checkedItemStack);
				}

			}

		}

		if (transposer != null && stackList.size() == 1 && stackList.get(0).isEnchanted()
				&& this.canDisenchant(transposer, stackList.get(0)))
			return true;

		return false;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
		NonNullList<ItemStack> remaining = NonNullList.withSize(inv.size(), ItemStack.EMPTY);
		Map<ItemStack, Integer> stackList = new HashMap<>();
		ItemStack transposer = null;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack checkedItemStack = inv.getItem(i);

			if (!checkedItemStack.isEmpty()) {
				if (checkedItemStack.getItem() instanceof EnchantmentTransposer) {
					if (transposer == null) {
						transposer = checkedItemStack.copy();
					} else
						return remaining;
				} else {
					stackList.put(checkedItemStack, i);
				}
			}
		}

		if (transposer != null && stackList.size() == 1) {
			ItemStack returned = stackList.keySet().iterator().next();

			if (returned.isEnchanted() && this.canDisenchant(transposer, returned)) {
				remaining.set(stackList.get(returned), this.disenchant(transposer, returned).getB());
			}
		}

		return remaining;
	}

	private Tuple<ItemStack, ItemStack> disenchant(ItemStack transposer, ItemStack target) {
		// Port: enchantments are an ItemEnchantments component in 1.21.
		ItemEnchantments.Mutable transposed = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(target));
		ItemEnchantments.Mutable leftover = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(target));
		transposed.removeIf(enchant -> !((EnchantmentTransposer)transposer.getItem()).canTranspose(enchant));
		leftover.removeIf(enchant -> ((EnchantmentTransposer)transposer.getItem()).canTranspose(enchant));

		ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
		EnchantmentHelper.setEnchantments(book, transposed.toImmutable());

		ItemStack item = target.copy();
		EnchantmentHelper.setEnchantments(item, leftover.toImmutable());

		return new Tuple<>(book, item);
	}

	private boolean canDisenchant(ItemStack transposer, ItemStack target) {
		return EnchantmentHelper.getEnchantmentsForCrafting(target).keySet().stream()
				.anyMatch(((EnchantmentTransposer)transposer.getItem())::canTranspose);
	}

	@Override
	public RecipeSerializer<EnchantmentTransposingRecipe> getSerializer() {
		return SERIALIZER;
	}
}
