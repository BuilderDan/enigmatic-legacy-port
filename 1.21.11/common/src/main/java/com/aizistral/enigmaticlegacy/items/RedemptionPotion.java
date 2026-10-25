package com.aizistral.enigmaticlegacy.items;

import net.minecraft.client.Minecraft;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBasePotion;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class RedemptionPotion extends ItemBasePotion {

	public RedemptionPotion() {
		super(getDefaultProperties().stacksTo(1).rarity(Rarity.EPIC));
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flagIn) {
		if (Minecraft.getInstance().hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.redemptionPotion1");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.redemptionPotion2");
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}
	}

	@Override
	public void onConsumed(Level worldIn, Player player, ItemStack potion) {
		EnigmaticItems.FORBIDDEN_FRUIT.defineConsumedFruit(player, false);
	}

	@Override
	public boolean canDrink(Level world, Player player, ItemStack potion) {
		return EnigmaticItems.FORBIDDEN_FRUIT.haveConsumedFruit(player);
	}



}
