package com.aizistral.enigmaticlegacy.items;

import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.api.items.IHidden;
import com.aizistral.enigmaticlegacy.client.Quote;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class QuotePlayer extends ItemBase implements IHidden {

	public QuotePlayer() {
		super(getDefaultProperties().rarity(Rarity.EPIC).stacksTo(1).fireResistant());
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flagIn) {
		if (Minecraft.getInstance().hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.quotePlayer1");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.quotePlayer2");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.quotePlayer3");
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}

		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");

		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.quotePlayerSelection", ChatFormatting.GOLD,
				Quote.getAllQuotes().get(stack.getDamageValue()).getName().toUpperCase());
	}

	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);

		if (player.isCrouching()) {
			player.startUsingItem(hand);

			if (itemstack.getDamageValue() < Quote.getAllQuotes().size() - 1) {
				itemstack.setDamageValue(itemstack.getDamageValue() + 1);
			} else {
				itemstack.setDamageValue(0);
			}

			player.swing(hand, SwingAnimation.DEFAULT, false);

			if (player instanceof ServerPlayer) {
				player.sendOverlayMessage(Component.literal("Quote: " + Quote.getAllQuotes().get(itemstack.getDamageValue()).getName().toUpperCase()));
			}
		} else {
			if (player instanceof ServerPlayer splayer) {
				Quote.getAllQuotes().get(itemstack.getDamageValue()).play(splayer, 10);
			}
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		return this.use(context.getLevel(), context.getPlayer(), context.getHand());
	}

}
