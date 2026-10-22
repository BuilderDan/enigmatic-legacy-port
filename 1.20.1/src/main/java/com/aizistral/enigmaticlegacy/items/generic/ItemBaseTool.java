package com.aizistral.enigmaticlegacy.items.generic;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

/**
 * Port: only the default properties of upstream's ItemBaseTool so far; the base class
 * itself is ported with the first item that extends it.
 */
public final class ItemBaseTool {
	private ItemBaseTool() {}

	public static Item.Properties getDefaultProperties() {
		Item.Properties props = new Item.Properties();

		props.stacksTo(1);
		props.rarity(Rarity.COMMON);

		return props;
	}
}
