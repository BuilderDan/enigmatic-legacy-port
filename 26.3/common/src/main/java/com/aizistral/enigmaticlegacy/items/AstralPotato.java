package com.aizistral.enigmaticlegacy.items;

import net.minecraft.client.Minecraft;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseFood;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class AstralPotato extends ItemBaseFood {

	public AstralPotato() {
		super(getDefaultProperties().rarity(Rarity.EPIC).fireResistant(),
				new FoodProperties.Builder().nutrition(8).saturationModifier(1.4F).build());
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flagIn) {
		if (Minecraft.getInstance().hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.astralPotato1");
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}
	}

	@Override
	public boolean isFoil(ItemStack pStack) {
		return super.isFoil(pStack);
	}

	@Override
	public void onConsumed(Level worldIn, Player player, ItemStack food) {
		if (player instanceof ServerPlayer playerMP) {
			player.level().playSound(null, player.blockPosition(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.5F, 1);
			player.teleportTo(player.getX() + (0.5 - random.nextDouble()) * 4.0, player.getY(), player.getZ() + (0.5 - random.nextDouble()) * 4.0);
		}
	}

}