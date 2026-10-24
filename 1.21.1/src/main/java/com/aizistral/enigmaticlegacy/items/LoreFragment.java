package com.aizistral.enigmaticlegacy.items;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
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
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flagIn) {

		// Port: name and lore are the CUSTOM_NAME and LORE components in 1.21.
		if (stack.has(DataComponents.CUSTOM_NAME) || !stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines().isEmpty())
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
