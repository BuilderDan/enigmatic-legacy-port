package com.aizistral.enigmaticlegacy.helpers;

import com.aizistral.enigmaticlegacy.registries.EnigmaticEnchantments;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class EnigmaticEnchantmentHelper {

	public static boolean hasCustomCrossbowEnchantments(ItemStack crossbowStack) {
		return (EnigmaticEnchantments.getLevel(EnigmaticEnchantments.CEASELESS, crossbowStack) > 0 || EnigmaticEnchantments.getLevel(EnigmaticEnchantments.SHARPSHOOTER, crossbowStack) > 0);
	}

	public static boolean hasSharpshooterEnchantment(ItemStack stack) {
		return EnigmaticEnchantmentHelper.getSharpshooterLevel(stack) > 0;
	}

	public static boolean hasCeaselessEnchantment(ItemStack stack) {
		return EnigmaticEnchantmentHelper.getCeaselessLevel(stack) > 0;
	}

	public static boolean hasNemesisCurseEnchantment(ItemStack stack) {
		return EnigmaticEnchantmentHelper.getNemesisCurseLevel(stack) > 0;
	}

	public static int getSharpshooterLevel(ItemStack stack) {
		return EnigmaticEnchantments.getLevel(EnigmaticEnchantments.SHARPSHOOTER, stack);
	}

	public static int getCeaselessLevel(ItemStack stack) {
		return EnigmaticEnchantments.getLevel(EnigmaticEnchantments.CEASELESS, stack);
	}

	public static int getNemesisCurseLevel(ItemStack stack) {
		return EnigmaticEnchantments.getLevel(EnigmaticEnchantments.NEMESIS, stack);
	}
}
