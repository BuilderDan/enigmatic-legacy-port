package com.aizistral.enigmaticlegacy.handlers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.aizistral.enigmaticlegacy.config.OmniconfigHandler;
import com.aizistral.enigmaticlegacy.entities.PermanentItemEntity;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.items.EnigmaticAmulet;
import com.aizistral.enigmaticlegacy.items.EnigmaticAmulet.AmuletColor;
import com.aizistral.enigmaticlegacy.items.ForbiddenAxe;
import com.aizistral.enigmaticlegacy.items.generic.ItemSpellstoneCurio;
import com.aizistral.enigmaticlegacy.items.MiningCharm;
import com.aizistral.enigmaticlegacy.items.BlazingCore;
import com.aizistral.enigmaticlegacy.items.AngelBlessing;
import com.aizistral.enigmaticlegacy.items.AvariceScroll;
import com.aizistral.enigmaticlegacy.items.CursedScroll;
import com.aizistral.enigmaticlegacy.items.EyeOfNebula;
import com.aizistral.enigmaticlegacy.items.OceanStone;
import com.aizistral.enigmaticlegacy.items.VoidPearl;
import com.aizistral.enigmaticlegacy.objects.DimensionalPosition;
import com.aizistral.enigmaticlegacy.objects.FireImmunityData;
import com.aizistral.enigmaticlegacy.mixin.AccessorAbstractArrow;
import com.aizistral.enigmaticlegacy.objects.Perhaps;
import com.aizistral.enigmaticlegacy.packets.clients.PacketCosmicRevive;
import com.aizistral.enigmaticlegacy.packets.clients.PacketForceArrowRotations;
import com.aizistral.enigmaticlegacy.packets.clients.PacketPlayerMotion;
import com.aizistral.enigmaticlegacy.packets.clients.PacketPortalParticles;
import com.aizistral.enigmaticlegacy.packets.clients.PacketRecallParticles;
import com.aizistral.enigmaticlegacy.packets.clients.PacketSyncFireImmunity;
import com.aizistral.enigmaticlegacy.packets.clients.PacketWitherParticles;
import com.aizistral.enigmaticlegacy.platform.Services;
import com.aizistral.enigmaticlegacy.platform.network.TargetPoint;
import com.aizistral.enigmaticlegacy.mixin.AccessorMobEffectInstance;
import com.aizistral.enigmaticlegacy.registries.EnigmaticEffects;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.enigmaticlegacy.registries.EnigmaticSounds;
import com.aizistral.enigmaticlegacy.triggers.BeheadingTrigger;
import com.aizistral.enigmaticlegacy.triggers.ForbiddenFruitTrigger;
import com.aizistral.etherium.core.EtheriumEventHandler;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.food.FoodData;
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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

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
	public static final String NBT_KEY_ENIGMATICGIFT = "enigmaticlegacy.firstjoin";
	public static final Map<Player, Float> LAST_HEALTH = new WeakHashMap<>();
	public static final Random THEY_SEE_ME_ROLLIN = new Random();

	/**
	 * Port: was part of the Forge {@code LivingAttackEvent} handler. Fabric calls it from
	 * {@code ServerLivingEntityEvents.ALLOW_DAMAGE}.
	 *
	 * @return false to cancel the attack (upstream: {@code event.setCanceled(true)}).
	 */
	public static boolean onLivingAttack(LivingEntity entity, DamageSource source, float amount) {
		// Port: upstream EtheriumEventHandler.onEntityAttacked is a separate Forge subscriber.
		if (!EtheriumEventHandler.onEntityAttacked(entity, source))
			return false;

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
			if (player.hasEffect(EnigmaticEffects.MOLTEN_HEART) && EnigmaticEffects.MOLTEN_HEART.providesImmunity(source))
				return false;

			if (source.is(DamageTypes.FALL)) {
				if (EnigmaticItems.ENIGMATIC_AMULET.hasColor(player, AmuletColor.MAGENTA) && amount <= 2.0f)
					return false;
			}

			List<ItemStack> advancedCurios = SuperpositionHandler.getAdvancedCurios(player);
			if (advancedCurios.size() > 0) {
				for (ItemStack advancedCurioStack : advancedCurios) {
					ItemSpellstoneCurio advancedCurio = (ItemSpellstoneCurio) advancedCurioStack.getItem();

					if (advancedCurio.immunityList.stream().anyMatch(source::is))
						return false;
				}
			}

			if (SuperpositionHandler.hasCurio(player, EnigmaticItems.EYE_OF_NEBULA)) {
				if (EyeOfNebula.dodgeProbability.getValue().roll() && player.invulnerableTime <= 10 && source.getEntity() instanceof LivingEntity) {
					for (int counter = 0; counter <= 32; counter++) {
						if (SuperpositionHandler.validTeleportRandomly(player, player.level(), (int) EyeOfNebula.dodgeRange.getValue())) {
							break;
						}
					}

					player.invulnerableTime = 20;
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
	 * the event, through {@link #onLivingHurt}.
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

			if (advancedCurio == EnigmaticItems.GOLEM_HEART && (source.is(DamageTypes.EXPLOSION) || source.is(DamageTypes.PLAYER_EXPLOSION))
					&& SuperpositionHandler.hasAnyArmor(player)) {
				continue;
			} else if (advancedCurio == EnigmaticItems.EYE_OF_NEBULA && player.isInWater()) {
				amount *= 2F;
			} else if (advancedCurio == EnigmaticItems.OCEAN_STONE && trueSource != null) {
				if (trueSource.getMobType() == MobType.WATER || trueSource instanceof Drowned) {
					amount *= OceanStone.underwaterCreaturesResistance.getValue().asModifierInverted();
				}
			}

			for (var type : advancedCurio.resistanceList.keySet()) {
				if (source.is(type)) {
					amount *= advancedCurio.resistanceList.get(type).get();
					break;
				}
			}
		}

		if (source.is(DamageTypes.FALL)) {
			if (EnigmaticItems.ENIGMATIC_AMULET.hasColor(player, AmuletColor.MAGENTA)) {
				amount -= 2.0f;
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

		amount = EnigmaticItems.COSMIC_SCROLL.onEntityHurt(player, source, amount);

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
				if (effect.getEffect().equals(EnigmaticEffects.MOLTEN_HEART)) {
					// Port: duration is private on Fabric; see AccessorMobEffectInstance.
					AccessorMobEffectInstance accessor = (AccessorMobEffectInstance) effect;

					if (player.tickCount % 2 == 0 && accessor.enigmaticlegacy$getDuration() > 0) {
						accessor.enigmaticlegacy$setDuration(accessor.enigmaticlegacy$getDuration() + 1);
					}
				} else {
					effect.tick(player, () -> {});
				}
			}
		}
	}

	/**
	 * Port: was the Forge {@code MobEffectEvent.Applicable} handler. Fabric calls it from
	 * {@code MixinLivingEntity} at the head of {@code canBeAffected}, where Forge fires the event.
	 *
	 * @return false to deny the effect (upstream: {@code event.setResult(Result.DENY)}).
	 */
	public static boolean onPotionApplicable(LivingEntity entity, MobEffectInstance effect) {
		if (entity instanceof Player player) {
			if (isApplyingNightVision)
				return true;

			if (new ResourceLocation("mana-and-artifice", "chrono-exhaustion").equals(BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect())))
				return true;

			if (SuperpositionHandler.hasCurio(player, EnigmaticItems.VOID_PEARL))
				return false;
			else if ((SuperpositionHandler.hasCurio(player, EnigmaticItems.ENIGMATIC_ITEM) || SuperpositionHandler.hasCurio(player, EnigmaticItems.THE_CUBE))
					&& !effect.getEffect().isBeneficial())
				return false;
		}

		return true;
	}

	/**
	 * Port: was the Forge {@code LivingDeathEvent} handler (Cube, Heart of Creation, Pearl of the
	 * Void, Architect's Favor). Fabric calls it from {@code ServerLivingEntityEvents.ALLOW_DEATH}.
	 * The Nemesis Curse term follows with that item.
	 *
	 * @return false to cancel the death (upstream: {@code event.setCanceled(true)}).
	 */
	public static boolean onLivingDeath(LivingEntity entity, DamageSource source, float amount) {
		if (entity instanceof Player player && !player.level().isClientSide) {
			if (LAST_HEALTH.containsKey(player) && LAST_HEALTH.get(player) >= 1.5F && SuperpositionHandler.hasCurio(player, EnigmaticItems.THE_CUBE)) {
				player.setHealth(1);
				return false;
			} else if (SuperpositionHandler.hasCurio(player, EnigmaticItems.ENIGMATIC_ITEM) || player.getInventory().contains(new ItemStack(EnigmaticItems.ENIGMATIC_ITEM))) {
				player.setHealth(1);
				return false;
			} else if (SuperpositionHandler.hasCurio(player, EnigmaticItems.VOID_PEARL) && Math.random() <= VoidPearl.undeadProbability.getValue().asMultiplier(false)) {
				player.setHealth(1);
				return false;
			}

			if (!EnigmaticItems.COSMIC_SCROLL.onLivingDeath(entity))
				return false;

			if (player instanceof ServerPlayer serverPlayer && SuperpositionHandler.hasCurio(serverPlayer, EnigmaticItems.THE_CUBE)
					&& !SuperpositionHandler.hasSpellstoneCooldown(serverPlayer)) {
				serverPlayer.setHealth(serverPlayer.getMaxHealth() * 0.3F);
				EnigmaticItems.THE_CUBE.triggerActiveAbility(serverPlayer.level(), serverPlayer, SuperpositionHandler.getCurioStack(serverPlayer, EnigmaticItems.THE_CUBE));

				serverPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 2));
				serverPlayer.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 1));
				serverPlayer.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1200, 0));
				serverPlayer.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 1));

				Services.NETWORK.sendToNear(new TargetPoint(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), 128, serverPlayer.level().dimension()),
						new PacketCosmicRevive(serverPlayer.getId(), 1));
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
		// TODO: Unwitnessed Amulet -> Enigmatic Amulet conversion when the Unwitnessed Amulet is ported.
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

		// Port: upstream ran this for players in its LivingTickEvent handler.
		if (EnigmaticItems.FORBIDDEN_FRUIT.haveConsumedFruit(player)) {
			FoodData foodStats = player.getFoodData();
			foodStats.setFoodLevel(20);
			foodStats.setSaturation(0F);

			if (player.hasEffect(MobEffects.HUNGER)) {
				player.removeEffect(MobEffects.HUNGER);
			}
		}
	}

	/** Port-only: drops per-player state kept in static maps when the server stops. */
	public static void onServerStopped() {
		EnigmaticItems.ENIGMATIC_ITEM.flightMap.clear();
		SuperpositionHandler.clearSpellstoneCooldowns();
		FireImmunityData.clear();
		POSTMORTAL_POSESSIONS.clear();
		EnigmaticItems.HEAVEN_SCROLL.flyMap.clear();
		EnigmaticItems.FABULOUS_SCROLL.flyMap.clear();
		EnigmaticItems.THE_CUBE.clearLocationCache();
		EyeOfNebula.EMPOWERED_PLAYERS.clear();
		LAST_HEALTH.clear();
	}

	/**
	 * Port: was upstream's {@code onConfirmedDeath} ({@code LivingDeathEvent}, LOWEST). Called by
	 * MixinServerPlayer at the head of {@code ServerPlayer.die}, where totems and ALLOW_DEATH
	 * have already had their chance, so the death is final. Upstream's canceled-event branch is
	 * not needed. The Ascension Amulet and cursed-item parts follow with those items.
	 */
	public static void onConfirmedDeath(ServerPlayer player, DamageSource source) {
		SuperpositionHandler.setPersistentBoolean(player, "DeathFromEntity", source.getEntity() instanceof LivingEntity);

		// TODO: Ascension Amulet branch (else-if) when that item is ported.
		// TODO: the POSTMORTAL_POSESSIONS amulet entry is read by upstream's drops handler to summon
		// the Extradimensional Vessel (storage crystal, saved XP); not ported yet, so the amulet
		// currently drops like any other item.
		if (SuperpositionHandler.hasCurio(player, EnigmaticItems.ENIGMATIC_AMULET) || SuperpositionHandler.hasItem(player, EnigmaticItems.ENIGMATIC_AMULET)) {
			POSTMORTAL_POSESSIONS.put(player, EnigmaticItems.ENIGMATIC_AMULET);
		}

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
	 * Port: was upstream's {@code LivingDamageEvent} handler. Fabric calls it from MixinLivingEntity
	 * and MixinPlayer in {@code actuallyHurt}, right before the combat tracker records the damage,
	 * which is where Forge fires the event.
	 */
	public static void onLivingDamage(LivingEntity entity, DamageSource source, float amount) {
		if (entity instanceof ServerPlayer player) {
			LAST_HEALTH.put(player, player.getHealth());
		}

		if (source.getDirectEntity() instanceof Player player && source.is(DamageTypes.PLAYER_ATTACK)
				&& SuperpositionHandler.hasCurio(player, EnigmaticItems.VOID_PEARL)) {
			entity.addEffect(new MobEffectInstance(MobEffects.WITHER, VoidPearl.witheringTime.getValue(), VoidPearl.witheringLevel.getValue(), false, true));
		}

		if (source.getDirectEntity() instanceof Player player && !player.level().isClientSide) {
			float lifesteal = 0;

			if (EnigmaticItems.ENIGMATIC_AMULET.hasColor(player, AmuletColor.BLACK)) {
				lifesteal += amount * 0.1F;
			}

			// TODO: Eldritch Amulet, Pact of Infinite Avarice, Eldritch Pan and Growing Bloodlust terms.

			if (lifesteal > 0) {
				player.heal(lifesteal);
			}
		}
	}

	/**
	 * Port: was upstream's {@code ProjectileImpactEvent} handler. Fabric calls it from the
	 * projectile mixins (MixinAbstractArrow, MixinAbstractHurtingProjectile and
	 * MixinThrowableProjectile) in place of the {@code onHit} call in {@code tick}.
	 *
	 * @return true to cancel the impact (upstream: {@code event.setCanceled(true)}).
	 */
	public static boolean onProjectileImpact(Projectile arrow, HitResult hit) {
		if (hit instanceof EntityHitResult result && result.getEntity() instanceof Player player) {
			if (!player.level().isClientSide) {
				if (arrow.getOwner() == player) {
					for (String tag : arrow.getTags()) {
						if (tag.startsWith("AB_DEFLECTED")) {
							try {
								int time = Integer.parseInt(tag.split(":")[1]);
								if (arrow.tickCount - time < 10)
									// If we cancel the event here it gets stuck in the infinite loop
									return false;
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}
				}

				boolean trigger = false;
				double chance = 0.0D;

				if (SuperpositionHandler.hasCurio(player, EnigmaticItems.ANGEL_BLESSING)) {
					trigger = true;
					chance += AngelBlessing.deflectChance.getValue().asModifier(false);
				}

				if (SuperpositionHandler.hasCurio(player, EnigmaticItems.THE_CUBE)) {
					trigger = true;
					chance += 0.35;
				}

				if (EnigmaticItems.ENIGMATIC_AMULET.hasColor(player, AmuletColor.VIOLET)) {
					trigger = true;
					chance += 0.15;
				}

				if (trigger && Math.random() <= chance) {
					arrow.setDeltaMovement(arrow.getDeltaMovement().scale(-1.0D));
					arrow.yRotO = arrow.getYRot() + 180.0F;
					arrow.setYRot(arrow.getYRot() + 180.0F);

					if (arrow instanceof AbstractArrow absar) {
						if (!(absar instanceof ThrownTrident)) {
							absar.setOwner(player);
						}

						((AccessorAbstractArrow) absar).enigmaticlegacy$resetPiercedEntities();
						absar.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
						absar.setPierceLevel((byte) 0);
					} else if (arrow instanceof AbstractHurtingProjectile hurting) {
						hurting.setOwner(player);

						hurting.xPower = -hurting.xPower;
						hurting.yPower = -hurting.yPower;
						hurting.zPower = -hurting.zPower;
					}

					arrow.getTags().removeIf(tag -> tag.startsWith("AB_DEFLECTED"));
					arrow.addTag("AB_DEFLECTED:" + arrow.tickCount);

					Services.NETWORK.sendToNear(new TargetPoint(player.getX(), player.getY(), player.getZ(), 64.0D, player.level().dimension()),
							new PacketForceArrowRotations(arrow.getId(), arrow.getYRot(), arrow.getXRot(), arrow.getDeltaMovement().x, arrow.getDeltaMovement().y, arrow.getDeltaMovement().z, arrow.getX(), arrow.getY(), arrow.getZ()));
					player.level().playSound(null, player.blockPosition(), EnigmaticSounds.DEFLECT, SoundSource.PLAYERS, 1.0F, 0.95F + (float) (Math.random() * 0.1D));
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Port: the first-join gift part of upstream's {@code PlayerLoggedInEvent} handler. Fabric
	 * calls it from {@code ServerPlayConnectionEvents.JOIN}.
	 */
	public static void onPlayerJoin(ServerPlayer player) {
		EnigmaticItems.FORBIDDEN_FRUIT.syncConsumedFruit(player);
		EnigmaticItems.COSMIC_SCROLL.onPlayerJoin(player);

		if (EnigmaticItems.FORBIDDEN_FRUIT.haveConsumedFruit(player)) {
			ForbiddenFruitTrigger.INSTANCE.trigger(player);
		}

		if (OmniconfigHandler.isItemEnabled(EnigmaticItems.ENIGMATIC_AMULET))
			if (SuperpositionHandler.getPersistentTag(player, NBT_KEY_ENIGMATICGIFT, null) == null) {
				ItemStack amuletStack = new ItemStack(EnigmaticItems.ENIGMATIC_AMULET);
				EnigmaticItems.ENIGMATIC_AMULET.setInscription(amuletStack, player.getGameProfile().getName());
				EnigmaticItems.ENIGMATIC_AMULET.setProperlyGranted(amuletStack);

				if (!EnigmaticAmulet.seededColorGen.getValue()) {
					EnigmaticItems.ENIGMATIC_AMULET.setPseudoRandomColor(amuletStack);
				} else {
					EnigmaticItems.ENIGMATIC_AMULET.setSeededColor(amuletStack);
				}

				if (player.getInventory().getItem(8).isEmpty()) {
					player.getInventory().setItem(8, amuletStack);
				} else {
					if (!player.getInventory().add(amuletStack)) {
						ItemEntity dropAmulet = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), amuletStack);
						player.level().addFreshEntity(dropAmulet);
					}
				}

				SuperpositionHandler.setPersistentBoolean(player, NBT_KEY_ENIGMATICGIFT, true);
			}
	}

	/**
	 * Port: the End city part of upstream's {@code LootTableLoadEvent} handler. Fabric calls it
	 * from {@code LootTableEvents.MODIFY}; {@code addPool} adds the pool to the table.
	 * Also adds the spellstone pools; the other dungeon "epic" pools are not ported yet.
	 */
	public static void onLootTablesLoaded(ResourceLocation id, Consumer<LootPool> addPool) {
		if (!OmniconfigHandler.customDungeonLootEnabled.getValue())
			return;

		if (id.equals(BuiltInLootTables.END_CITY_TREASURE)) {
			// Port: upstream also adds Lore Inscriber (weight 10); it is not ported yet, so it is left out.
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
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.ETHERIUM_ORE, 60, 1F, 2F),
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.EXTRADIMENSIONAL_EYE, 20));

			addPool.accept(epic);
			// Port: upstream's second End pool (Corrupted Tome with a revelation function) is not ported.
		}

		LootPool spellstones = null;

		if (SuperpositionHandler.getMergedAir$EarthenDungeons().contains(id)) {
			spellstones = SuperpositionHandler.constructLootPool("spellstones", -12F, 1F,
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.GOLEM_HEART, 35),
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.ANGEL_BLESSING, 65),
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.ICHOR_BOTTLE, 65));
		} else if (SuperpositionHandler.getMergedEnder$EarthenDungeons().contains(id)) {
			spellstones = SuperpositionHandler.constructLootPool("spellstones", -10F, 1F,
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.EYE_OF_NEBULA, 35),
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.GOLEM_HEART, 65));
		} else if (SuperpositionHandler.getAirDungeons().contains(id)) {
			spellstones = SuperpositionHandler.constructLootPool("spellstones", -10F, 1F,
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.ANGEL_BLESSING, 100),
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.ICHOR_BOTTLE, 100));
		} else if (SuperpositionHandler.getEarthenDungeons().contains(id)) {
			spellstones = SuperpositionHandler.constructLootPool("spellstones", -20F, 1F,
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.GOLEM_HEART, 100));
		} else if (SuperpositionHandler.getNetherDungeons().contains(id)) {
			// Port: upstream's nether "epic" pool is not ported yet; only the spellstone pool is.
			spellstones = SuperpositionHandler.constructLootPool("spellstones", -24F, 1F,
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.BLAZING_CORE, 100));
		} else if (SuperpositionHandler.getWaterDungeons().contains(id)) {
			spellstones = SuperpositionHandler.constructLootPool("spellstones", -20F, 1F,
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.OCEAN_STONE, 100));
		} else if (SuperpositionHandler.getEnderDungeons().contains(id)) {
			spellstones = SuperpositionHandler.constructLootPool("spellstones", -12F, 1F,
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.EYE_OF_NEBULA, 90),
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.VOID_PEARL, 10));
		}

		if (spellstones != null) {
			addPool.accept(spellstones);
		}
	}

	/**
	 * Port: was the Forge {@code PlayerEvent.BreakSpeed} handler (miningStuff). Fabric calls it from
	 * {@code MixinPlayer} at the end of {@code Player.getDestroySpeed}, where Forge fires the event.
	 *
	 * @return the new break speed.
	 */
	public static float onBreakSpeed(Player player, float originalSpeed) {
		float correctedSpeed = originalSpeed;

		float miningBoost = 1.0F;
		if (SuperpositionHandler.hasCurio(player, EnigmaticItems.MINING_CHARM)) {
			miningBoost += MiningCharm.breakSpeedBonus.getValue().asModifier();
		}

		if (SuperpositionHandler.hasCurio(player, EnigmaticItems.CURSED_SCROLL)) {
			miningBoost += CursedScroll.miningBoost.getValue().asModifier() * SuperpositionHandler.getCurseAmount(player);
		}

		if (EnigmaticItems.ENIGMATIC_AMULET.hasColor(player, AmuletColor.GREEN)) {
			miningBoost += 0.25F;
		}

		if (SuperpositionHandler.hasCurio(player, EnigmaticItems.THE_CUBE)) {
			miningBoost += 0.6F;
		}

		if (!player.onGround())
			if (SuperpositionHandler.hasCurio(player, EnigmaticItems.HEAVEN_SCROLL) || SuperpositionHandler.hasCurio(player, EnigmaticItems.FABULOUS_SCROLL)
					|| SuperpositionHandler.hasCurio(player, EnigmaticItems.ENIGMATIC_ITEM)) {
				correctedSpeed *= 5F;
			}

		if (player.isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player))
			if (SuperpositionHandler.hasCurio(player, EnigmaticItems.OCEAN_STONE)) {
				correctedSpeed *= 5F;
			}

		correctedSpeed = correctedSpeed * miningBoost;
		correctedSpeed -= originalSpeed;

		return originalSpeed + correctedSpeed;
	}

	/**
	 * Port: the rest of upstream's {@code LivingHurtEvent} handler (attacker terms and the Cube).
	 * Fabric calls it from MixinLivingEntity and MixinPlayer at the head of {@code actuallyHurt};
	 * player targets also go through {@link #onPlayerHurt}. The Berserk Emblem and Hunter's
	 * Guidebook terms follow with those items.
	 *
	 * @return the new damage amount.
	 */
	public static float onLivingHurt(LivingEntity victim, DamageSource source, float amount) {
		if (source.getEntity() instanceof Player attacker) {
			if (SuperpositionHandler.hasCurio(attacker, EnigmaticItems.CURSED_SCROLL)) {
				amount += amount * (CursedScroll.damageBoost.getValue().asModifier() * SuperpositionHandler.getCurseAmount(attacker));
			}

			if (SuperpositionHandler.hasCurio(attacker, EnigmaticItems.EYE_OF_NEBULA) && SuperpositionHandler.isMagic(source)) {
				amount *= EyeOfNebula.magicBoost.getValue().asModifier(true);
			}

			if (EyeOfNebula.EMPOWERED_PLAYERS.remove(attacker.getUUID())) {
				amount *= EyeOfNebula.attackEmpower.getValue().asModifier(true);
			}
		}

		if (victim instanceof Player player) {
			amount = onPlayerHurt(player, source, amount);
		}

		if (victim instanceof ServerPlayer player && SuperpositionHandler.hasCurio(player, EnigmaticItems.THE_CUBE)) {
			if (source.getDirectEntity() instanceof LivingEntity living) {
				if (amount <= EnigmaticItems.THE_CUBE.getDamageLimit(player) && THEY_SEE_ME_ROLLIN.nextDouble() <= 0.35) {
					living.hurt(source, amount);
					player.level().playSound(null, player.blockPosition(), EnigmaticSounds.SWORD_HIT_REJECT, SoundSource.PLAYERS, 1.0F, 1.0F);
					return 0;
				} else {
					EnigmaticItems.THE_CUBE.applyRandomEffect(living, false);
				}
			}

			if (source.getEntity() != null && amount > EnigmaticItems.THE_CUBE.getDamageLimit(player)) {
				player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);

				if (source.getDirectEntity() instanceof LivingEntity living) {
					Vec3 dir = living.position().subtract(player.position()).normalize();
					knockBack(living, 1.0F, dir.x, dir.z);
				}

				return 0;
			}
		}

		// Port: upstream EtheriumEventHandler.onEntityHurt is a separate Forge subscriber.
		amount = EtheriumEventHandler.onEntityHurt(victim, source, amount);

		return amount;
	}

	/** Upstream EtheriumConfigHandler.knockBack (pulls the entity toward the player). */
	private static void knockBack(LivingEntity entity, float strength, double xRatio, double zRatio) {
		entity.hasImpulse = true;
		Vec3 vec = new Vec3(xRatio, 0.0D, zRatio).normalize().scale(strength);
		double y = entity.onGround() ? Math.min(0.4D, strength) : 0.0D;

		if (entity instanceof ServerPlayer player) {
			Services.NETWORK.sendToPlayer(player, new PacketPlayerMotion(-vec.x, y, -vec.z));
		}

		entity.setDeltaMovement(-vec.x, y, -vec.z);
	}

	/**
	 * Port: the Cube part of upstream's {@code LivingDeathEvent} handler for killed entities.
	 * Fabric calls it from {@code ServerLivingEntityEvents.AFTER_DEATH}.
	 */
	public static void onEntityKilled(LivingEntity entity, DamageSource source) {
		if (source.getEntity() instanceof ServerPlayer player && SuperpositionHandler.hasCurio(player, EnigmaticItems.THE_CUBE)) {
			EnigmaticItems.THE_CUBE.applyRandomEffect(player, true);
		}
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

	/** Upstream onLivingDrops (NORMAL), Pact of Infinite Avarice and Axe of Executioner parts. */
	private static void onLivingDropsNormal(LivingEntity entity, DamageSource source, Collection<ItemEntity> drops,
			int lootingLevel, boolean recentlyHit) {
		if (recentlyHit && source.getEntity() instanceof Player killer && SuperpositionHandler.isTheCursedOne(killer)) {
			if (SuperpositionHandler.hasCurio(killer, EnigmaticItems.AVARICE_SCROLL)
					&& THEY_SEE_ME_ROLLIN.nextInt(100) < AvariceScroll.emeraldChance.getValue()) {
				addDrop(entity, drops, new ItemStack(Items.EMERALD, 1));
			}
		}

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
