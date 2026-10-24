package com.aizistral.enigmaticlegacy.packets.clients;
import java.util.function.Supplier;

import com.aizistral.enigmaticlegacy.client.SlotUnlockedToastClient;
import com.aizistral.enigmaticlegacy.platform.network.PacketContext;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Packet for displaying Curio slot unlock notification.
 * @author Integral
 */

public class PacketSlotUnlocked {

	private String type;

	public PacketSlotUnlocked(String type) {
		this.type = type;
	}

	public static void encode(PacketSlotUnlocked msg, FriendlyByteBuf buf) {
		buf.writeUtf(msg.type);
	}

	public static PacketSlotUnlocked decode(FriendlyByteBuf buf) {
		return new PacketSlotUnlocked(buf.readUtf());
	}

	public static void handle(PacketSlotUnlocked msg, Supplier<PacketContext> ctx) {
		// Port: the toast and its queue moved to the client-only SlotUnlockedToastClient.
		ctx.get().enqueueWork(() -> SlotUnlockedToastClient.onSlotUnlocked(msg.type));
		ctx.get().setPacketHandled(true);
	}

}
