package com.aizistral.enigmaticlegacy.fabric;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.EnigmaticSetup;
import com.aizistral.enigmaticlegacy.config.OmniconfigHandler;
import com.aizistral.enigmaticlegacy.config.OmniconfigSync;
import com.aizistral.enigmaticlegacy.dev.DevHooks;
import com.aizistral.enigmaticlegacy.dev.DevPlaytestScenarios;
import com.aizistral.enigmaticlegacy.fabric.platform.FabricEnabledCondition;
import com.aizistral.enigmaticlegacy.fabric.platform.FabricPlatformHelper;
import com.aizistral.enigmaticlegacy.fabric.platform.TrinketsAccessorySlots;
import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;
import com.aizistral.enigmaticlegacy.handlers.SoulArchive;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.items.GolemHeart;
import com.aizistral.enigmaticlegacy.objects.LoggerWrapper;
import com.aizistral.enigmaticlegacy.objects.PersistentData;
import com.aizistral.enigmaticlegacy.objects.RegisteredMeleeAttack;
import com.aizistral.enigmaticlegacy.objects.SpecialLootModifier;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.registry.FuelRegistryEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.fabricmc.fabric.api.item.v1.EnchantmentEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

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
		EnigmaticSetup.registerContent();
		TrinketsAccessorySlots.init();
		EnchantmentEvents.ALLOW_ENCHANTING.register((enchantment, stack, context) -> {
			Boolean allowed = EnigmaticSetup.allowEnchanting(enchantment, stack, context == EnchantingContext.PRIMARY);
			return allowed == null ? TriState.DEFAULT : TriState.of(allowed);
		});
		// Port: Forge's IForgeItem canElytraFly/elytraFlightTick (and Caelus' flight attribute).
		EntityElytraEvents.CUSTOM.register((entity, tickElytra) -> {
			ItemStack stack = SuperpositionHandler.getEnigmaticElytra(entity);

			if (stack != null && EnigmaticItems.ENIGMATIC_ELYTRA.canElytraFly(stack, entity)) {
				if (tickElytra) {
					EnigmaticItems.ENIGMATIC_ELYTRA.elytraFlightTick(stack, entity, entity.getFallFlyingTicks());
				}
				return true;
			}

			return false;
		});
		FabricEnabledCondition.register();
		EnigmaticSetup.registerShared();
		EnigmaticSetup.registerPackets();

		// Port-only: the multiplayer playtest, where a dedicated server runs the scenarios (see DevPlaytest).
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER && !DevHooks.playtestMode().isEmpty()
				&& !DevHooks.playtestMode().equals("false")) {
			DevPlaytestScenarios.register(DevHooks.playtestMode());
			DevPlaytestScenarios.start();
		}

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			OmniconfigSync.onPlayerLoggedIn(handler.getPlayer());
			EnigmaticEventHandler.onPlayerJoin(handler.getPlayer());
		});
		ServerLivingEntityEvents.ALLOW_DAMAGE.register(EnigmaticEventHandler::onLivingAttack);
		ServerLivingEntityEvents.ALLOW_DEATH.register(EnigmaticEventHandler::onLivingDeath);
		ServerLivingEntityEvents.AFTER_DEATH.register(EnigmaticEventHandler::onEntityKilled);
		// Port: upstream EntityJoinLevelEvent (arthropods avoid Acknowledgment holders).
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> EnigmaticEventHandler.onEntityLoad(entity));
		// Forge keeps the PlayerPersisted compound across respawns; copy ours over too.
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			((PersistentData) newPlayer).enigmaticlegacy$setPersistedTag(
					((PersistentData) oldPlayer).enigmaticlegacy$getPersistedTag().copy());
			EnigmaticEventHandler.onPlayerClone(oldPlayer, newPlayer, alive);
		});
		// Port: upstream reapplies the lost-soul max health penalty in its PlayerEvent.Clone handler.
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			EnigmaticItems.SOUL_CRYSTAL.updatePlayerSoulMap(newPlayer);
			// Port: upstream onPlayerRespawn (PlayerRespawnEvent).
			EnigmaticEventHandler.LAST_SOUL_COMPASS_UPDATE.remove(newPlayer);
			// Fabric's alive flag is true exactly when upstream's isEndConquered is.
			EnigmaticEventHandler.onPlayerRespawnQuotes(newPlayer, alive);
			EnigmaticEventHandler.onPlayerRespawnSlots(newPlayer, alive);
		});
		// Port: upstream onPlayerTravel (PlayerChangedDimensionEvent).
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
			EnigmaticEventHandler.LAST_SOUL_COMPASS_UPDATE.remove(player);
			EnigmaticEventHandler.onPlayerTravelQuotes(player);
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				EnigmaticEventHandler.onPlayerTick(player);
			}
		});
		// Port: upstream builds this in common setup, after every mod has registered its items.
		ServerLifecycleEvents.SERVER_STARTING.register(server -> GolemHeart.buildArmorExclusions());
		ServerLifecycleEvents.SERVER_STARTING.register(SoulArchive::initialize);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> EnigmaticEventHandler.onServerStopped());
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> EnigmaticEventHandler.onCommandRegistry(dispatcher));
		// Port: stands in for upstream's AttackEntityEvent subscriber in RegisteredMeleeAttack.
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
			if (!world.isClientSide()) {
				RegisteredMeleeAttack.registerAttack(player);
			}

			return InteractionResult.PASS;
		});
		// Port: upstream RevelationTome.getBurnTime; 1.21.11 Fabric registers fuel through this event.
		FuelRegistryEvents.BUILD.register((builder, context) -> {
			builder.add(EnigmaticItems.TATTERED_TOME, 400);
			builder.add(EnigmaticItems.WITHERED_TOME, 400);
			builder.add(EnigmaticItems.CORRUPTED_TOME, 400);
		});
		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) ->
				EnigmaticEventHandler.onLootTablesLoaded(key, registries, tableBuilder::pool));
		// Port: maps loot tables to their ids for SpecialLootModifier (Forge gives them on the context).
		LootTableEvents.ALL_LOADED.register((resourceManager, lootRegistry) ->
				SpecialLootModifier.onLootTablesLoaded(lootRegistry));
	}
}
