package com.aizistral.enigmaticlegacy.items;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class TwistedMirror extends ItemBase implements ICursed {

	public TwistedMirror() {
		super(ItemBase.getDefaultProperties().rarity(Rarity.RARE).stacksTo(1));
	}

	// Port: upstream's commented-out use-duration variant of this item is not carried over.

	@Override
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flagIn) {
		if (Minecraft.getInstance().hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.twistedMirror1");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.twistedMirror2");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.twistedMirror3");
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}

		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
		ItemLoreHelper.indicateCursedOnesOnly(list);
	}

	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand) {
		// Port: EnigmaticLegacy.PROXY.isInVanillaDimension -> SuperpositionHandler.isInVanillaDimension.
		if (SuperpositionHandler.isInVanillaDimension(player) && SuperpositionHandler.isTheCursedOne(player) && !player.getCooldowns().isOnCooldown(new ItemStack(this))) {
			player.startUsingItem(hand);

			if (player instanceof ServerPlayer) {
				SuperpositionHandler.backToSpawn((ServerPlayer) player);
				player.getCooldowns().addCooldown(new ItemStack(this), 200);
			}

			return InteractionResult.SUCCESS;
		} else
			return InteractionResult.PASS;
	}

}
