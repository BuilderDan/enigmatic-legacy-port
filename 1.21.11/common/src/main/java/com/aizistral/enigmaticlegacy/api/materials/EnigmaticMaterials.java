package com.aizistral.enigmaticlegacy.api.materials;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

/**
 * Tools material provider.
 * @author Integral
 *
 * Port: only the materials of ported items are here so far. Upstream registers tiers 5 and 6
 * (Etherium, Creation) with Forge's TierSortingRegistry. 26.3 tool materials are records with
 * no level; the harvest level picks the matching vanilla incorrect_for_*_tool tag, and levels
 * above 4 use netherite's. Repair items are item tags (data/enigmaticlegacy/tags/item/repairs/)
 * instead of upstream's Ingredient suppliers, so Etherium's repair item is no longer a config value.
 */
public final class EnigmaticMaterials {
	public static final TagKey<Item> REPAIRS_FORBIDDEN_AXE = repairTag("forbidden_axe");
	public static final TagKey<Item> REPAIRS_ENDER_SLAYER = repairTag("ender_slayer");
	public static final TagKey<Item> REPAIRS_ELDRITCH_PAN = repairTag("eldritch_pan");
	public static final TagKey<Item> REPAIRS_ETHERIUM = repairTag("etherium");
	public static final TagKey<Item> REPAIRS_INFERNAL_SHIELD = repairTag("infernal_shield");

	public static final ToolMaterial FORBIDDEN_AXE = new ToolMaterial(BlockTags.INCORRECT_FOR_WOODEN_TOOL, 2000, 6.0F, 3.0F, 16, REPAIRS_FORBIDDEN_AXE);
	public static final ToolMaterial ENDER_SLAYER = new ToolMaterial(BlockTags.INCORRECT_FOR_WOODEN_TOOL, 2000, 6.0F, 3.0F, 16, REPAIRS_ENDER_SLAYER);
	public static final ToolMaterial ELDRITCH_PAN = new ToolMaterial(BlockTags.INCORRECT_FOR_WOODEN_TOOL, 4000, 6.0F, 3.0F, 24, REPAIRS_ELDRITCH_PAN);
	public static final ToolMaterial ETHERIUM = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 3000, 8.0F, 5.0F, 32, REPAIRS_ETHERIUM);

	private EnigmaticMaterials() {
		throw new IllegalStateException("Can't touch this");
	}

	/**
	 * Port: tool and sword properties take durability from the material, so items with
	 * upstream's custom durability use a copy of their material.
	 */
	public static ToolMaterial withDurability(ToolMaterial material, int durability) {
		return new ToolMaterial(material.incorrectBlocksForDrops(), durability, material.speed(),
				material.attackDamageBonus(), material.enchantmentValue(), material.repairItems());
	}

	private static TagKey<Item> repairTag(String name) {
		return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, "repairs/" + name));
	}
}
