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
		ClientPlayNetworking.registerGlobalReceiver(packet.id(), (client, listener, buf, sender) -> {
			// The buffer is released after this call, so decode on the network thread.
			M msg = packet.decoder().apply(buf);
			var context = new PacketContext(client, null);
			packet.handler().accept(msg, () -> context);
		});
	}
}
