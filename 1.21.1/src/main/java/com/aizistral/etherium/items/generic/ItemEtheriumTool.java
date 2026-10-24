package com.aizistral.etherium.items.generic;

import java.util.Set;

import com.aizistral.enigmaticlegacy.api.items.ICreativeTabMember;
import com.aizistral.enigmaticlegacy.api.materials.EnigmaticMaterials;
import com.aizistral.enigmaticlegacy.registries.EnigmaticTabs;
import com.aizistral.etherium.core.IEtheriumTool;
import com.google.common.collect.Sets;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port: Forge's stack-aware isCorrectToolForDrops(ItemStack, BlockState) does not exist
 * on Fabric, so the vanilla isCorrectToolForDrops(BlockState) is overridden instead.
 */
public abstract class ItemEtheriumTool extends DiggerItem implements IEtheriumTool, ICreativeTabMember {
	public Set<TagKey<Block>> effectiveTags;

	public ItemEtheriumTool(float attackDamageIn, float attackSpeedIn, TagKey<Block> blocks, Properties builder) {
		// Port: 1.21 tools take their attack attributes from the item properties.
		super(EnigmaticMaterials.ETHERIUM, blocks, builder.attributes(DiggerItem.createAttributes(EnigmaticMaterials.ETHERIUM, attackDamageIn, attackSpeedIn)));

		this.effectiveTags = Sets.newHashSet();
	}

	@Override
	public CreativeModeTab getCreativeTab() {
		return EnigmaticTabs.MAIN;
	}

	@Override
	public boolean isCorrectToolForDrops(ItemStack stack, BlockState blockIn) {
		return super.isCorrectToolForDrops(stack, blockIn) || this.hasAnyTag(blockIn, this.effectiveTags);
	}

	protected boolean hasAnyTag(BlockState state, Set<TagKey<Block>> tags) {
		return tags.stream().anyMatch(tag -> this.hasTag(state, tag));
	}

	protected boolean hasTag(BlockState state, TagKey<Block> tag) {
		return state.is(tag);
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		return !this.hasAnyTag(state, this.effectiveTags) ? super.getDestroySpeed(stack, state) : this.getTier().getSpeed();
	}

}
