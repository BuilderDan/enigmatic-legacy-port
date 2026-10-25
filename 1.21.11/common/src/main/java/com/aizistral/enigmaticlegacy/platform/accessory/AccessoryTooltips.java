package com.aizistral.enigmaticlegacy.platform.accessory;

/**
 * Port addition: shared by the ItemStack tooltip mixin and the loader accessory adapters.
 * Counts nested tooltip passes so accessory attribute lines are only hidden inside them.
 */
public final class AccessoryTooltips {
	public static final ThreadLocal<int[]> TOOLTIP_PASS = ThreadLocal.withInitial(() -> new int[1]);

	private AccessoryTooltips() {}
}
