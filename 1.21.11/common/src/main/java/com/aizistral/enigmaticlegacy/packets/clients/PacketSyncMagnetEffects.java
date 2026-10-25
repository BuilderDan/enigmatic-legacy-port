package com.aizistral.enigmaticlegacy.packets.clients;

import java.util.function.Supplier;

import com.aizistral.enigmaticlegacy.items.MagnetRing;
import com.aizistral.enigmaticlegacy.platform.network.PacketContext;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Port-only: syncs whether the player turned magnet ring effects off. Upstream syncs it as
 * part of {@code PacketSyncTransientData}, which is not ported.
 */
public class PacketSyncMagnetEffects {
	private boolean disabled;

	public PacketSyncMagnetEffects(boolean disabled) {
		this.disabled = disabled;
	}

	public static void encode(PacketSyncMagnetEffects msg, FriendlyByteBuf buf) {
		buf.writeBoolean(msg.disabled);
	}

	public static PacketSyncMagnetEffects decode(FriendlyByteBuf buf) {
		return new PacketSyncMagnetEffects(buf.readBoolean());
	}

	public static void handle(PacketSyncMagnetEffects msg, Supplier<PacketContext> ctx) {
		ctx.get().enqueueWork(() -> MagnetRing.clientMagnetEffectsDisabled = msg.disabled);
		ctx.get().setPacketHandled(true);
	}
}
