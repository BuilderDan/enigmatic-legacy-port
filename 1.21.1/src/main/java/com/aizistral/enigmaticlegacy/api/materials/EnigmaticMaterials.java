package com.aizistral.enigmaticlegacy.api.materials;

import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import java.util.function.Supplier;

import com.aizistral.enigmaticlegacy.config.EtheriumConfigHandler;
import com.aizistral.enigmaticlegacy.items.EldritchPan;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

/**
 * Tools material provider.
 * @author Integral
 *
 * Port: only the tiers of ported items are here so far. Upstream registers tiers 5 and 6
 * (Etherium, Creation) with Forge's TierSortingRegistry. 1.21 tiers have no level; the
 * harvest level picks the matching vanilla incorrect_for_*_tool tag, and levels above 4
 * use netherite's.
 */

public enum EnigmaticMaterials implements Tier {
	FORBIDDEN_AXE(0, 2000, 6.0F, 3.0F, 16, () -> Ingredient.EMPTY),
	ENDER_SLAYER(0, 2000, 6.0F, 3.0F, 16, () -> Ingredient.of(Blocks.OBSIDIAN)),
	ELDRITCH_PAN(0, 4000, 6.0F, 3.0F, 24, EldritchPan::getRepairMaterial),
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
	public TagKey<Block> getIncorrectBlocksForDrops() {
		return switch (this.harvestLevel) {
			case 0 -> BlockTags.INCORRECT_FOR_WOODEN_TOOL;
			case 1 -> BlockTags.INCORRECT_FOR_STONE_TOOL;
			case 2 -> BlockTags.INCORRECT_FOR_IRON_TOOL;
			case 3 -> BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
			default -> BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
		};
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
