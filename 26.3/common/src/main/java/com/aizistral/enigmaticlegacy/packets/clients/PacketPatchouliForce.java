package com.aizistral.enigmaticlegacy.packets.clients;

import com.aizistral.enigmaticlegacy.helpers.PatchouliState;
import java.util.function.Supplier;

import com.aizistral.enigmaticlegacy.platform.network.PacketContext;

import net.minecraft.network.FriendlyByteBuf;
import vazkii.patchouli.client.base.PersistentData;

/**
 * Sets Patchouli's book GUI scale once, the first time a player joins.
 */

public class PacketPatchouliForce {

	public PacketPatchouliForce() {
		// NO-OP
	}

	public static void encode(PacketPatchouliForce msg, FriendlyByteBuf buf) {
		// NO-OP
	}

	public static PacketPatchouliForce decode(FriendlyByteBuf buf) {
		return new PacketPatchouliForce();
	}

	public static void handle(PacketPatchouliForce msg, Supplier<PacketContext> ctx) {
		ctx.get().enqueueWork(() -> {
			if (PatchouliState.LOADED) {
				PersistentData.data.bookGuiScale = 4;
			}
		});

		ctx.get().setPacketHandled(true);
	}

}
