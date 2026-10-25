package com.aizistral.enigmaticlegacy.platform;

import java.util.ServiceLoader;

import com.aizistral.enigmaticlegacy.platform.accessory.AccessorySlots;
import com.aizistral.enigmaticlegacy.platform.data.PlayerDataPlatform;
import com.aizistral.enigmaticlegacy.platform.network.NetworkPlatform;

/**
 * Port-only. Loads the loader-specific implementations of the platform services,
 * listed by each loader in {@code META-INF/services}.
 */
public final class Services {
	public static final PlatformHelper PLATFORM = load(PlatformHelper.class);
	public static final AccessorySlots ACCESSORIES = load(AccessorySlots.class);
	public static final NetworkPlatform NETWORK = load(NetworkPlatform.class);
	public static final PlayerDataPlatform PLAYER_DATA = load(PlayerDataPlatform.class);

	private Services() {
		// NO-OP
	}

	public static <T> T load(Class<T> type) {
		return ServiceLoader.load(type, Services.class.getClassLoader()).findFirst()
				.orElseThrow(() -> new IllegalStateException("No implementation of " + type.getName() + " found"));
	}
}
