package com.aizistral.enigmaticlegacy.packets.clients;

import java.util.function.Supplier;

import com.aizistral.enigmaticlegacy.objects.FireImmunityData;
import com.aizistral.enigmaticlegacy.platform.network.PacketContext;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Port-only: syncs the Blazing Core's fire immunity timer to its player. Upstream syncs
 * it as part of {@code PacketSyncTransientData}, which is not ported.
 */
public class PacketSyncFireImmunity {
	private int timer, last;

	public PacketSyncFireImmunity(int timer, int last) {
		this.timer = timer;
		this.last = last;
	}

	public static void encode(PacketSyncFireImmunity msg, FriendlyByteBuf buf) {
		buf.writeVarInt(msg.timer);
		buf.writeVarInt(msg.last);
	}

	public static PacketSyncFireImmunity decode(FriendlyByteBuf buf) {
		return new PacketSyncFireImmunity(buf.readVarInt(), buf.readVarInt());
	}

	public static void handle(PacketSyncFireImmunity msg, Supplier<PacketContext> ctx) {
		ctx.get().enqueueWork(() -> FireImmunityData.setClient(msg.timer, msg.last));
		ctx.get().setPacketHandled(true);
	}
}
