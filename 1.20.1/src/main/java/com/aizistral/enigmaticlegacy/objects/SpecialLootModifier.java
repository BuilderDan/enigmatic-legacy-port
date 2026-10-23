package com.aizistral.enigmaticlegacy.objects;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.config.OmniconfigHandler;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.omniconfig.Configuration;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.ImmutableList;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * Port: upstream is a Forge global loot modifier (registered through a codec and
 * {@code data/forge/loot_modifiers}). Fabric has no such system, so this is a plain class
 * called from {@code MixinLootTable} after a table rolls its items. Vanilla 1.20.1 loot
 * tables do not know their own id (Forge adds {@code getQueriedLootTableId}), so the ids
 * are kept in an identity map rebuilt on every loot reload.
 */
public class SpecialLootModifier {
	public static final List<ResourceLocation> SUSPICIOUS_TABLES = ImmutableList.of(
			new ResourceLocation("minecraft", "archaeology/desert_pyramid"),
			new ResourceLocation("minecraft", "archaeology/desert_well"),
			new ResourceLocation("minecraft", "archaeology/ocean_ruin_cold"),
			new ResourceLocation("minecraft", "archaeology/ocean_ruin_warm"),
			new ResourceLocation("minecraft", "archaeology/trail_ruins_common"),
			new ResourceLocation("minecraft", "archaeology/trail_ruins_rare")
			);

	// Port: stands in for Forge's LootContext#getQueriedLootTableId.
	private static Map<LootTable, ResourceLocation> tableIds = new IdentityHashMap<>();

	public static Omniconfig.DoubleParameter earthHeartChance;

	@SubscribeConfig(receiveClient = true)
	public static void onConfig(OmniconfigWrapper builder) {
		builder.pushPrefix("CustomLoot");

		if (builder.config.getSidedType() != Configuration.SidedConfigType.CLIENT) {
			earthHeartChance = builder
					.comment("The chance for Heart of the Earth to be obtained from suspicious blocks (default is 3%).")
					.getDouble("EarthHeartChance", 0.03);
		}

		builder.popPrefix();
	}

	/**
	 * Port-only. Called from LootTableEvents.ALL_LOADED.
	 */
	public static void onLootTablesLoaded(LootDataManager manager) {
		Map<LootTable, ResourceLocation> ids = new IdentityHashMap<>();

		for (ResourceLocation id : manager.getKeys(LootDataType.TABLE)) {
			ids.put(manager.getLootTable(id), id);
		}

		tableIds = ids;
	}

	@Nullable
	public static ResourceLocation getTableId(LootTable table) {
		return tableIds.get(table);
	}

	// Port: static, and the table id is passed in instead of read from the context.
	public static ObjectArrayList<ItemStack> doApply(@Nullable ResourceLocation tableId, ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);

		if (tableId != null && entity instanceof ServerPlayer player) {
			if (isVanillaChest(tableId)) {
				if (OmniconfigHandler.isItemEnabled(EnigmaticItems.ENIGMATIC_EYE))
					if (!SuperpositionHandler.hasPersistentTag(player, "LootedArchitectEye")) {
						SuperpositionHandler.setPersistentBoolean(player, "LootedArchitectEye", true);
						generatedLoot.add(new ItemStack(EnigmaticItems.ENIGMATIC_EYE, 1));
					}

				if (SuperpositionHandler.hasPersistentTag(player, "LootedIchorBottle")) {
					generatedLoot.removeIf(stack -> stack.is(EnigmaticItems.ICHOR_BOTTLE));
				} else if (generatedLoot.stream().anyMatch(stack -> stack.is(EnigmaticItems.ICHOR_BOTTLE))) {
					SuperpositionHandler.setPersistentBoolean(player, "LootedIchorBottle", true);
				}
			}

			if (OmniconfigHandler.isItemEnabled(EnigmaticItems.ASTRAL_FRUIT))
				if ("minecraft:chests/end_city_treasure".equals(String.valueOf(tableId))) {
					if (!SuperpositionHandler.hasPersistentTag(player, "LootedFirstEndCityChest")) {
						SuperpositionHandler.setPersistentBoolean(player, "LootedFirstEndCityChest", true);

						if (SuperpositionHandler.isTheCursedOne(player)) {
							generatedLoot.add(new ItemStack(EnigmaticItems.ASTRAL_FRUIT, 1));
						}
					}
				}

			if (OmniconfigHandler.isItemEnabled(EnigmaticItems.EARTH_HEART))
				if (SUSPICIOUS_TABLES.stream().anyMatch(table -> table.equals(tableId))) {
					if (context.getRandom().nextDouble() < earthHeartChance.getValue()) {
						generatedLoot.clear();
						generatedLoot.add(new ItemStack(EnigmaticItems.EARTH_HEART, 1));
					}
				}
		}

		return generatedLoot;
	}

	private static boolean isVanillaChest(ResourceLocation tableId) {
		return String.valueOf(tableId).startsWith("minecraft:chests/");
	}

}
