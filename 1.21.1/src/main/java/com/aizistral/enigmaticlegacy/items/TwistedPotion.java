package com.aizistral.enigmaticlegacy.items;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBasePotion;
import com.aizistral.enigmaticlegacy.packets.clients.PacketPortalParticles;
import com.aizistral.enigmaticlegacy.packets.clients.PacketRecallParticles;
import com.aizistral.enigmaticlegacy.platform.Services;
import com.aizistral.enigmaticlegacy.platform.network.TargetPoint;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class TwistedPotion extends ItemBasePotion implements ICursed {

	public TwistedPotion() {
		super(getDefaultProperties().stacksTo(1).rarity(Rarity.EPIC));
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flagIn) {
		if (Screen.hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.twistedPotion1");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.twistedPotion2");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.twistedPotion3");
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}

		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
		ItemLoreHelper.indicateCursedOnesOnly(list);
	}

	@Override
	public void onConsumed(Level worldIn, Player playerIn, ItemStack potion) {
		if (playerIn instanceof ServerPlayer player) {
			CompoundTag location = this.getLastDeathLocation(player);

			if (location != null) {
				double x = location.getDouble("x"), y = location.getDouble("y"), z = location.getDouble("z");
				ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(location.getString("dimension")));

				player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, (float) (0.8F + (Math.random() * 0.2)));
				player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1, 1);
				Services.NETWORK.sendToNear(new TargetPoint(player.getX(), player.getY(), player.getZ(), 128, player.level().dimension()), new PacketPortalParticles(player.getX(), player.getY() + (player.getBbHeight() / 2), player.getZ(), 100, 1.25F, false));

				SuperpositionHandler.sendToDimension(player, dimension);
				// Port: upstream's moveTo; on Fabric teleportTo also syncs the new position to the client.
				player.teleportTo(x, y, z);
				player.setHealth(1);

				player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, (float) (0.8F + (Math.random() * 0.2)));
				player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1, 1);
				Services.NETWORK.sendToNear(new TargetPoint(player.getX(), player.getY(), player.getZ(), 128, player.level().dimension()), new PacketRecallParticles(player.getX(), player.getY() + (player.getBbHeight() / 2), player.getZ(), 48, false));
			}
		}
	}

	@Override
	public boolean canDrink(Level world, Player player, ItemStack potion) {
		return SuperpositionHandler.isTheCursedOne(player) && this.getLastDeathLocation(player) != null;
	}

	private CompoundTag getLastDeathLocation(Player player) {
		return (CompoundTag) SuperpositionHandler.getPersistentTag(player, "LastDeathLocation", null);
	}

}
