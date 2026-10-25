package com.aizistral.enigmaticlegacy.packets.server;

import java.util.function.Supplier;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.platform.network.PacketContext;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;

/**
 * Packet for opening Ender Chest inventory to a player.
 * @author Integral
 */

// Port: NetworkEvent.Context -> PacketContext.
public class PacketEnderRingKey {

	private boolean pressed;

	public PacketEnderRingKey(boolean pressed) {
		this.pressed = pressed;
	}

	public static void encode(PacketEnderRingKey msg, FriendlyByteBuf buf) {
		buf.writeBoolean(msg.pressed);
	}

	public static PacketEnderRingKey decode(FriendlyByteBuf buf) {
		return new PacketEnderRingKey(buf.readBoolean());
	}

	public static void handle(PacketEnderRingKey msg, Supplier<PacketContext> ctx) {

		ctx.get().enqueueWork(() -> {
			ServerPlayer playerServ = ctx.get().getSender();

			if (SuperpositionHandler.hasCurio(playerServ, EnigmaticItems.ENDER_RING) || SuperpositionHandler.hasCurio(playerServ, EnigmaticItems.CURSED_RING)) {
				// Port: upstream builds the menu, sends ClientboundOpenScreenPacket and posts Forge's
				// PlayerContainerEvent.Open by hand; vanilla openMenu does the same steps.
				playerServ.openMenu(new SimpleMenuProvider((containerId, inventory, player) ->
				new ChestMenu(MenuType.GENERIC_9x3, containerId, inventory, playerServ.getEnderChestInventory(), 3) {
					@Override
					public void removed(Player playerIn) {
						super.removed(playerIn);

						if (!playerIn.level().isClientSide()) {
							playerIn.level().playSound(null, playerServ.blockPosition(), SoundEvents.ENDER_CHEST_CLOSE, SoundSource.PLAYERS, 1.0F, (float) (0.8F + (Math.random() * 0.2)));
						}
					}
				}, Component.translatable("container.enderchest")));

				playerServ.level().playSound(null, playerServ.blockPosition(), SoundEvents.ENDER_CHEST_OPEN, SoundSource.PLAYERS, 1.0F, (float) (0.8F + (Math.random() * 0.2)));

			}
		});
		ctx.get().setPacketHandled(true);
	}

}
