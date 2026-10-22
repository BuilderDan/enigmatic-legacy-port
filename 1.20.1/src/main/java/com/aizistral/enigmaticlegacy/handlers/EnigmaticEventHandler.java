package com.aizistral.enigmaticlegacy.handlers;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.aizistral.enigmaticlegacy.config.OmniconfigHandler;
import com.aizistral.enigmaticlegacy.entities.PermanentItemEntity;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.items.ForbiddenAxe;
import com.aizistral.enigmaticlegacy.items.generic.ItemSpellstoneCurio;
import com.aizistral.enigmaticlegacy.items.MiningCharm;
import com.aizistral.enigmaticlegacy.items.BlazingCore;
import com.aizistral.enigmaticlegacy.objects.DimensionalPosition;
import com.aizistral.enigmaticlegacy.objects.FireImmunityData;
import com.aizistral.enigmaticlegacy.objects.Perhaps;
import com.aizistral.enigmaticlegacy.packets.clients.PacketPortalParticles;
import com.aizistral.enigmaticlegacy.packets.clients.PacketRecallParticles;
import com.aizistral.enigmaticlegacy.packets.clients.PacketSyncFireImmunity;
import com.aizistral.enigmaticlegacy.packets.clients.PacketWitherParticles;
import com.aizistral.enigmaticlegacy.platform.Services;
import com.aizistral.enigmaticlegacy.platform.network.TargetPoint;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.enigmaticlegacy.triggers.BeheadingTrigger;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

/**
 * Port: upstream's handler is one big Forge event subscriber. Here it holds only the
 * handlers ported so far, as plain static methods; each loader wires them to its own
 * events (Fabric: {@code EnigmaticLegacyFabric}).
 */
public class EnigmaticEventHandler {
	// Port: set while the Charm of Treasure Hunter applies Night Vision; upstream's potion handler
	// skips that case. No ported item reads it yet.
	public static boolean isApplyingNightVision = false;

	public static final Multimap<Player, Item> POSTMORTAL_POSESSIONS = ArrayListMultimap.create();

