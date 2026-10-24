package com.aizistral.enigmaticlegacy.packets.clients;

import java.util.function.Supplier;

import com.aizistral.enigmaticlegacy.helpers.PatchouliHelper;
import com.aizistral.enigmaticlegacy.platform.network.PacketContext;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Packet for marking an entry in The Acknowledgment as read or unread.
 * @author Integral
 */

public class PacketSetEntryState {

	private boolean isRead;
	private String entryId;

	public PacketSetEntryState(boolean read, String id) {
		this.isRead = read;
		this.entryId = id;
	}

	public static void encode(PacketSetEntryState msg, FriendlyByteBuf buf) {
		buf.writeBoolean(msg.isRead);
		buf.writeUtf(msg.entryId);
	}

	public static PacketSetEntryState decode(FriendlyByteBuf buf) {
		return new PacketSetEntryState(buf.readBoolean(), buf.readUtf());
	}

	public static void handle(PacketSetEntryState msg, Supplier<PacketContext> ctx) {
		ctx.get().enqueueWork(() -> {
			if (msg.isRead) {
				PatchouliHelper.markEntryRead(ResourceLocation.parse(msg.entryId));
			} else
				PatchouliHelper.markEntryUnread(ResourceLocation.parse(msg.entryId));
		});

		ctx.get().setPacketHandled(true);
	}

}
