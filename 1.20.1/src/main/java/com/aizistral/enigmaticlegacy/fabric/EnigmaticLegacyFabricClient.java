package com.aizistral.enigmaticlegacy.fabric;

import net.fabricmc.api.ClientModInitializer;

/**
 * Fabric client entry point (unofficial port, see CHANGES.md).
 */
public class EnigmaticLegacyFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EnigmaticLegacyFabric.LOGGER.info("Enigmatic Legacy (Fabric port) client initializing");
	}
}
