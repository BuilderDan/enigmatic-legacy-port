package com.aizistral.enigmaticlegacy.registries;

import static com.aizistral.enigmaticlegacy.EnigmaticLegacy.MODID;

import com.aizistral.enigmaticlegacy.api.generic.ConfigurableItem;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Upstream registry. Port: enchantments are data-driven in 1.21 (definitions in
 * data/enigmaticlegacy/enchantment/), so the fields are registry keys instead of Enchantment
 * instances, and nothing is registered from code. The behavior that upstream kept in the
 * enchantment classes now lives in static helpers in the enchantments package.
 */
public class EnigmaticEnchantments {

	@ConfigurableItem("Sharpshooter Enchantment")
	public static final ResourceKey<Enchantment> SHARPSHOOTER = key("sharpshooter");

	@ConfigurableItem("Ceaseless Enchantment")
	public static final ResourceKey<Enchantment> CEASELESS = key("ceaseless");

	@ConfigurableItem("Torrent Enchantment")
	public static final ResourceKey<Enchantment> TORRENT = key("torrent");

	@ConfigurableItem("Wrath Enchantment")
	public static final ResourceKey<Enchantment> WRATH = key("wrath");

	@ConfigurableItem("Slayer Enchantment")
	public static final ResourceKey<Enchantment> SLAYER = key("slayer");

	@ConfigurableItem("Curse of Nemesis")
	public static final ResourceKey<Enchantment> NEMESIS = key("nemesis");

	@ConfigurableItem("Curse of Eternal Binding")
	public static final ResourceKey<Enchantment> ETERNAL_BINDING = key("eternal_binding");

	@ConfigurableItem("Curse of Sorrow")
	public static final ResourceKey<Enchantment> SORROW = key("sorrow");

	private static ResourceKey<Enchantment> key(String name) {
		return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(MODID, name));
	}

	/** Port: level of the given enchantment on the stack, without needing registry access. */
	public static int getLevel(ResourceKey<Enchantment> key, ItemStack stack) {
		for (var entry : stack.getEnchantments().entrySet()) {
			if (entry.getKey().is(key))
				return entry.getIntValue();
		}

		return 0;
	}

	/** Port: the config key of an enchantment holder, or null if it is not one of ours. */
	public static ResourceKey<Enchantment> ownKey(Holder<Enchantment> holder) {
		return holder.unwrapKey().filter(key -> key.identifier().getNamespace().equals(MODID)).orElse(null);
	}

	/** Port: kept so existing init calls still compile; enchantments load from data. */
	public static void init() {
		// NO-OP
	}
}
