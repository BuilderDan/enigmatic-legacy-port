package com.aizistral.enigmaticlegacy.enchantments;

import com.aizistral.enigmaticlegacy.config.OmniconfigHandler;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Upstream enchantment. Port: Forge's canApplyAtEnchantingTable and isAllowedOnBooks overrides
 * are gone (Fabric has no such hooks; vanilla's table checks the CROSSBOW category and
 * isDiscoverable); stack.canApplyAtEnchantingTable(this) becomes super.canEnchant(stack).
 */
public class SharpshooterEnchantment extends Enchantment {
	public SharpshooterEnchantment(EquipmentSlot... slots) {
		super(Enchantment.Rarity.RARE, EnchantmentCategory.CROSSBOW, slots);
	}

	@Override
	public int getMinLevel() {
		return 1;
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

	@Override
	protected boolean checkCompatibility(final Enchantment ench) {
		return ench != Enchantments.MULTISHOT && ench != Enchantments.PIERCING && super.checkCompatibility(ench);
	}

	@Override
	public boolean isTreasureOnly() {
		return false;
	}

	@Override
	public boolean isCurse() {
		return false;
	}

	@Override
	public boolean isDiscoverable() {
		return OmniconfigHandler.isItemEnabled(this);
	}

	@Override
	public boolean canEnchant(ItemStack stack) {
		return OmniconfigHandler.isItemEnabled(this) && super.canEnchant(stack);
	}

}