	/**
	 * Port: was part of the Forge {@code LivingAttackEvent} handler. Fabric calls it from
	 * {@code ServerLivingEntityEvents.ALLOW_DAMAGE}.
	 *
	 * @return false to cancel the attack (upstream: {@code event.setCanceled(true)}).
	 */
	public static boolean onLivingAttack(LivingEntity entity, DamageSource source, float amount) {
		if (source.getDirectEntity() instanceof Player && source.is(DamageTypes.PLAYER_ATTACK)) {
			Player player = (Player) source.getDirectEntity();

			/*
			 * Handler for triggering Extradimensional Eye's effects when player left-clicks
			 * (or, more technically correct, directly attacks) the entity with the Eye in
			 * main hand.
			 */

			if (player.getMainHandItem() != null && player.getMainHandItem().getItem() == EnigmaticItems.EXTRADIMENSIONAL_EYE)
				if (ItemNBTHelper.verifyExistance(player.getMainHandItem(), "BoundDimension"))
					if (ItemNBTHelper.getString(player.getMainHandItem(), "BoundDimension", "minecraft:overworld").equals(entity.level().dimension().location().toString())) {
						ItemStack stack = player.getMainHandItem();

						Services.NETWORK.sendToNear(new TargetPoint(entity.getX(), entity.getY(), entity.getZ(), 128, entity.level().dimension()), new PacketPortalParticles(entity.getX(), entity.getY() + (entity.getBbHeight() / 2), entity.getZ(), 96, 1.5D, false));

						entity.level().playSound(null, entity.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, (float) (0.8F + (Math.random() * 0.2)));
						entity.teleportTo(ItemNBTHelper.getDouble(stack, "BoundX", 0D), ItemNBTHelper.getDouble(stack, "BoundY", 0D), ItemNBTHelper.getDouble(stack, "BoundZ", 0D));
						entity.level().playSound(null, entity.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, (float) (0.8F + (Math.random() * 0.2)));

						Services.NETWORK.sendToNear(new TargetPoint(entity.getX(), entity.getY(), entity.getZ(), 128, entity.level().dimension()), new PacketRecallParticles(entity.getX(), entity.getY() + (entity.getBbHeight() / 2), entity.getZ(), 48, false));

						if (!player.getAbilities().instabuild) {
							stack.shrink(1);
						}

						return false;
					}
		}

		if (entity instanceof Player player) {
			List<ItemStack> advancedCurios = SuperpositionHandler.getAdvancedCurios(player);
			if (advancedCurios.size() > 0) {
				for (ItemStack advancedCurioStack : advancedCurios) {
					ItemSpellstoneCurio advancedCurio = (ItemSpellstoneCurio) advancedCurioStack.getItem();

					if (advancedCurio.immunityList.stream().anyMatch(source::is))
						return false;
				}
			}

			// Port: upstream reads the timer from TransientPlayerData; see FireImmunityData.
			if (!player.getAbilities().invulnerable && player.getEffect(MobEffects.FIRE_RESISTANCE) == null)
				if (SuperpositionHandler.hasCurio(player, EnigmaticItems.BLAZING_CORE) && source.is(DamageTypes.LAVA)) {
					FireImmunityData data = FireImmunityData.get(player);
					int timer = data.getTimer();

					if (timer < FireImmunityData.TIMER_CAP) {
						boolean cancel = timer < FireImmunityData.TIMER_CAP - FireImmunityData.FIRE_DIFF;

						if (timer == 0 && !player.level().isClientSide) {
							player.level().playSound(null, player.blockPosition(), SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 0.5F + (player.getRandom().nextFloat() * 0.5F));
							Services.NETWORK.sendToNear(new TargetPoint(player.getX(), player.getY(), player.getZ(), 32, player.level().dimension()), new PacketWitherParticles(player.getX(), player.getY(0.25D), player.getZ(), 8, false, 1));
						}

						data.setTimer(timer + 200);

						if (cancel)
							return false;
					}
				}
		}

		return true;
	}

	/**
	 * Port: the player-target part of upstream's {@code LivingHurtEvent} handler. Fabric calls
	 * it from {@code MixinPlayer} at the head of {@code Player.actuallyHurt}, where Forge fires
	 * the event. Only the Blazing Core's terms are ported; the Golem Heart, Eye of Nebula and
	 * Ocean Stone terms follow with those items.
	 *
	 * @return the new damage amount.
	 */
	public static float onPlayerHurt(Player player, DamageSource source, float amount) {
		if (player.isInvulnerableTo(source))
			return amount;

		List<ItemStack> advancedCurios = SuperpositionHandler.getAdvancedCurios(player);

		for (ItemStack advancedCurioStack : advancedCurios) {
			ItemSpellstoneCurio advancedCurio = (ItemSpellstoneCurio) advancedCurioStack.getItem();
			Mob trueSource = source.getEntity() instanceof Mob mob ? mob : null;

			if (advancedCurio == EnigmaticItems.BLAZING_CORE && trueSource != null)
				if (trueSource.getMobType() == MobType.WATER || trueSource instanceof Drowned) {
					amount *= 2F;
				}

			for (var type : advancedCurio.resistanceList.keySet()) {
				if (source.is(type)) {
					amount *= advancedCurio.resistanceList.get(type).get();
					break;
				}
			}
		}

		if (!player.level().isClientSide && SuperpositionHandler.hasCurio(player, EnigmaticItems.BLAZING_CORE)) {
			BlazingCore core = (BlazingCore) EnigmaticItems.BLAZING_CORE;

			if (source.getEntity() instanceof LivingEntity attacker && core.nemesisList.stream().anyMatch(source::is)) {
				if (!attacker.fireImmune()) {
					attacker.hurt(attacker.damageSources().source(DamageTypes.ON_FIRE, player), (float) BlazingCore.damageFeedback.getValue());
					attacker.setSecondsOnFire(BlazingCore.ignitionFeedback.getValue());
				}
			}
		}

		return amount;
	}

