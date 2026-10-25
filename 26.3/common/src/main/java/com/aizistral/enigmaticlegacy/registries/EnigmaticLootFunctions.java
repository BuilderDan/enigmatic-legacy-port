package com.aizistral.enigmaticlegacy.registries;

import com.aizistral.enigmaticlegacy.api.generic.ObjectHolder;
import com.aizistral.enigmaticlegacy.handlers.LootFunctionRevelation;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;

/**
 * Port: 26.3 has no LootItemFunctionType; the function's MapCodec is registered directly.
 */
public class EnigmaticLootFunctions extends AbstractRegistry<MapCodec<? extends LootItemFunction>> {
	private static final EnigmaticLootFunctions INSTANCE = new EnigmaticLootFunctions();

	@ObjectHolder(value = MODID + ":revelation", registryName = "loot_function_type")
	public static MapCodec<? extends LootItemFunction> REVELATION;

	private EnigmaticLootFunctions() {
		super(BuiltInRegistries.LOOT_FUNCTION_TYPE);
		this.register("revelation", () -> LootFunctionRevelation.CODEC);
	}

	public static void init() {
		// NO-OP
	}

}
