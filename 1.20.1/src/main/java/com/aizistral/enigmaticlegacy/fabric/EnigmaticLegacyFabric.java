package com.aizistral.enigmaticlegacy.fabric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;

/**
 * Fabric entry point. Enigmatic Legacy is created and owned by Aizistral;
 * this class is part of the unofficial Fabric port (see CHANGES.md).
 */
public class EnigmaticLegacyFabric implements ModInitializer {
	public static final String MODID = "enigmaticlegacy";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		LOGGER.info("Enigmatic Legacy (Fabric port) initializing");
	}
}
