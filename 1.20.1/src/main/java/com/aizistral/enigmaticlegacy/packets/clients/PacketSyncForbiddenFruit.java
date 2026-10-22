package com.aizistral.enigmaticlegacy.packets.clients;

import java.util.function.Supplier;

import com.aizistral.enigmaticlegacy.items.ForbiddenFruit;
import com.aizistral.enigmaticlegacy.platform.network.PacketContext;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Port-only: syncs whether the player has eaten the Forbidden Fruit. Upstream syncs it as
 * part of {@code PacketSyncTransientData}, which is not ported.
 */
public class PacketSyncForbiddenFruit {
	private boolean consumed;

	public PacketSyncForbiddenFruit(boolean consumed) {
		this.consumed = consumed;
	}

	public static void encode(PacketSyncForbiddenFruit msg, FriendlyByteBuf buf) {
		buf.writeBoolean(msg.consumed);
	}

	public static PacketSyncForbiddenFruit decode(FriendlyByteBuf buf) {
		return new PacketSyncForbiddenFruit(buf.readBoolean());
	}

	public static void handle(PacketSyncForbiddenFruit msg, Supplier<PacketContext> ctx) {
		ctx.get().enqueueWork(() -> ForbiddenFruit.clientConsumedFruit = msg.consumed);
		ctx.get().setPacketHandled(true);
	}
}
