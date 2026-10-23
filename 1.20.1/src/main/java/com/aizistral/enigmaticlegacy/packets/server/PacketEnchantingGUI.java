package com.aizistral.enigmaticlegacy.packets.server;

import java.util.function.Supplier;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.platform.network.PacketContext;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Packet for opening Ender Chest inventory to a player.
 * @author Integral
 */

// Port: NetworkEvent.Context -> PacketContext.
public class PacketEnchantingGUI {

	public PacketEnchantingGUI() {
		// NO-OP
	}

	public static void encode(PacketEnchantingGUI msg, FriendlyByteBuf buf) {
		// NO-OP
	}

	public static PacketEnchantingGUI decode(FriendlyByteBuf buf) {
		return new PacketEnchantingGUI();
	}

	public static void handle(PacketEnchantingGUI msg, Supplier<PacketContext> ctx) {
		ctx.get().enqueueWork(() -> {
			SuperpositionHandler.grantAdvancement(ctx.get().getSender(), new ResourceLocation(EnigmaticLegacy.MODID, "book/enchantments/generic"));
		});
		ctx.get().setPacketHandled(true);
	}

}
