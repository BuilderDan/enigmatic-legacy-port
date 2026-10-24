package com.aizistral.enigmaticlegacy.packets.clients;

import java.util.UUID;
import java.util.function.Supplier;

import com.aizistral.enigmaticlegacy.objects.ElytraBoostData;
import com.aizistral.enigmaticlegacy.platform.network.PacketContext;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Port-only: syncs a player's Majestic Elytra boosting flag to the clients tracking them.
 * Upstream syncs it as part of {@code PacketSyncTransientData}, which is not ported.
 */
public class PacketSyncElytraBoosting {
	private UUID player;
	private boolean boosting;

	public PacketSyncElytraBoosting(UUID player, boolean boosting) {
		this.player = player;
		this.boosting = boosting;
	}

	public static void encode(PacketSyncElytraBoosting msg, FriendlyByteBuf buf) {
		buf.writeUUID(msg.player);
		buf.writeBoolean(msg.boosting);
	}

	public static PacketSyncElytraBoosting decode(FriendlyByteBuf buf) {
		return new PacketSyncElytraBoosting(buf.readUUID(), buf.readBoolean());
	}

	public static void handle(PacketSyncElytraBoosting msg, Supplier<PacketContext> ctx) {
		ctx.get().enqueueWork(() -> ElytraBoostData.setClient(msg.player, msg.boosting));
		ctx.get().setPacketHandled(true);
	}
}
