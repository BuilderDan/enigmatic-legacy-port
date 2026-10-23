package com.aizistral.enigmaticlegacy.fabric;

import com.aizistral.enigmaticlegacy.objects.LoggerWrapper;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.api.capabilities.PlayerPlaytimeCounter;
import com.aizistral.enigmaticlegacy.config.OmniconfigHandler;
import com.aizistral.enigmaticlegacy.config.OmniconfigSync;
import com.aizistral.enigmaticlegacy.fabric.platform.FabricPlatformHelper;
import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;
import com.aizistral.enigmaticlegacy.handlers.SoulArchive;
import com.aizistral.enigmaticlegacy.fabric.platform.TrinketsAccessorySlots;
import com.aizistral.enigmaticlegacy.objects.EnabledCondition;
import com.aizistral.enigmaticlegacy.objects.PersistentData;
import com.aizistral.enigmaticlegacy.objects.RegisteredMeleeAttack;
import com.aizistral.enigmaticlegacy.packets.clients.PacketForceArrowRotations;
import com.aizistral.enigmaticlegacy.packets.clients.PacketHandleItemPickup;
import com.aizistral.enigmaticlegacy.packets.clients.PacketFlameParticles;
import com.aizistral.enigmaticlegacy.packets.clients.PacketPortalParticles;
import com.aizistral.enigmaticlegacy.packets.clients.PacketGenericParticleEffect;
import com.aizistral.enigmaticlegacy.packets.clients.PacketRecallParticles;
import com.aizistral.enigmaticlegacy.packets.clients.PacketSyncFireImmunity;
import com.aizistral.enigmaticlegacy.packets.clients.PacketUpdateCompass;
import com.aizistral.enigmaticlegacy.packets.clients.PacketSyncForbiddenFruit;
import com.aizistral.enigmaticlegacy.packets.clients.PacketSyncPlayTime;
import com.aizistral.enigmaticlegacy.packets.clients.PacketSyncElytraBoosting;
import com.aizistral.enigmaticlegacy.packets.server.PacketUpdateElytraBoosting;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.minecraft.world.item.ItemStack;
import com.aizistral.enigmaticlegacy.items.GolemHeart;
import com.aizistral.enigmaticlegacy.packets.clients.PacketPlayerMotion;
import com.aizistral.enigmaticlegacy.packets.clients.PacketPlayerSetlook;
import com.aizistral.enigmaticlegacy.packets.clients.PacketCosmicRevive;
import com.aizistral.enigmaticlegacy.packets.server.PacketEnderRingKey;
import com.aizistral.enigmaticlegacy.packets.server.PacketXPScrollKey;
import com.aizistral.enigmaticlegacy.packets.server.PacketInkwellField;
import com.aizistral.enigmaticlegacy.platform.Services;
import com.aizistral.enigmaticlegacy.platform.accessory.AccessoryDrops;
import com.aizistral.enigmaticlegacy.platform.accessory.DropRule;
import com.aizistral.enigmaticlegacy.platform.network.PacketDirection;
import com.aizistral.enigmaticlegacy.registries.EnigmaticAttributes;
import com.aizistral.enigmaticlegacy.registries.EnigmaticEffects;
import com.aizistral.enigmaticlegacy.registries.EnigmaticEnchantments;
import com.aizistral.enigmaticlegacy.registries.EnigmaticPotions;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.enigmaticlegacy.registries.EnigmaticLootFunctions;
import com.aizistral.enigmaticlegacy.registries.EnigmaticRecipes;
import com.aizistral.enigmaticlegacy.registries.EnigmaticSounds;
import com.aizistral.enigmaticlegacy.registries.EnigmaticTabs;
import com.aizistral.enigmaticlegacy.triggers.BeheadingTrigger;
import com.aizistral.enigmaticlegacy.triggers.ForbiddenFruitTrigger;
import com.aizistral.enigmaticlegacy.triggers.RevelationGainTrigger;
import com.aizistral.enigmaticlegacy.triggers.RevelationTomeBurntTrigger;
import com.aizistral.enigmaticlegacy.triggers.CursedRingEquippedTrigger;
import com.aizistral.enigmaticlegacy.triggers.UseUnholyGrailTrigger;
import com.aizistral.omniconfig.packets.PacketSyncOptions;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import com.aizistral.enigmaticlegacy.packets.clients.PacketWitherParticles;
import com.aizistral.enigmaticlegacy.packets.server.PacketSpellstoneKey;
import com.aizistral.enigmaticlegacy.registries.EnigmaticEntities;
import com.aizistral.enigmaticlegacy.registries.EnigmaticMenus;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.registry.FuelRegistry;
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
		EnigmaticEffects.init();
		EnigmaticPotions.init();
		EnigmaticSounds.init();
		EnigmaticAttributes.init();
		EnigmaticEnchantments.init();
		EnigmaticItems.init();
		EnigmaticEntities.init();
		EnigmaticMenus.init();
		EnigmaticLootFunctions.init();
		// Port: upstream RevelationTome.getBurnTime.
		FuelRegistry.INSTANCE.add(EnigmaticItems.TATTERED_TOME, 400);
		FuelRegistry.INSTANCE.add(EnigmaticItems.WITHERED_TOME, 400);
		FuelRegistry.INSTANCE.add(EnigmaticItems.CORRUPTED_TOME, 400);
		// Port: upstream adds this in onLoadComplete.
		EnigmaticItems.SPELLSTONES.add(EnigmaticItems.ANGEL_BLESSING);
		EnigmaticItems.SPELLSTONES.add(EnigmaticItems.GOLEM_HEART);
		EnigmaticItems.SPELLSTONES.add(EnigmaticItems.OCEAN_STONE);
		EnigmaticItems.SPELLSTONES.add(EnigmaticItems.BLAZING_CORE);
		EnigmaticItems.SPELLSTONES.add(EnigmaticItems.EYE_OF_NEBULA);
		EnigmaticItems.SPELLSTONES.add(EnigmaticItems.VOID_PEARL);
		EnigmaticItems.SPELLSTONES.add(EnigmaticItems.THE_CUBE);
		EnigmaticItems.SPELLSTONES.add(EnigmaticItems.ENIGMATIC_ITEM);
		EnigmaticRecipes.init();
		EnigmaticRecipes.registerBrewingRecipes();
		EnigmaticRecipes.registerHiddenRecipes();
		EnigmaticTabs.init();
		TrinketsAccessorySlots.init();
		// Port: upstream onCurioDrops (Curios DropRulesEvent).
		AccessoryDrops.register((entity, stack) -> EnchantmentHelper.getItemEnchantmentLevel(EnigmaticEnchantments.ETERNAL_BINDING, stack) > 0 ? DropRule.ALWAYS_KEEP : null);
		AccessoryDrops.register((entity, stack) -> entity instanceof ServerPlayer player
				&& EnigmaticEventHandler.POSTMORTAL_POSESSIONS.containsEntry(player, EnigmaticItems.ELDRITCH_AMULET)
				&& EnchantmentHelper.getItemEnchantmentLevel(Enchantments.VANISHING_CURSE, stack) <= 0 ? DropRule.ALWAYS_KEEP : null);
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
		EnabledCondition.register();
		PlayerPlaytimeCounter.registerStats();
		registerPackets();
		CriteriaTriggers.register(BeheadingTrigger.INSTANCE);
		CriteriaTriggers.register(UseUnholyGrailTrigger.INSTANCE);
		CriteriaTriggers.register(CursedRingEquippedTrigger.INSTANCE);
		CriteriaTriggers.register(ForbiddenFruitTrigger.INSTANCE);
		CriteriaTriggers.register(RevelationGainTrigger.INSTANCE);
		CriteriaTriggers.register(RevelationTomeBurntTrigger.INSTANCE);

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
			// TODO Port Quote: upstream removes "DestroyedCursedRing" here and plays Quote.RING_DESTRUCTION.
		});
		// Port: upstream onPlayerTravel (PlayerChangedDimensionEvent).
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) ->
				EnigmaticEventHandler.LAST_SOUL_COMPASS_UPDATE.remove(player));
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				EnigmaticEventHandler.onPlayerTick(player);
			}
		});
		// Port: upstream builds this in common setup, after every mod has registered its items.
		ServerLifecycleEvents.SERVER_STARTING.register(server -> GolemHeart.buildArmorExclusions());
		ServerLifecycleEvents.SERVER_STARTING.register(SoulArchive::initialize);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> EnigmaticEventHandler.onServerStopped());
		// Port: stands in for upstream's AttackEntityEvent subscriber in RegisteredMeleeAttack.
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
			if (!world.isClientSide) {
				RegisteredMeleeAttack.registerAttack(player);
			}

			return InteractionResult.PASS;
		});
		LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) ->
				EnigmaticEventHandler.onLootTablesLoaded(id, tableBuilder::pool));
	}

	private static void registerPackets() {
		var network = Services.NETWORK;
		network.register(PacketSyncPlayTime.class, PacketDirection.TO_CLIENT, PacketSyncPlayTime::encode,
				PacketSyncPlayTime::decode, PacketSyncPlayTime::handle);
		network.register(PacketUpdateCompass.class, PacketDirection.TO_CLIENT, PacketUpdateCompass::encode,
				PacketUpdateCompass::decode, PacketUpdateCompass::handle);
		network.register(PacketSyncFireImmunity.class, PacketDirection.TO_CLIENT, PacketSyncFireImmunity::encode,
				PacketSyncFireImmunity::decode, PacketSyncFireImmunity::handle);
		network.register(PacketSyncForbiddenFruit.class, PacketDirection.TO_CLIENT, PacketSyncForbiddenFruit::encode,
				PacketSyncForbiddenFruit::decode, PacketSyncForbiddenFruit::handle);
		network.register(PacketSyncOptions.class, PacketDirection.TO_CLIENT, PacketSyncOptions::encode,
				PacketSyncOptions::decode, PacketSyncOptions::handle);
		network.register(PacketPortalParticles.class, PacketDirection.TO_CLIENT, PacketPortalParticles::encode,
				PacketPortalParticles::decode, PacketPortalParticles::handle);
		network.register(PacketFlameParticles.class, PacketDirection.TO_CLIENT, PacketFlameParticles::encode,
				PacketFlameParticles::decode, PacketFlameParticles::handle);
		network.register(PacketGenericParticleEffect.class, PacketDirection.TO_CLIENT, PacketGenericParticleEffect::encode,
				PacketGenericParticleEffect::decode, PacketGenericParticleEffect::handle);
		network.register(PacketRecallParticles.class, PacketDirection.TO_CLIENT, PacketRecallParticles::encode,
				PacketRecallParticles::decode, PacketRecallParticles::handle);
		network.register(PacketHandleItemPickup.class, PacketDirection.TO_CLIENT, PacketHandleItemPickup::encode,
				PacketHandleItemPickup::decode, PacketHandleItemPickup::handle);
		network.register(PacketEnderRingKey.class, PacketDirection.TO_SERVER, PacketEnderRingKey::encode,
				PacketEnderRingKey::decode, PacketEnderRingKey::handle);
		network.register(PacketXPScrollKey.class, PacketDirection.TO_SERVER, PacketXPScrollKey::encode,
				PacketXPScrollKey::decode, PacketXPScrollKey::handle);
		network.register(PacketInkwellField.class, PacketDirection.TO_SERVER, PacketInkwellField::encode,
				PacketInkwellField::decode, PacketInkwellField::handle);
		network.register(PacketWitherParticles.class, PacketDirection.TO_CLIENT, PacketWitherParticles::encode,
				PacketWitherParticles::decode, PacketWitherParticles::handle);
		network.register(PacketSpellstoneKey.class, PacketDirection.TO_SERVER, PacketSpellstoneKey::encode,
				PacketSpellstoneKey::decode, PacketSpellstoneKey::handle);
		network.register(PacketForceArrowRotations.class, PacketDirection.TO_CLIENT, PacketForceArrowRotations::encode,
				PacketForceArrowRotations::decode, PacketForceArrowRotations::handle);
		network.register(PacketPlayerMotion.class, PacketDirection.TO_CLIENT, PacketPlayerMotion::encode,
				PacketPlayerMotion::decode, PacketPlayerMotion::handle);
		network.register(PacketPlayerSetlook.class, PacketDirection.TO_CLIENT, PacketPlayerSetlook::encode,
				PacketPlayerSetlook::decode, PacketPlayerSetlook::handle);
		network.register(PacketCosmicRevive.class, PacketDirection.TO_CLIENT, PacketCosmicRevive::encode,
				PacketCosmicRevive::decode, PacketCosmicRevive::handle);
		network.register(PacketUpdateElytraBoosting.class, PacketDirection.TO_SERVER, PacketUpdateElytraBoosting::encode,
				PacketUpdateElytraBoosting::decode, PacketUpdateElytraBoosting::handle);
		network.register(PacketSyncElytraBoosting.class, PacketDirection.TO_CLIENT, PacketSyncElytraBoosting::encode,
				PacketSyncElytraBoosting::decode, PacketSyncElytraBoosting::handle);
	}
}
