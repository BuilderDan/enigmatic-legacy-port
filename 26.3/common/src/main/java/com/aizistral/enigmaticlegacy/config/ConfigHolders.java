package com.aizistral.enigmaticlegacy.config;

import java.util.List;

/**
 * Port-only. Forge finds {@code @SubscribeConfig} methods and {@code @ConfigurableItem}
 * fields through mod file scan data; Fabric has none, so this lists every upstream class
 * that holds them. Classes not ported yet are skipped when they cannot be found.
 */
public final class ConfigHolders {
	public static final List<String> CLASSES = List.of(
			"com.aizistral.enigmaticlegacy.config.EtheriumConfigHandler",
			"com.aizistral.enigmaticlegacy.effects.GrowingBloodlustEffect",
			"com.aizistral.enigmaticlegacy.effects.GrowingHungerEffect",
			"com.aizistral.enigmaticlegacy.effects.MoltenHeartEffect",
			"com.aizistral.enigmaticlegacy.enchantments.CeaselessEnchantment",
			"com.aizistral.enigmaticlegacy.handlers.EnigmaticUpdateHandler",
			"com.aizistral.enigmaticlegacy.items.AngelBlessing",
			"com.aizistral.enigmaticlegacy.items.AstralBreaker",
			"com.aizistral.enigmaticlegacy.items.AvariceScroll",
			"com.aizistral.enigmaticlegacy.items.BerserkEmblem",
			"com.aizistral.enigmaticlegacy.items.BlazingCore",
			"com.aizistral.enigmaticlegacy.items.CosmicScroll",
			"com.aizistral.enigmaticlegacy.items.CursedRing",
			"com.aizistral.enigmaticlegacy.items.CursedScroll",
			"com.aizistral.enigmaticlegacy.items.EldritchPan",
			"com.aizistral.enigmaticlegacy.items.EndAnchor",
			"com.aizistral.enigmaticlegacy.items.EnderRing",
			"com.aizistral.enigmaticlegacy.items.EnderSlayer",
			"com.aizistral.enigmaticlegacy.items.EnigmaticAmulet",
			"com.aizistral.enigmaticlegacy.items.EnigmaticItem",
			"com.aizistral.enigmaticlegacy.items.EyeOfNebula",
			"com.aizistral.enigmaticlegacy.items.ForbiddenAxe",
			"com.aizistral.enigmaticlegacy.items.ForbiddenFruit",
			"com.aizistral.enigmaticlegacy.items.GolemHeart",
			"com.aizistral.enigmaticlegacy.items.GuardianHeart",
			"com.aizistral.enigmaticlegacy.items.HeavenScroll",
			"com.aizistral.enigmaticlegacy.items.HunterGuidebook",
			"com.aizistral.enigmaticlegacy.items.MagnetRing",
			"com.aizistral.enigmaticlegacy.items.MegaSponge",
			"com.aizistral.enigmaticlegacy.items.MiningCharm",
			"com.aizistral.enigmaticlegacy.items.MonsterCharm",
			"com.aizistral.enigmaticlegacy.items.OblivionStone",
			"com.aizistral.enigmaticlegacy.items.OceanStone",
			"com.aizistral.enigmaticlegacy.items.PetGuidebook",
			"com.aizistral.enigmaticlegacy.items.SuperMagnetRing",
			"com.aizistral.enigmaticlegacy.items.TheInfinitum",
			"com.aizistral.enigmaticlegacy.items.TheTwist",
			"com.aizistral.enigmaticlegacy.items.VoidPearl",
			"com.aizistral.enigmaticlegacy.items.XPScroll",
			"com.aizistral.enigmaticlegacy.items.generic.ItemSpellstoneCurio",
			"com.aizistral.enigmaticlegacy.objects.SpecialLootModifier",
			"com.aizistral.enigmaticlegacy.registries.EnigmaticEnchantments",
			"com.aizistral.enigmaticlegacy.registries.EnigmaticItems");

	private ConfigHolders() {
		// NO-OP
	}
}
