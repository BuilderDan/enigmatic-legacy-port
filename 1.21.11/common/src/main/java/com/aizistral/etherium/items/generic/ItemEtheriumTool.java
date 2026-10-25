package com.aizistral.etherium.items.generic;

import com.aizistral.enigmaticlegacy.api.materials.EnigmaticMaterials;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseTool;
import com.aizistral.etherium.core.IEtheriumTool;
import com.google.common.collect.Sets;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;

/**
 * Port: 26.3 has no DiggerItem, so this extends {@link ItemBaseTool} with an empty set of
 * extra effective tags, which keeps upstream's behavior. The material overload carries
 * custom durability (see {@link EnigmaticMaterials#withDurability}).
 */
public abstract class ItemEtheriumTool extends ItemBaseTool implements IEtheriumTool {

	public ItemEtheriumTool(float attackDamageIn, float attackSpeedIn, TagKey<Block> blocks, Properties builder) {
		this(attackDamageIn, attackSpeedIn, blocks, EnigmaticMaterials.ETHERIUM, builder);
	}

	public ItemEtheriumTool(float attackDamageIn, float attackSpeedIn, TagKey<Block> blocks, ToolMaterial material, Properties builder) {
		super(attackDamageIn, attackSpeedIn, material, builder, blocks, Sets.newHashSet());
	}

}
