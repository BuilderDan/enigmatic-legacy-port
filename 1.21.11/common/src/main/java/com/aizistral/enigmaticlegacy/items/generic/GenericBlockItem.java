package com.aizistral.enigmaticlegacy.items.generic;

import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import java.util.function.Consumer;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.api.items.ICreativeTabMember;
import com.aizistral.enigmaticlegacy.registries.EnigmaticTabs;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;

public class GenericBlockItem extends BlockItem implements ICreativeTabMember {
	private final Supplier<CreativeModeTab> tab; // supplier cuz weird shit happens otherwise

	public GenericBlockItem(Block blockIn) {
		this(blockIn, GenericBlockItem.getDefaultProperties());
	}

	public GenericBlockItem(Block blockIn, Properties props) {
		this(blockIn, props, () -> EnigmaticTabs.MAIN);
	}

	public GenericBlockItem(Block blockIn, Properties props, Supplier<@Nullable CreativeModeTab> tab) {
		super(blockIn, props);
		this.tab = tab;
	}

	@Override
	public CreativeModeTab getCreativeTab() {
		return this.tab.get();
	}

	// 26.3 port: vanilla tooltips take a Consumer; subclasses keep the old List overload.
	@Override
	@SuppressWarnings("deprecation")
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag flag) {
		List<Component> list = new ArrayList<>();
		this.appendHoverText(stack, context, list, flag);
		list.forEach(adder);
	}

	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		// NO-OP
	}

	public static Properties getDefaultProperties() {
		Properties props = new Item.Properties();

		props.stacksTo(64);
		props.rarity(Rarity.COMMON);

		return props;
	}

}
