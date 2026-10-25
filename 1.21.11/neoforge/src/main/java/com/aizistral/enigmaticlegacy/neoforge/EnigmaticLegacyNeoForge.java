package com.aizistral.enigmaticlegacy.neoforge;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.EnigmaticSetup;
import com.aizistral.enigmaticlegacy.config.OmniconfigHandler;
import com.aizistral.enigmaticlegacy.config.OmniconfigSync;
import com.aizistral.enigmaticlegacy.dev.DevHooks;
import com.aizistral.enigmaticlegacy.dev.DevPlaytestScenarios;
import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;
import com.aizistral.enigmaticlegacy.handlers.SoulArchive;
import com.aizistral.enigmaticlegacy.items.GolemHeart;
import com.aizistral.enigmaticlegacy.neoforge.platform.CuriosAccessorySlots;
import com.aizistral.enigmaticlegacy.neoforge.platform.NeoForgeEnabledCondition;
import com.aizistral.enigmaticlegacy.neoforge.platform.NeoForgeNetwork;
import com.aizistral.enigmaticlegacy.neoforge.platform.PlaytimeCounterAttachment;
import com.aizistral.enigmaticlegacy.objects.EnabledCondition;
import com.aizistral.enigmaticlegacy.objects.LoggerWrapper;
import com.aizistral.enigmaticlegacy.objects.PersistentData;
import com.aizistral.enigmaticlegacy.objects.RegisteredMeleeAttack;
import com.aizistral.enigmaticlegacy.objects.SpecialLootModifier;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * NeoForge entry point. Enigmatic Legacy is created and owned by Aizistral;
 * this class is part of the unofficial NeoForge port (see CHANGES.md).
 */
@Mod(EnigmaticLegacy.MODID)
public class EnigmaticLegacyNeoForge {
	public static final String MODID = EnigmaticLegacy.MODID;
	public static final LoggerWrapper LOGGER = EnigmaticLegacy.LOGGER;
	private static boolean registered = false;

	public EnigmaticLegacyNeoForge(IEventBus modBus, ModContainer container) {
		LOGGER.info("Enigmatic Legacy (NeoForge port) initializing");
		OmniconfigHandler.initialize();
		// Packets must be collected before RegisterPayloadHandlersEvent freezes the list.
		EnigmaticSetup.registerPackets();
		modBus.addListener(NeoForgeNetwork::onRegisterPayloads);
		modBus.addListener(RegisterEvent.class, EnigmaticLegacyNeoForge::onRegister);
		CuriosAccessorySlots.init();
		// Port: maps loot tables to their ids for SpecialLootModifier (Forge gives them on the context).
		SpecialLootModifier.tableIdLookup = LootTable::getLootTableId;

		if (FMLEnvironment.getDist() == Dist.CLIENT) {
			EnigmaticLegacyNeoForgeClient.init(modBus);
		}

		// Port-only: the multiplayer playtest, where a dedicated server runs the scenarios (see DevPlaytest).
		if (FMLEnvironment.getDist() == Dist.DEDICATED_SERVER && !DevHooks.playtestMode().isEmpty()
				&& !DevHooks.playtestMode().equals("false")) {
			DevPlaytestScenarios.register(DevHooks.playtestMode());
			DevPlaytestScenarios.start();
		}

		registerGameEvents(NeoForge.EVENT_BUS);
	}

	// Port: common code registers through vanilla registries, which are unfrozen during RegisterEvent.
	private static void onRegister(RegisterEvent event) {
		if (!registered) {
			registered = true;
			EnigmaticSetup.registerContent();
			EnigmaticSetup.registerShared();
		}

		event.register(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, PlaytimeCounterAttachment.ID, () -> PlaytimeCounterAttachment.TYPE);
		event.register(NeoForgeRegistries.Keys.CONDITION_CODECS, EnabledCondition.ID, () -> NeoForgeEnabledCondition.CODEC);
	}

