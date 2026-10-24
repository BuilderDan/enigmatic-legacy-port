package com.aizistral.enigmaticlegacy.items;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class EscapeScroll extends ItemBaseCurio {

	public EscapeScroll() {
		super(ItemBaseCurio.getDefaultProperties().rarity(Rarity.RARE).stacksTo(1));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {

		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");

		if (Screen.hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.escapeTome1");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.escapeTome2");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.escapeTome3");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.escapeTome4");
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}
	}

}
