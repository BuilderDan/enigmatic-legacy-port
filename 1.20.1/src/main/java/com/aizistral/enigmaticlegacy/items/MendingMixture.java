package com.aizistral.enigmaticlegacy.items;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class MendingMixture extends ItemBase {

	public MendingMixture() {
		super(ItemBase.getDefaultProperties().stacksTo(1).rarity(Rarity.EPIC).craftRemainder(Items.GLASS_BOTTLE));
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
		if (Screen.hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.mendingMixture1");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.mendingMixture2");
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}

}
