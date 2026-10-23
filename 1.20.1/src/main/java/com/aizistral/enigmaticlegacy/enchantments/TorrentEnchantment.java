package com.aizistral.enigmaticlegacy.enchantments;

import static com.aizistral.enigmaticlegacy.objects.RegisteredMeleeAttack.getRegisteredAttackStregth;

import com.aizistral.enigmaticlegacy.config.OmniconfigHandler;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.TridentImpalerEnchantment;

/**
 * Upstream enchantment. Port: Forge's canApplyAtEnchantingTable and isAllowedOnBooks overrides
 * are gone (Fabric has no such hooks; vanilla's table checks the TRIDENT category and
 * isDiscoverable); stack.canApplyAtEnchantingTable(this) becomes super.canEnchant(stack).
 */
public class TorrentEnchantment extends Enchantment {
	public TorrentEnchantment(EquipmentSlot... slots) {
		super(Enchantment.Rarity.RARE, EnchantmentCategory.TRIDENT, slots);
	}

	@Override
	public int getMinCost(int enchantmentLevel) {
		return 1 + (enchantmentLevel - 1) * 8;
	}

	@Override
	public int getMaxCost(int enchantmentLevel) {
		return this.getMinCost(enchantmentLevel) + 20;
	}

	@Override
	public int getMaxLevel() {
		return 5;
	}

	@Override
	public boolean checkCompatibility(Enchantment ench) {
		return !(ench instanceof DamageEnchantment) && !(ench instanceof TridentImpalerEnchantment) && !(ench instanceof WrathEnchantment) && super.checkCompatibility(ench);
	}

	@Override
	public boolean isDiscoverable() {
		return OmniconfigHandler.isItemEnabled(this);
	}

	@Override
	public boolean canEnchant(ItemStack stack) {
		return OmniconfigHandler.isItemEnabled(this) && super.canEnchant(stack);
	}

	public float bonusDamageByCreature(LivingEntity attacker, LivingEntity living, int level) {
		float calculated = (living.fireImmune() || living.isSensitiveToWater() || living instanceof EnderDragon) ? level * 2.5F : 0F;
		calculated*= getRegisteredAttackStregth(attacker);

		return calculated;
	}

}
