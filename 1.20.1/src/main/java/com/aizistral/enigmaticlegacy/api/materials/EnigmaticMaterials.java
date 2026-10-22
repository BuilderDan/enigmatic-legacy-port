package com.aizistral.enigmaticlegacy.api.materials;

import java.util.function.Supplier;

import com.aizistral.enigmaticlegacy.config.EtheriumConfigHandler;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Tools material provider.
 * @author Integral
 *
 * Port: only the tiers of ported items are here so far. Upstream registers tiers 5 and 6
 * (Etherium, Creation) with Forge's TierSortingRegistry. Vanilla 1.20.1 still compares
 * tiers by getLevel(), so level 5 needs no registration on Fabric.
 */

public enum EnigmaticMaterials implements Tier {
	FORBIDDEN_AXE(0, 2000, 6.0F, 3.0F, 16, () -> Ingredient.EMPTY),
	ETHERIUM(5, 3000, 8.0F, 5.0F, 32, () -> EtheriumConfigHandler.instance().getRepairMaterial());

	private final int harvestLevel;
	private final int maxUses;
	private final float efficiency;
	private final float attackDamage;
	private final int enchantability;
	private final Supplier<Ingredient> repairMaterial;

	private EnigmaticMaterials(int harvestLevelIn, int maxUsesIn, float efficiencyIn, float attackDamageIn, int enchantabilityIn, Supplier<Ingredient> repairMaterialIn) {
		this.harvestLevel = harvestLevelIn;
		this.maxUses = maxUsesIn;
		this.efficiency = efficiencyIn;
		this.attackDamage = attackDamageIn;
		this.enchantability = enchantabilityIn;
		this.repairMaterial = repairMaterialIn;
	}

	@Override
	public int getUses() {
		return this.maxUses;
	}

	@Override
	public float getSpeed() {
		return this.efficiency;
	}

	@Override
	public float getAttackDamageBonus() {
		return this.attackDamage;
	}

	@Override
	public int getLevel() {
		return this.harvestLevel;
	}

	@Override
	public int getEnchantmentValue() {
		return this.enchantability;
	}

	@Override
	public Ingredient getRepairIngredient() {
		return this.repairMaterial.get();
	}
}
