package com.aizistral.enigmaticlegacy.registries;

import com.aizistral.enigmaticlegacy.api.generic.ObjectHolder;
import com.aizistral.enigmaticlegacy.handlers.LootFunctionRevelation;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public class EnigmaticLootFunctions extends AbstractRegistry<LootItemFunctionType> {
	private static final EnigmaticLootFunctions INSTANCE = new EnigmaticLootFunctions();

	@ObjectHolder(value = MODID + ":revelation", registryName = "loot_function_type")
	public static LootItemFunctionType REVELATION;

	private EnigmaticLootFunctions() {
		super(BuiltInRegistries.LOOT_FUNCTION_TYPE);
		this.register("revelation", () -> new LootItemFunctionType(new LootFunctionRevelation.Serializer()));
	}

	public static void init() {
		// NO-OP
	}

}
