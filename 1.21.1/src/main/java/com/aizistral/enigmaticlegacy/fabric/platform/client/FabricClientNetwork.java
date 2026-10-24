package com.aizistral.enigmaticlegacy.fabric.platform.client;

import com.aizistral.enigmaticlegacy.fabric.platform.FabricNetwork;
import com.aizistral.enigmaticlegacy.platform.network.PacketContext;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Port-only. Registers receivers for client-bound packets.
 */
public class FabricClientNetwork {

	/** Called from the client entry point, after all packets are registered. */
	public static void registerReceivers() {
		FabricNetwork.clientSender = ClientPlayNetworking::send;
		FabricNetwork.CLIENT_BOUND.forEach(FabricClientNetwork::registerReceiver);
	}

	private static <M> void registerReceiver(FabricNetwork.Packet<M> packet) {
		ClientPlayNetworking.registerGlobalReceiver(packet.type(), (payload, ctx) -> {
			var context = new PacketContext(ctx.client(), null);
			packet.handler().accept(payload.msg(), () -> context);
		});
	}
}
