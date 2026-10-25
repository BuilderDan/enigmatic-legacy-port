package com.aizistral.enigmaticlegacy.config;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;

import net.minecraft.server.level.ServerPlayer;

/**
 * Port: the Omniconfig sync logic from upstream's EnigmaticEventHandler (player login and
 * client logout), moved here so each loader's event hooks can call it.
 */
public class OmniconfigSync {

	/** Upstream: EnigmaticEventHandler#onPlayerLoggedIn. */
	public static void onPlayerLoggedIn(ServerPlayer player) {
		if (!OmniconfigWrapper.syncAllToPlayer(player)) {
			OmniconfigWrapper.onRemoteServer = false;
			EnigmaticLegacy.LOGGER.info("Logging in to local integrated server; no synchronization is required.");
		}
	}

	/** Upstream: client tick handler, when the client player becomes null. */
	public static void onClientLoggedOut() {
		if (OmniconfigWrapper.onRemoteServer) {

			/*
			 * After we log out of remote server, dismiss config values it
			 * sent us and load our own ones from local file.
			 */

			for (OmniconfigWrapper wrapper : OmniconfigWrapper.wrapperRegistry.values()) {

				EnigmaticLegacy.LOGGER.info("Dismissing values of " + wrapper.config.getConfigFile().getName() + " in favor of local config...");

				for (Omniconfig.GenericParameter param : wrapper.retrieveInvocationList()) {
					if (param.isSynchronized()) {
						String oldValue = param.valueToString();
						param.invoke(wrapper.config);

						EnigmaticLegacy.LOGGER.info("Value of '" + param.getId() + "' was restored to '" + param.valueToString() + "'; former server-forced value: " + oldValue);
					}
				}
			}

			OmniconfigWrapper.onRemoteServer = false;
		}
	}
}
