package com.aizistral.enigmaticlegacy.fabric;

import com.aizistral.enigmaticlegacy.config.OmniconfigSync;
import com.aizistral.enigmaticlegacy.fabric.platform.client.FabricClientNetwork;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

/**
 * Fabric client entry point (unofficial port, see CHANGES.md).
 */
public class EnigmaticLegacyFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EnigmaticLegacyFabric.LOGGER.info("Enigmatic Legacy (Fabric port) client initializing");
		FabricClientNetwork.registerReceivers();
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> OmniconfigSync.onClientLoggedOut());
		DevSelfCheck.register();
	}
}
