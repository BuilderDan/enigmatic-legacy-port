package com.aizistral.enigmaticlegacy.enchantments;

import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.config.OmniconfigHandler;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * Upstream enchantment. Port: Forge's canApplyAtEnchantingTable and isAllowedOnBooks overrides
 * are gone (Fabric has no such hooks; vanilla's table checks the CROSSBOW category and
 * isDiscoverable); stack.canApplyAtEnchantingTable(this) becomes super.canEnchant(stack).
 */
public class CeaselessEnchantment extends Enchantment {
	public static Omniconfig.BooleanParameter allowNoArrow;

	@SubscribeConfig
	public static void onConfig(OmniconfigWrapper builder) {
		builder.pushPrefix("CeaselessEnchantment");

		allowNoArrow = builder
				.comment("Whether or not crossbows with Ceaseless should be able to shoot basic arrows even if there are none in player's inventory.")
				.getBoolean("AllowNoArrow", true);

		builder.popPrefix();
	}

	public CeaselessEnchantment(final EquipmentSlot... slots) {
		super(Enchantment.Rarity.RARE, EnchantmentCategory.CROSSBOW, slots);
	}

	@Override
	public int getMinLevel() {
		return 1;
	}

	@Override
	public int getMaxLevel() {
		return 1;
	}

	@Override
	protected boolean checkCompatibility(final Enchantment ench) {
		return super.checkCompatibility(ench);
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