	private static void registerGameEvents(IEventBus bus) {
		bus.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
			if (event.getEntity() instanceof ServerPlayer player) {
				OmniconfigSync.onPlayerLoggedIn(player);
				EnigmaticEventHandler.onPlayerJoin(player);
			}
		});
		bus.addListener(LivingIncomingDamageEvent.class, event -> {
			if (!event.getEntity().level().isClientSide()
					&& !EnigmaticEventHandler.onLivingAttack(event.getEntity(), event.getSource(), event.getAmount())) {
				event.setCanceled(true);
			}
		});
		bus.addListener(LivingDeathEvent.class, event -> {
			if (event.getEntity().level().isClientSide())
				return;

			// LivingDeathEvent has no damage amount; onLivingDeath does not use it.
			if (!EnigmaticEventHandler.onLivingDeath(event.getEntity(), event.getSource(), 0F)) {
				event.setCanceled(true);
			}
		});
		// Runs after the cancellable pass above, like Fabric's AFTER_DEATH.
		bus.addListener(net.neoforged.bus.api.EventPriority.LOWEST, LivingDeathEvent.class, event -> {
			if (!event.isCanceled() && !event.getEntity().level().isClientSide()) {
				EnigmaticEventHandler.onEntityKilled(event.getEntity(), event.getSource());
			}
		});
		// NeoForge captures death drops itself, so this replaces the common capture in MixinLivingEntity.
		bus.addListener(LivingDropsEvent.class, event -> {
			LivingEntity entity = event.getEntity();
			int looting = event.getSource().getEntity() instanceof LivingEntity killer ? EnchantmentHelper.getEnchantmentLevel(entity.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING), killer) : 0;
			EnigmaticEventHandler.onLivingDrops(entity, event.getSource(), event.getDrops(), looting, event.isRecentlyHit());
		});
		// Port: upstream EntityJoinLevelEvent (arthropods avoid Acknowledgment holders).
		bus.addListener(EntityJoinLevelEvent.class, event -> {
			if (!event.getLevel().isClientSide()) {
				EnigmaticEventHandler.onEntityLoad(event.getEntity());
			}
		});
		// Forge keeps the PlayerPersisted compound across respawns; copy ours over too.
		bus.addListener(PlayerEvent.Clone.class, event -> {
			if (event.getEntity() instanceof ServerPlayer newPlayer && event.getOriginal() instanceof ServerPlayer oldPlayer) {
				((PersistentData) newPlayer).enigmaticlegacy$setPersistedTag(
						((PersistentData) oldPlayer).enigmaticlegacy$getPersistedTag().copy());
				EnigmaticEventHandler.onPlayerClone(oldPlayer, newPlayer, !event.isWasDeath());
			}
		});
		bus.addListener(PlayerEvent.PlayerRespawnEvent.class, event -> {
			if (event.getEntity() instanceof ServerPlayer player) {
				EnigmaticItems.SOUL_CRYSTAL.updatePlayerSoulMap(player);
				EnigmaticEventHandler.LAST_SOUL_COMPASS_UPDATE.remove(player);
				EnigmaticEventHandler.onPlayerRespawnQuotes(player, event.isEndConquered());
				EnigmaticEventHandler.onPlayerRespawnSlots(player, event.isEndConquered());
			}
		});
		bus.addListener(PlayerEvent.PlayerChangedDimensionEvent.class, event -> {
			if (event.getEntity() instanceof ServerPlayer player) {
				EnigmaticEventHandler.LAST_SOUL_COMPASS_UPDATE.remove(player);
				EnigmaticEventHandler.onPlayerTravelQuotes(player);
			}
		});
		bus.addListener(ServerTickEvent.Post.class, event -> {
			for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
				EnigmaticEventHandler.onPlayerTick(player);
			}
		});
		// Port: upstream builds this in common setup, after every mod has registered its items.
		bus.addListener(ServerStartingEvent.class, event -> {
			GolemHeart.buildArmorExclusions();
			SoulArchive.initialize(event.getServer());
		});
		bus.addListener(ServerStoppedEvent.class, event -> EnigmaticEventHandler.onServerStopped());
		bus.addListener(RegisterCommandsEvent.class, event -> EnigmaticEventHandler.onCommandRegistry(event.getDispatcher()));
		// Port: stands in for upstream's AttackEntityEvent subscriber in RegisteredMeleeAttack.
		bus.addListener(AttackEntityEvent.class, event -> {
			if (!event.getEntity().level().isClientSide()) {
				RegisteredMeleeAttack.registerAttack(event.getEntity());
			}
		});
		bus.addListener(LootTableLoadEvent.class, event ->
				EnigmaticEventHandler.onLootTablesLoaded(event.getKey(), event.getRegistries(), event.getTable()::addPool));
	}
}
