package com.aizistral.enigmaticlegacy.packets.server;

import java.util.function.Supplier;

import com.aizistral.enigmaticlegacy.items.MagnetRing;
import com.aizistral.enigmaticlegacy.platform.network.PacketContext;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Packet for toggling magnet ring effects, sent by the inventory magnet button.
 * @author Integral
 */

// Port: NetworkEvent.Context -> PacketContext; the flag lives in persistent player NBT
// instead of TransientPlayerData.
public class PacketToggleMagnetEffects {

	public PacketToggleMagnetEffects() {
	}

	public static void encode(PacketToggleMagnetEffects msg, FriendlyByteBuf buf) {
	}

	public static PacketToggleMagnetEffects decode(FriendlyByteBuf buf) {
		return new PacketToggleMagnetEffects();
	}

	public static void handle(PacketToggleMagnetEffects msg, Supplier<PacketContext> ctx) {
		ctx.get().enqueueWork(() -> MagnetRing.toggleMagnetEffects(ctx.get().getSender()));
		ctx.get().setPacketHandled(true);
	}

}
