package com.aizistral.enigmaticlegacy.items;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBasePotion;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class RecallPotion extends ItemBasePotion {

	public RecallPotion() {
		super(getDefaultProperties().stacksTo(1).rarity(Rarity.RARE));
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
		if (Screen.hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.recallPotion1");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.recallPotion2");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.recallPotion3");
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}
	}

	@Override
	public void onConsumed(Level worldIn, Player player, ItemStack potion) {
		if (player instanceof ServerPlayer) {
			SuperpositionHandler.backToSpawn((ServerPlayer)player);
		}
	}

	@Override
	public boolean canDrink(Level world, Player player, ItemStack potion) {
		return SuperpositionHandler.isInVanillaDimension(player);
	}

}
