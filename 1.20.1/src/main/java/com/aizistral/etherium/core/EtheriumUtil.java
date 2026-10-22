package com.aizistral.etherium.core;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Rarity;

/**
 * Port of upstream's EtheriumUtil. Methods are added as the code that calls them is
 * ported. Port: defaultProperties no longer reads the Etherium config, which upstream
 * fetched but never used.
 */
public class EtheriumUtil {

	public static Properties defaultProperties(Class<?> itemClass) {
		Properties props = new Item.Properties();

		props.stacksTo(1);
		props.rarity(Rarity.RARE);

		return props;
	}

}
