package com.aizistral.enigmaticlegacy.fabric;

import com.aizistral.enigmaticlegacy.objects.LoggerWrapper;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.api.capabilities.PlayerPlaytimeCounter;
import com.aizistral.enigmaticlegacy.config.OmniconfigHandler;
import com.aizistral.enigmaticlegacy.config.OmniconfigSync;
import com.aizistral.enigmaticlegacy.fabric.platform.FabricPlatformHelper;
import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;
import com.aizistral.enigmaticlegacy.fabric.platform.TrinketsAccessorySlots;
import com.aizistral.enigmaticlegacy.objects.EnabledCondition;
import com.aizistral.enigmaticlegacy.objects.PersistentData;
import com.aizistral.enigmaticlegacy.packets.clients.PacketForceArrowRotations;
import com.aizistral.enigmaticlegacy.packets.clients.PacketHandleItemPickup;
import com.aizistral.enigmaticlegacy.packets.clients.PacketPortalParticles;
import com.aizistral.enigmaticlegacy.packets.clients.PacketRecallParticles;
import com.aizistral.enigmaticlegacy.packets.clients.PacketSyncFireImmunity;
import com.aizistral.enigmaticlegacy.packets.clients.PacketSyncPlayTime;
import com.aizistral.enigmaticlegacy.packets.server.PacketEnderRingKey;
import com.aizistral.enigmaticlegacy.packets.server.PacketXPScrollKey;
import com.aizistral.enigmaticlegacy.platform.Services;
import com.aizistral.enigmaticlegacy.platform.network.PacketDirection;
import com.aizistral.enigmaticlegacy.registries.EnigmaticAttributes;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.enigmaticlegacy.registries.EnigmaticRecipes;
import com.aizistral.enigmaticlegacy.registries.EnigmaticSounds;
import com.aizistral.enigmaticlegacy.registries.EnigmaticTabs;
import com.aizistral.enigmaticlegacy.triggers.BeheadingTrigger;
import com.aizistral.omniconfig.packets.PacketSyncOptions;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import com.aizistral.enigmaticlegacy.packets.clients.PacketWitherParticles;
import com.aizistral.enigmaticlegacy.packets.server.PacketSpellstoneKey;
import com.aizistral.enigmaticlegacy.registries.EnigmaticEntities;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.advancements.CriteriaTriggers;

/**
 * Fabric entry point. Enigmatic Legacy is created and owned by Aizistral;
 * this class is part of the unofficial Fabric port (see CHANGES.md).
 */
public class EnigmaticLegacyFabric implements ModInitializer {
	public static final String MODID = EnigmaticLegacy.MODID;
	public static final LoggerWrapper LOGGER = EnigmaticLegacy.LOGGER;

	@Override
	public void onInitialize() {
		LOGGER.info("Enigmatic Legacy (Fabric port) initializing");
		FabricPlatformHelper.init();
		OmniconfigHandler.initialize();
		EnigmaticSounds.init();
		EnigmaticAttributes.init();
		EnigmaticItems.init();
		EnigmaticEntities.init();
		// Port: upstream adds this in onLoadComplete.
		EnigmaticItems.SPELLSTONES.add(EnigmaticItems.ENIGMATIC_ITEM);
		EnigmaticItems.SPELLSTONES.add(EnigmaticItems.BLAZING_CORE);
		EnigmaticRecipes.init();
		EnigmaticRecipes.registerBrewingRecipes();
		EnigmaticTabs.init();
		TrinketsAccessorySlots.init();
		EnabledCondition.register();
		PlayerPlaytimeCounter.registerStats();
		registerPackets();
		CriteriaTriggers.register(BeheadingTrigger.INSTANCE);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			OmniconfigSync.onPlayerLoggedIn(handler.getPlayer());
			EnigmaticEventHandler.onPlayerJoin(handler.getPlayer());
		});
		ServerLivingEntityEvents.ALLOW_DAMAGE.register(EnigmaticEventHandler::onLivingAttack);
		ServerLivingEntityEvents.ALLOW_DEATH.register(EnigmaticEventHandler::onLivingDeath);
		// Forge keeps the PlayerPersisted compound across respawns; copy ours over too.
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) ->
				((PersistentData) newPlayer).enigmaticlegacy$setPersistedTag(
						((PersistentData) oldPlayer).enigmaticlegacy$getPersistedTag().copy()));
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				EnigmaticEventHandler.onPlayerTick(player);
			}
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> EnigmaticEventHandler.onServerStopped());
		LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) ->
				EnigmaticEventHandler.onLootTablesLoaded(id, tableBuilder::pool));
	}

	private static void registerPackets() {
		var network = Services.NETWORK;
		network.register(PacketSyncPlayTime.class, PacketDirection.TO_CLIENT, PacketSyncPlayTime::encode,
				PacketSyncPlayTime::decode, PacketSyncPlayTime::handle);
		network.register(PacketSyncFireImmunity.class, PacketDirection.TO_CLIENT, PacketSyncFireImmunity::encode,
				PacketSyncFireImmunity::decode, PacketSyncFireImmunity::handle);
		network.register(PacketSyncOptions.class, PacketDirection.TO_CLIENT, PacketSyncOptions::encode,
				PacketSyncOptions::decode, PacketSyncOptions::handle);
		network.register(PacketPortalParticles.class, PacketDirection.TO_CLIENT, PacketPortalParticles::encode,
				PacketPortalParticles::decode, PacketPortalParticles::handle);
		network.register(PacketRecallParticles.class, PacketDirection.TO_CLIENT, PacketRecallParticles::encode,
				PacketRecallParticles::decode, PacketRecallParticles::handle);
		network.register(PacketHandleItemPickup.class, PacketDirection.TO_CLIENT, PacketHandleItemPickup::encode,
				PacketHandleItemPickup::decode, PacketHandleItemPickup::handle);
		network.register(PacketEnderRingKey.class, PacketDirection.TO_SERVER, PacketEnderRingKey::encode,
				PacketEnderRingKey::decode, PacketEnderRingKey::handle);
		network.register(PacketXPScrollKey.class, PacketDirection.TO_SERVER, PacketXPScrollKey::encode,
				PacketXPScrollKey::decode, PacketXPScrollKey::handle);
		network.register(PacketWitherParticles.class, PacketDirection.TO_CLIENT, PacketWitherParticles::encode,
				PacketWitherParticles::decode, PacketWitherParticles::handle);
		network.register(PacketSpellstoneKey.class, PacketDirection.TO_SERVER, PacketSpellstoneKey::encode,
				PacketSpellstoneKey::decode, PacketSpellstoneKey::handle);
		network.register(PacketForceArrowRotations.class, PacketDirection.TO_CLIENT, PacketForceArrowRotations::encode,
				PacketForceArrowRotations::decode, PacketForceArrowRotations::handle);
	}
}
