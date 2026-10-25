package com.aizistral.enigmaticlegacy.enchantments;

import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;

/**
 * Upstream enchantment. Port: enchantments are data-driven in 1.21 (definition in
 * data/enigmaticlegacy/enchantment/ceaseless.json), so this class only keeps the config.
 */
public class CeaselessEnchantment {
	public static Omniconfig.BooleanParameter allowNoArrow;

	@SubscribeConfig
	public static void onConfig(OmniconfigWrapper builder) {
		builder.pushPrefix("CeaselessEnchantment");

		allowNoArrow = builder
				.comment("Whether or not crossbows with Ceaseless should be able to shoot basic arrows even if there are none in player's inventory.")
				.getBoolean("AllowNoArrow", true);

		builder.popPrefix();
	}

	private CeaselessEnchantment() {
		// NO-OP
	}
}
