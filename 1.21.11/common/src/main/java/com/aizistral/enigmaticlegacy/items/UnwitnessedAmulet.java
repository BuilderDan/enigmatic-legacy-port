package com.aizistral.enigmaticlegacy.items;

import net.minecraft.client.Minecraft;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
// Port: Curios SlotContext -> platform SlotContext (Trinkets on Fabric).
import com.aizistral.enigmaticlegacy.platform.accessory.SlotContext;

public class UnwitnessedAmulet extends ItemBaseCurio {

	public UnwitnessedAmulet() {
		super(ItemBaseCurio.getDefaultProperties().rarity(Rarity.UNCOMMON).fireResistant());
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flagIn) {
		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");

		if (Minecraft.getInstance().hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.unwitnessedAmuletShift1");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.unwitnessedAmuletShift2");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.unwitnessedAmuletShift3");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.unwitnessedAmuletShift4");
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.unwitnessedAmuletHoldShift");
		}
	}

	@Override
	public boolean canEquip(SlotContext context, ItemStack stack) {
		return false;
	}

	// Port: upstream's getAttributesTooltip override (returns the list unchanged) has no Trinkets counterpart; dropped.

}