	/**
	 * Port: the Blazing Core part of upstream's {@code LivingTickEvent} handler: a player wearing
	 * it ticks its potion effects twice as fast. Called on both sides (server from
	 * {@link #onPlayerTick}, client from the Fabric client tick).
	 */
	public static void tickBlazingCoreEffects(Player player) {
		if (SuperpositionHandler.hasCurio(player, EnigmaticItems.BLAZING_CORE)) {
			List<MobEffectInstance> effects = new java.util.ArrayList<>(player.getActiveEffects());

			for (MobEffectInstance effect : effects) {
				effect.tick(player, () -> {});
			}
		}
	}

	/**
	 * Port: was the Forge {@code MobEffectEvent.Applicable} handler. Fabric calls it from
	 * {@code MixinLivingEntity} at the head of {@code canBeAffected}, where Forge fires the event.
	 * The Pearl of the Void and Cube terms follow with those items.
	 *
	 * @return false to deny the effect (upstream: {@code event.setResult(Result.DENY)}).
	 */
	public static boolean onPotionApplicable(LivingEntity entity, MobEffectInstance effect) {
		if (entity instanceof Player player) {
			if (isApplyingNightVision)
				return true;

			if (new ResourceLocation("mana-and-artifice", "chrono-exhaustion").equals(BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect())))
				return true;

			if (SuperpositionHandler.hasCurio(player, EnigmaticItems.ENIGMATIC_ITEM) && !effect.getEffect().isBeneficial())
				return false;
		}

