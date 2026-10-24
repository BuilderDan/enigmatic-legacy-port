package com.aizistral.enigmaticlegacy.items;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class LoreFragment extends ItemBase {

	public LoreFragment() {
		super(ItemBase.getDefaultProperties().rarity(Rarity.UNCOMMON).stacksTo(16));

		// TODO Lore Fragment copying recipe
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {

		if (!stack.getOrCreateTagElement("display").getString("Name").equals("") || stack.getOrCreateTagElement("display").getList("Lore", 8).size() != 0)
			return;

		if (Screen.hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.loreFragment1");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.loreFragment2");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.loreFragment3");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.loreFragment4");
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}

	}

}
