package com.aizistral.enigmaticlegacy.registries;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Port: since 1.21.2, block and item Properties need their registry id before the block or item
 * is constructed. Upstream constructors build their own Properties without knowing their name, so
 * the registry sets the id here while it constructs an object, and MixinBlockProperties and
 * MixinItemProperties apply it to every Properties created in that window.
 */
public final class PendingRegistryId {
	public static @Nullable ResourceKey<Block> block;
	public static @Nullable ResourceKey<Item> item;
	/** Block items use the block's translation key, like vanilla Items.registerBlock. */
	public static boolean blockItem;

	/** Runs the supplier with the pending id set when the registry is the block or item registry. */
	@SuppressWarnings("unchecked")
	static <T> T construct(Registry<T> registry, Identifier id, boolean isBlockItem, Supplier<? extends T> supplier) {
		boolean isBlock = registry == BuiltInRegistries.BLOCK, isItem = registry == BuiltInRegistries.ITEM;

		if (isBlock) {
			block = ResourceKey.create(Registries.BLOCK, id);
		} else if (isItem) {
			item = ResourceKey.create(Registries.ITEM, id);
			blockItem = isBlockItem;
		}

		try {
			return supplier.get();
		} finally {
			block = null;
			item = null;
			blockItem = false;
		}
	}

	private PendingRegistryId() {
	}
}