		return true;
	}

	/**
	 * Port: was the Heart of Creation part of the Forge {@code LivingDeathEvent} handler. Fabric
	 * calls it from {@code ServerLivingEntityEvents.ALLOW_DEATH}. The Nemesis Curse term follows
	 * with that item.
	 *
	 * @return false to cancel the death (upstream: {@code event.setCanceled(true)}).
	 */
	public static boolean onLivingDeath(LivingEntity entity, DamageSource source, float amount) {
		if (entity instanceof Player player) {
			if (SuperpositionHandler.hasCurio(player, EnigmaticItems.ENIGMATIC_ITEM) || player.getInventory().contains(new ItemStack(EnigmaticItems.ENIGMATIC_ITEM))) {
				player.setHealth(1);
				return false;
			}
		}

		return true;
	}

	/**
	 * Port: was the Heart of Creation part of the Forge {@code PlayerTickEvent} handler (server
	 * side). Fabric calls it for each player from {@code ServerTickEvents.END_SERVER_TICK}.
	 */
	public static void onPlayerTick(ServerPlayer player) {
		if (SuperpositionHandler.hasSpellstoneCooldown(player)) {
			SuperpositionHandler.tickSpellstoneCooldown(player, 1);
		}

		EnigmaticItems.ENIGMATIC_ITEM.handleEnigmaticFlight(player);

		FireImmunityData fireData = FireImmunityData.get(player);
		fireData.setTimer(fireData.getTimer() - (player.isOnFire() ? 100 : 200));

		if (fireData.pollSync()) {
			Services.NETWORK.sendToPlayer(player, new PacketSyncFireImmunity(fireData.getTimer(), fireData.getLast()));
		}

		tickBlazingCoreEffects(player);
	}

	/** Port-only: drops per-player state kept in static maps when the server stops. */
	public static void onServerStopped() {
		EnigmaticItems.ENIGMATIC_ITEM.flightMap.clear();
		SuperpositionHandler.clearSpellstoneCooldowns();
		FireImmunityData.clear();
		POSTMORTAL_POSESSIONS.clear();
	}

	/**
	 * Port: was upstream's {@code onConfirmedDeath} ({@code LivingDeathEvent}, LOWEST). Called by
	 * MixinServerPlayer at the head of {@code ServerPlayer.die}, where totems and ALLOW_DEATH
	 * have already had their chance, so the death is final. Upstream's canceled-event branch is
	 * not needed. The Enigmatic Amulet and cursed-item parts follow with those items.
	 */
	public static void onConfirmedDeath(ServerPlayer player, DamageSource source) {
		SuperpositionHandler.setPersistentBoolean(player, "DeathFromEntity", source.getEntity() instanceof LivingEntity);

		if (SuperpositionHandler.hasCurio(player, EnigmaticItems.ESCAPE_SCROLL)) {
			POSTMORTAL_POSESSIONS.put(player, EnigmaticItems.ESCAPE_SCROLL);

			ItemStack tomeStack = SuperpositionHandler.getCurioStack(player, EnigmaticItems.ESCAPE_SCROLL);
			PermanentItemEntity droppedTomeStack = new PermanentItemEntity(player.level(), player.getX(), player.getY() + (player.getBbHeight() / 2), player.getZ(), tomeStack.copy());
			droppedTomeStack.setPickupDelay(10);
			player.level().addFreshEntity(droppedTomeStack);

			tomeStack.shrink(1);
		}
	}

	/**
	 * Port: the End city part of upstream's {@code LootTableLoadEvent} handler. Fabric calls it
	 * from {@code LootTableEvents.MODIFY}; {@code addPool} adds the pool to the table.
	 * The other dungeon pools are not ported yet.
	 */
	public static void onLootTablesLoaded(ResourceLocation id, Consumer<LootPool> addPool) {
		if (!OmniconfigHandler.customDungeonLootEnabled.getValue())
			return;

		if (id.equals(BuiltInLootTables.END_CITY_TREASURE)) {
			// Port: upstream also adds Lore Inscriber (weight 10) and Etherium Ore (weight 60,
			// 1-2); neither is ported yet, so they are left out.
			LootPool epic = SuperpositionHandler.constructLootPool("epic", 1F, 2F,
					LootItem.lootTableItem(Items.ENDER_PEARL).setWeight(40).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))),
					LootItem.lootTableItem(Items.ENDER_EYE).setWeight(20).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))),
					LootItem.lootTableItem(Items.GLISTERING_MELON_SLICE).setWeight(30).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))),
					LootItem.lootTableItem(Items.GOLDEN_CARROT).setWeight(30).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))),
					LootItem.lootTableItem(Items.PHANTOM_MEMBRANE).setWeight(25).apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 7))),
					LootItem.lootTableItem(Items.ENCHANTING_TABLE).setWeight(10),
					LootItem.lootTableItem(Items.CAKE).setWeight(15),
					LootItem.lootTableItem(Items.END_CRYSTAL).setWeight(7),
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.RECALL_POTION, 15),
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.MENDING_MIXTURE, 40),
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.ASTRAL_DUST, 85, 1F, 4F),
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.EXTRADIMENSIONAL_EYE, 20));

			addPool.accept(epic);
			// Port: upstream's second End pool (Corrupted Tome with a revelation function) is not ported.
		} else if (SuperpositionHandler.getNetherDungeons().contains(id)) {
			// Port: upstream's nether "epic" pool is not ported yet; only the spellstone pool is.
			LootPool spellstones = SuperpositionHandler.constructLootPool("spellstones", -24F, 1F,
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.BLAZING_CORE, 100));

			addPool.accept(spellstones);
		}
	}

	/**
	 * Port: was the Forge {@code PlayerEvent.BreakSpeed} handler (miningStuff). Fabric calls it from
	 * {@code MixinPlayer} at the end of {@code Player.getDestroySpeed}, where Forge fires the event.
	 * Only the Charm of Treasure Hunter's boost and the Heart of Creation's airborne term are ported
	 * so far; the Cursed Scroll, Enigmatic Amulet, Cube, flight scrolls and Ocean Stone terms follow
	 * with those items.
	 *
	 * @return the new break speed.
	 */
	public static float onBreakSpeed(Player player, float originalSpeed) {
		float correctedSpeed = originalSpeed;

		float miningBoost = 1.0F;
		if (SuperpositionHandler.hasCurio(player, EnigmaticItems.MINING_CHARM)) {
			miningBoost += MiningCharm.breakSpeedBonus.getValue().asModifier();
		}

		if (!player.onGround())
			if (SuperpositionHandler.hasCurio(player, EnigmaticItems.ENIGMATIC_ITEM)) {
				correctedSpeed *= 5F;
			}

		correctedSpeed = correctedSpeed * miningBoost;
		correctedSpeed -= originalSpeed;

		return originalSpeed + correctedSpeed;
	}

	/**
	 * Port: stands in for Curios' fortune loot modifier, which adds each worn curio's
	 * getFortuneLevel to the tool's Fortune. Fabric calls it from {@code MixinApplyBonusCount}
	 * and {@code MixinBonusLevelTableCondition}, where vanilla reads the tool's enchantment level.
	 *
	 * @return the enchantment level with worn accessories' Fortune added.
	 */
	public static int addAccessoryFortune(int level, Enchantment enchantment, LootContext context) {
		if (enchantment == Enchantments.BLOCK_FORTUNE
				&& context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof LivingEntity entity)
			return level + Services.ACCESSORIES.getFortuneBonus(entity, context);

		return level;
	}

	/**
	 * Port: stands in for {@code LivingDropsEvent}. Called by MixinLivingEntity at the end of
	 * dropAllDeathLoot with every drop it captured; whatever is left in {@code drops} afterwards
	 * is spawned, as Forge does. Runs upstream's NORMAL handler, then its LOWEST one.
	 */
	public static void onLivingDrops(LivingEntity entity, DamageSource source, Collection<ItemEntity> drops,
			int lootingLevel, boolean recentlyHit) {
		onLivingDropsNormal(entity, source, drops, lootingLevel, recentlyHit);
		onLivingDropsLowest(entity, source, drops);
	}

	/** Upstream onLivingDrops (NORMAL), Axe of Executioner part. */
	private static void onLivingDropsNormal(LivingEntity entity, DamageSource source, Collection<ItemEntity> drops,
			int lootingLevel, boolean recentlyHit) {
		if (!recentlyHit || !(source.getEntity() instanceof Player player)
				|| player.getMainHandItem().getItem() != EnigmaticItems.FORBIDDEN_AXE)
			return;

		if (entity.getClass() == Skeleton.class) {
			beheadIfLucky(entity, player, drops, lootingLevel, Items.SKELETON_SKULL);
		} else if (entity.getClass() == WitherSkeleton.class) {
			if (!ifDroplistContainsItem(drops, Items.WITHER_SKELETON_SKULL) && theySeeMeRollin(lootingLevel)) {
				addDrop(entity, drops, new ItemStack(Items.WITHER_SKELETON_SKULL));
			}

			if (ifDroplistContainsItem(drops, Items.WITHER_SKELETON_SKULL) && player instanceof ServerPlayer serverPlayer) {
				BeheadingTrigger.INSTANCE.trigger(serverPlayer);
			}
		} else if (entity.getClass() == Zombie.class) {
			beheadIfLucky(entity, player, drops, lootingLevel, Items.ZOMBIE_HEAD);
		} else if (entity.getClass() == Creeper.class) {
			beheadIfLucky(entity, player, drops, lootingLevel, Items.CREEPER_HEAD);
		} else if (entity.getClass() == EnderDragon.class) {
			beheadIfLucky(entity, player, drops, lootingLevel, Items.DRAGON_HEAD);
		}
	}

	private static void beheadIfLucky(LivingEntity entity, Player player, Collection<ItemEntity> drops,
			int lootingLevel, Item head) {
		if (!ifDroplistContainsItem(drops, head) && theySeeMeRollin(lootingLevel)) {
			addDrop(entity, drops, new ItemStack(head));

			if (player instanceof ServerPlayer serverPlayer) {
				BeheadingTrigger.INSTANCE.trigger(serverPlayer);
			}
		}
	}

	/**
	 * Upstream onLivingDropsLowest, Escape Scroll part. The Eldritch Pan, Enigmatic Amulet,
	 * soul crystal and soul loss parts follow with those items.
	 */
	private static void onLivingDropsLowest(LivingEntity entity, DamageSource source, Collection<ItemEntity> drops) {
		if (entity instanceof ServerPlayer player) {
			CompoundTag deathLocation = new CompoundTag();
			deathLocation.putDouble("x", player.getX());
			deathLocation.putDouble("y", player.getY());
			deathLocation.putDouble("z", player.getZ());
			deathLocation.putString("dimension", player.level().dimension().location().toString());
			SuperpositionHandler.setPersistentTag(player, "LastDeathLocation", deathLocation);

			boolean hadEscapeScroll = POSTMORTAL_POSESSIONS.containsEntry(player, EnigmaticItems.ESCAPE_SCROLL);
			DimensionalPosition dimPoint = hadEscapeScroll ? SuperpositionHandler.getRespawnPoint(player) : new DimensionalPosition(player.getX(), player.getY(), player.getZ(), player.level());

			if (hadEscapeScroll) {
				// TODO: upstream sends drops onto the End Anchor when the player respawns at one
				// (no scatter); add that check when the End Anchor block is ported.
				boolean isEndAnchor = false;

				player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, (float) (0.8F + (Math.random() * 0.2)));
				Services.NETWORK.sendToNear(new TargetPoint(player.getX(), player.getY(), player.getZ(), 128, player.level().dimension()), new PacketPortalParticles(player.getX(), player.getY() + (player.getBbHeight() / 2), player.getZ(), 100, 1.25F, false));

				for (ItemEntity dropIt : drops) {
					ItemEntity alternativeDrop = new ItemEntity(dimPoint.world, dimPoint.posX, dimPoint.posY, dimPoint.posZ, dropIt.getItem());
					alternativeDrop.teleportTo(dimPoint.posX, dimPoint.posY, dimPoint.posZ);

					if (!isEndAnchor) {
						alternativeDrop.setDeltaMovement(player.getRandom().nextDouble() - 0.5, player.getRandom().nextDouble() - 0.5, player.getRandom().nextDouble() - 0.5);
					} else {
						alternativeDrop.setDeltaMovement(0, 0, 0);
					}

					dimPoint.world.addFreshEntity(alternativeDrop);
					dropIt.setItem(ItemStack.EMPTY);
				}

				drops.clear();

				dimPoint.world.playSound(null, BlockPos.containing(dimPoint.posX, dimPoint.posY, dimPoint.posZ), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, (float) (0.8F + (Math.random() * 0.2)));
				Services.NETWORK.sendToNear(new TargetPoint(dimPoint.posX, dimPoint.posY, dimPoint.posZ, 128, dimPoint.world.dimension()), new PacketRecallParticles(dimPoint.posX, dimPoint.posY, dimPoint.posZ, 48, false));
			}

			POSTMORTAL_POSESSIONS.removeAll(player);
		} else if (entity instanceof Player) {
			POSTMORTAL_POSESSIONS.removeAll(entity);
		}
	}

	/** Upstream addDrop. The drop spawns with the rest of the list after the handlers run. */
	private static void addDrop(LivingEntity entity, Collection<ItemEntity> drops, ItemStack stack) {
		ItemEntity drop = new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), stack);
		drop.setPickUpDelay(10);
		drops.add(drop);
	}

	/** Upstream SuperpositionHandler.ifDroplistContainsItem. */
	private static boolean ifDroplistContainsItem(Collection<ItemEntity> drops, Item item) {
		for (ItemEntity drop : drops) {
			if (drop.getItem().getItem() == item)
				return true;
		}

		return false;
	}

	public static boolean theySeeMeRollin(int lootingLevel) {
		int chance = ForbiddenAxe.beheadingBase.getValue().asPercentage()
				+ ForbiddenAxe.beheadingBonus.getValue().asPercentage() * lootingLevel;
		return new Perhaps(Math.min(chance, 100)).roll();
	}

}
