package com.aizistral.enigmaticlegacy.handlers;

import com.aizistral.enigmaticlegacy.api.quack.IAbyssalHeartBearer;
import com.aizistral.enigmaticlegacy.registries.EnigmaticBlocks;
import net.minecraft.server.level.ServerLevel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.config.OmniconfigHandler;
import com.aizistral.enigmaticlegacy.entities.PermanentItemEntity;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.helpers.PotionHelper;
import com.aizistral.enigmaticlegacy.items.MagnetRing;
import com.aizistral.enigmaticlegacy.items.EnigmaticAmulet;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.SimpleContainer;
import com.aizistral.enigmaticlegacy.items.EldritchPan;
import com.aizistral.enigmaticlegacy.effects.GrowingHungerEffect;
import com.aizistral.enigmaticlegacy.effects.GrowingBloodlustEffect;
import com.aizistral.enigmaticlegacy.items.EnigmaticElytra;
import com.aizistral.enigmaticlegacy.items.EnigmaticAmulet.AmuletColor;
import com.aizistral.enigmaticlegacy.items.ForbiddenAxe;
import com.aizistral.enigmaticlegacy.items.HunterGuidebook;
import com.aizistral.enigmaticlegacy.items.generic.ItemSpellstoneCurio;
import com.aizistral.enigmaticlegacy.items.MiningCharm;
import com.aizistral.enigmaticlegacy.items.BlazingCore;
import com.aizistral.enigmaticlegacy.items.AngelBlessing;
import com.aizistral.enigmaticlegacy.items.AvariceScroll;
import com.aizistral.enigmaticlegacy.items.CursedScroll;
import com.aizistral.enigmaticlegacy.items.BerserkEmblem;
import com.aizistral.enigmaticlegacy.items.TheInfinitum;
import com.aizistral.enigmaticlegacy.items.TheTwist;
import net.minecraft.core.NonNullList;
import com.aizistral.enigmaticlegacy.items.EyeOfNebula;
import com.aizistral.enigmaticlegacy.items.OceanStone;
import com.aizistral.enigmaticlegacy.items.VoidPearl;
import com.aizistral.enigmaticlegacy.mixin.AccessorMob;
import com.aizistral.enigmaticlegacy.mixin.AccessorTargetGoal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import java.util.Optional;
import com.aizistral.enigmaticlegacy.objects.DimensionalPosition;
import com.aizistral.enigmaticlegacy.objects.FireImmunityData;
import com.aizistral.enigmaticlegacy.mixin.AccessorAbstractArrow;
import com.aizistral.enigmaticlegacy.objects.Perhaps;
import com.aizistral.enigmaticlegacy.packets.clients.PacketCosmicRevive;
import com.aizistral.enigmaticlegacy.packets.clients.PacketForceArrowRotations;
import com.aizistral.enigmaticlegacy.packets.clients.PacketPermadeath;
import com.aizistral.enigmaticlegacy.packets.clients.PacketPlayerMotion;
import com.aizistral.enigmaticlegacy.packets.clients.PacketPortalParticles;
import com.aizistral.enigmaticlegacy.packets.clients.PacketRecallParticles;
import com.aizistral.enigmaticlegacy.packets.clients.PacketPatchouliForce;
import com.aizistral.enigmaticlegacy.packets.clients.PacketSetEntryState;
import com.aizistral.enigmaticlegacy.packets.clients.PacketSlotUnlocked;
import com.aizistral.enigmaticlegacy.packets.clients.PacketSyncFireImmunity;
import com.aizistral.enigmaticlegacy.packets.clients.PacketWitherParticles;
import com.aizistral.enigmaticlegacy.platform.Services;
import com.aizistral.enigmaticlegacy.platform.network.TargetPoint;
import com.aizistral.enigmaticlegacy.mixin.AccessorMobEffectInstance;
import com.aizistral.enigmaticlegacy.items.InfernalShield;
import com.aizistral.enigmaticlegacy.registries.EnigmaticEffects;
import com.aizistral.enigmaticlegacy.registries.EnigmaticEnchantments;
import com.aizistral.enigmaticlegacy.registries.EnigmaticDamageTypes;
import com.aizistral.enigmaticlegacy.enchantments.CeaselessEnchantment;
import com.aizistral.enigmaticlegacy.helpers.EnigmaticEnchantmentHelper;
import com.aizistral.enigmaticlegacy.mixin.AccessorThrownTrident;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.CrossbowItem;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import net.minecraft.world.entity.ExperienceOrb;
import com.aizistral.enigmaticlegacy.objects.RegisteredMeleeAttack;
import com.aizistral.enigmaticlegacy.objects.EntityPersistentData;
import com.aizistral.enigmaticlegacy.items.EnderSlayer;
import com.aizistral.enigmaticlegacy.registries.EnigmaticPotions;
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
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.Items;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import com.aizistral.enigmaticlegacy.items.CursedRing;
import com.aizistral.enigmaticlegacy.items.MonsterCharm;
import com.aizistral.enigmaticlegacy.client.Quote;
import com.aizistral.enigmaticlegacy.mixin.AccessorPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
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
	public static boolean isPoisonHurt = false;

	public static final Multimap<Player, Item> POSTMORTAL_POSESSIONS = ArrayListMultimap.create();
	public static final String NBT_KEY_ENIGMATICGIFT = "enigmaticlegacy.firstjoin";
	public static final String NBT_KEY_CURSEDGIFT = "enigmaticlegacy.cursedgift";
	private static final String NBT_KEY_ENABLESPELLSTONE = "enigmaticlegacy.spellstones_enabled";
	private static final String NBT_KEY_ENABLESCROLL = "enigmaticlegacy.scrolls_enabled";
	private static final String NBT_KEY_PATCHOULIFORCE = "enigmaticlegacy.patchouliforce";

	/** Port-only: Ring of the Seven Curses enchanting bonus for the table in use, see MixinEnchantmentMenu. */
	public static final ThreadLocal<Integer> ENCHANTING_BONUS = new ThreadLocal<>();
	public static final Map<Player, Float> LAST_HEALTH = new WeakHashMap<>();
	public static final Random THEY_SEE_ME_ROLLIN = new Random();
	public static final Map<LivingEntity, Float> KNOCKBACK_THAT_BASTARD = new WeakHashMap<>();
	public static final Map<Player, AABB> DESOLATION_BOXES = new WeakHashMap<>();
	public static final Map<Player, Integer> LAST_SOUL_COMPASS_UPDATE = new WeakHashMap<>();
	public static final Multimap<Player, Guardian> AGERED_GUARDIANS = ArrayListMultimap.create();

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

		if (source.getDirectEntity() instanceof Player player && source.is(DamageTypes.PLAYER_ATTACK)) {
			// Port: upstream cancels a non-cursed player's Twist and Ender Slayer hits in LivingHurtEvent; ALLOW_DAMAGE is the cancel point here.
			if ((player.getMainHandItem().is(EnigmaticItems.THE_TWIST) || player.getMainHandItem().is(EnigmaticItems.ENDER_SLAYER))
					&& !SuperpositionHandler.isTheCursedOne(player))
				return false;

			// Port: same for an unworthy player's Infinitum hit, which also curses the wielder.
			if (player.getMainHandItem().is(EnigmaticItems.THE_INFINITUM) && !SuperpositionHandler.isTheWorthyOne(player)) {
				player.addEffect(new MobEffectInstance(MobEffects.WITHER,            160, 3, false, true));
				player.addEffect(new MobEffectInstance(MobEffects.CONFUSION,         500, 3, false, true));
				player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,          300, 3, false, true));
				player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 3, false, true));
				player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,      300, 3, false, true));
				player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,         100, 3, false, true));
				return false;
			}

			// Port: and for an unworthy player's Eldritch Pan hit.
			if (player.getMainHandItem().is(EnigmaticItems.ELDRITCH_PAN) && !SuperpositionHandler.isTheWorthyOne(player))
				return false;

			float knockbackPower = 1F;

			// Port: the Cosmic Scroll knockback term follows with that item.
			if (player.getMainHandItem().is(EnigmaticItems.THE_TWIST) && SuperpositionHandler.isTheCursedOne(player)) {
				knockbackPower += TheTwist.knockbackBonus.getValue().asModifier(false);
			} else if (player.getMainHandItem().is(EnigmaticItems.THE_INFINITUM) && SuperpositionHandler.isTheWorthyOne(player)) {
				knockbackPower += TheInfinitum.knockbackBonus.getValue().asModifier(false);
			} else if (player.getMainHandItem().is(EnigmaticItems.ENDER_SLAYER) && SuperpositionHandler.isTheCursedOne(player)) {
				if (EnigmaticItems.ENDER_SLAYER.isEndDweller(entity)) {
					knockbackPower += EnderSlayer.endKnockbackBonus.getValue().asModifier(false);
				}
			}

			if (knockbackPower > 1) {
				KNOCKBACK_THAT_BASTARD.put(entity, (entity instanceof Phantom ? (knockbackPower*1.5F) : knockbackPower));
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

		if (entity instanceof Animal animal && source.getEntity() instanceof Player player) {
			if (SuperpositionHandler.hasItem(player, EnigmaticItems.ANIMAL_GUIDEBOOK)) {
				if (EnigmaticItems.ANIMAL_GUIDEBOOK.isProtectedAnimal(animal)) {
					// Port: upstream cancels here and un-cancels below; the flag keeps that order.
					boolean canceled = true;

					if (animal.getTarget() == player) {
						canceled = false;
					} else {
						for (WrappedGoal goal : ((AccessorMob) animal).enigmaticlegacy$getTargetSelector().getAvailableGoals())
							if (goal.getGoal() instanceof TargetGoal targetGoal) {
								if (((AccessorTargetGoal) targetGoal).enigmaticlegacy$getTargetMob() == player) {
									canceled = false;
								}
							}
					}

					Brain<?> brain = animal.getBrain();

					if (brain != null) {
						try {
							var memory = brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
									? brain.getMemory(MemoryModuleType.ATTACK_TARGET) : Optional.empty();
							if (memory.isPresent() && memory.get() == player) {
								canceled = false;
							}
						} catch (NullPointerException ex) {
							// I don't get why it happens here but it does lol
						}
					}

					if (canceled)
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

		/*
		 * Handler for stripping away Blazing Strength (upstream runs it at HIGHEST priority).
		 */

		if (amount > 0 && player.hasEffect(EnigmaticEffects.BLAZING_STRENGTH)) {
			player.removeEffect(EnigmaticEffects.BLAZING_STRENGTH);
		}

		if (SuperpositionHandler.hasCurio(player, EnigmaticItems.BERSERK_CHARM)) {
			amount *= 1.0F - (SuperpositionHandler.getMissingHealthPool(player) * (float) BerserkEmblem.damageResistance.getValue());
		}

		/*
		 * Handler for doubling damage on bearers of Ring of the Seven Curses.
		 */

		if (SuperpositionHandler.hasCurio(player, EnigmaticItems.CURSED_RING)) {
			amount *= CursedRing.painMultiplier.getValue().asModifier();
		}

		/*
		 * Handler for increasing damage on users of Bulwark of Blazing Pride.
		 */

		if (source.getEntity() != null) {
			if (player.getUseItem().getItem() instanceof InfernalShield && SuperpositionHandler.isActuallyReallyBlocking(player)) {
				Vec3 sourcePos = source.getSourcePosition();
				if (sourcePos != null) {
					Vec3 lookVec = player.getViewVector(1.0F);
					Vec3 sourceToSelf = sourcePos.vectorTo(player.position()).normalize();
					sourceToSelf = new Vec3(sourceToSelf.x, 0.0D, sourceToSelf.z);
					if (!(sourceToSelf.dot(lookVec) < 0.0D)) {
						amount *= 1.5F;
					}
				}
			}
		}

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
	 *
	 * @return false to cancel the death (upstream: {@code event.setCanceled(true)}).
	 */
	public static boolean onLivingDeath(LivingEntity entity, DamageSource source, float amount) {
		if (entity instanceof Player player && !player.level().isClientSide) {
			if (isPoisonHurt && source.is(DamageTypes.MAGIC)) {
				player.setHealth(1);
				return false;
			} else if (LAST_HEALTH.containsKey(player) && LAST_HEALTH.get(player) >= 1.5F && SuperpositionHandler.hasCurio(player, EnigmaticItems.THE_CUBE)) {
				player.setHealth(1);
				return false;
			} else if (SuperpositionHandler.hasCurio(player, EnigmaticItems.ENIGMATIC_ITEM) || player.getInventory().contains(new ItemStack(EnigmaticItems.ENIGMATIC_ITEM)) || source.is(EnigmaticDamageTypes.NEMESIS_CURSE)) {
				player.setHealth(1);
				return false;
			} else if (SuperpositionHandler.hasCurio(player, EnigmaticItems.VOID_PEARL) && Math.random() <= VoidPearl.undeadProbability.getValue().asMultiplier(false)) {
				player.setHealth(1);
				return false;
			} else if (SuperpositionHandler.isTheWorthyOne(player) && player.getMainHandItem().is(EnigmaticItems.THE_INFINITUM)
					&& Math.random() <= TheInfinitum.undeadProbability.getValue().asMultiplier(false)) {
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
	 * Port: was the Heart of Creation and Growing Hunger parts of the Forge {@code PlayerTickEvent}
	 * handler (server side). Fabric calls it for each player from {@code ServerTickEvents.END_SERVER_TICK}.
	 */
	public static void onPlayerTick(ServerPlayer player) {
		if (SuperpositionHandler.hasSpellstoneCooldown(player)) {
			SuperpositionHandler.tickSpellstoneCooldown(player, 1);
		}

		EnigmaticItems.ENIGMATIC_ITEM.handleEnigmaticFlight(player);

		/*
		 * Handle Growing Hunger application.
		 */

		if (player.getMainHandItem().is(EnigmaticItems.ELDRITCH_PAN)) {
			int currentTicks = EldritchPan.HOLDING_DURATIONS.getOrDefault(player, 0);

			if (SuperpositionHandler.cannotHunger(player)) {
				int bloodlustAmplifier = currentTicks / GrowingBloodlustEffect.ticksPerLevel.getValue();

				bloodlustAmplifier = Math.min(bloodlustAmplifier, 9);

				player.addEffect(new MobEffectInstance(EnigmaticEffects.GROWING_BLOODLUST,
						MobEffectInstance.INFINITE_DURATION, bloodlustAmplifier, true, true));
			} else {
				int hungerAmplifier = currentTicks / GrowingHungerEffect.ticksPerLevel.getValue();

				hungerAmplifier = Math.min(hungerAmplifier, 9);

				player.addEffect(new MobEffectInstance(EnigmaticEffects.GROWING_HUNGER,
						MobEffectInstance.INFINITE_DURATION, hungerAmplifier, true, true));
			}

			EldritchPan.HOLDING_DURATIONS.put(player, currentTicks + 1);
		} else {
			EldritchPan.HOLDING_DURATIONS.put(player, 0);
			player.removeEffect(EnigmaticEffects.GROWING_HUNGER);
			player.removeEffect(EnigmaticEffects.GROWING_BLOODLUST);
		}

		if (SuperpositionHandler.hasCurio(player, EnigmaticItems.DESOLATION_RING) && SuperpositionHandler.isTheWorthyOne(player)) {
			DESOLATION_BOXES.put(player, SuperpositionHandler.getBoundingBoxAroundEntity(player, 128));
		} else {
			DESOLATION_BOXES.remove(player);
		}

		if (SuperpositionHandler.hasItem(player, EnigmaticItems.SOUL_COMPASS)) {
			Integer lastUpdate = LAST_SOUL_COMPASS_UPDATE.get(player);

			if (lastUpdate == null || player.tickCount - lastUpdate > 10) {
				var optional = SuperpositionHandler.updateSoulCompass(player);

				optional.ifPresent(tuple -> {
					BlockPos pos = tuple.getB();

					if (player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 256) {
						player.level().getChunkAt(pos);

						UUID id = tuple.getA();
						int radius = 3;
						AABB box = new AABB(pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius,
								pos.getX() + radius, pos.getY() + radius, pos.getZ() + radius);
						List<PermanentItemEntity> list = player.level().getEntitiesOfClass(PermanentItemEntity.class, box,
								entity -> entity.getUUID().equals(id));

						if (list.size() <= 0) {
							SoulArchive.getInstance().removeItem(id);
						}
					}
				});
			}
		}

		FireImmunityData fireData = FireImmunityData.get(player);
		fireData.setTimer(fireData.getTimer() - (player.isOnFire() ? 100 : 200));

		if (fireData.pollSync()) {
			Services.NETWORK.sendToPlayer(player, new PacketSyncFireImmunity(fireData.getTimer(), fireData.getLast()));
		}

		tickBlazingCoreEffects(player);

		// Detect Unwitnessed Amulet and turn it into a bound Enigmatic Amulet of random color.
		// Port: Inventory.compartments is private on Fabric (Forge widens it); the same three lists are walked here.
		for (NonNullList<ItemStack> list : List.of(player.getInventory().items, player.getInventory().armor, player.getInventory().offhand)) {
			for (int i = 0; i < list.size(); ++i) {
				if (list.get(i).is(EnigmaticItems.UNWITNESSED_AMULET)) {
					ItemStack stack = new ItemStack(EnigmaticItems.ENIGMATIC_AMULET);
					EnigmaticItems.ENIGMATIC_AMULET.setInscription(stack, player.getGameProfile().getName());

					if (EnigmaticAmulet.seededColorGen.getValue()) {
						EnigmaticItems.ENIGMATIC_AMULET.setSeededColor(stack);
					} else {
						EnigmaticItems.ENIGMATIC_AMULET.setRandomColor(stack);
					}

					list.set(i, stack);
				}
			}
		}

		// Port: upstream does this in its PlayerTickEvent inventory loop.
		if (CursedRing.autoEquip.getValue() && !player.isCreative() && !player.isSpectator()
				&& !SuperpositionHandler.hasCurio(player, EnigmaticItems.CURSED_RING)) {
			List<ItemStack> items = player.getInventory().items;

			for (int i = 0; i < items.size(); i++) {
				ItemStack stack = items.get(i);

				if (stack.is(EnigmaticItems.CURSED_RING) && Services.ACCESSORIES.tryForceEquip(player, stack)) {
					items.set(i, ItemStack.EMPTY);
					break;
				}
			}
		}

		// Port: upstream ran these for players in its LivingTickEvent handler.
		if (!CursedRing.disableInsomnia.getValue())
			if (player.isSleeping() && SuperpositionHandler.hasCurio(player, EnigmaticItems.CURSED_RING)) {
				if (player.getSleepTimer() == 5) {
					player.sendSystemMessage(Component.translatable("message.enigmaticlegacy.cursed_sleep")
							.withStyle(ChatFormatting.RED));
				} else if (player.getSleepTimer() > 90) {
					((AccessorPlayer) player).enigmaticlegacy$setSleepCounter(90);
				}
			}

		if (player.isOnFire() && SuperpositionHandler.hasCurio(player, EnigmaticItems.CURSED_RING)) {
			player.setRemainingFireTicks(player.getRemainingFireTicks()+2);
		}

		// Port: upstream ran this for players in its LivingTickEvent handler.
		if (EnigmaticItems.FORBIDDEN_FRUIT.haveConsumedFruit(player)) {
			FoodData foodStats = player.getFoodData();
			foodStats.setFoodLevel(20);
			foodStats.setSaturation(0F);

			if (player.hasEffect(MobEffects.HUNGER)) {
				player.removeEffect(MobEffects.HUNGER);
			}
		}

		// Port: upstream boosted the Majestic Elytra at PlayerTickEvent START.
		EnigmaticElytra.onServerPlayerTick(player);
	}

	/**
	 * Port: stands in for upstream's LivingTickEvent subscriber (onPlayerTick), Ender Slayer part.
	 * Called by MixinLivingEntity at the head of tick.
	 */
	public static void onLivingTick(LivingEntity entity) {
		if (!(entity instanceof EnderMan || entity instanceof Shulker) || !entity.isAlive() || entity.level().isClientSide)
			return;

		CompoundTag data = EntityPersistentData.get(entity);
		int cooldown = data.getInt("ELTeleportBlock");

		if (cooldown > 0) {
			if (--cooldown > 0) {
				data.putInt("ELTeleportBlock", cooldown);
			} else {
				data.remove("ELTeleportBlock");
			}
		}
	}

	/** Port-only: drops per-player state kept in static maps when the server stops. */
	public static void onServerStopped() {
		RegisteredMeleeAttack.clearRegistry();
		EnigmaticItems.ENIGMATIC_ITEM.flightMap.clear();
		SuperpositionHandler.clearSpellstoneCooldowns();
		FireImmunityData.clear();
		POSTMORTAL_POSESSIONS.clear();
		AGERED_GUARDIANS.clear();
		KNOCKBACK_THAT_BASTARD.clear();
		LAST_SOUL_COMPASS_UPDATE.clear();
		EnigmaticItems.HEAVEN_SCROLL.flyMap.clear();
		EnigmaticItems.FABULOUS_SCROLL.flyMap.clear();
		EnigmaticItems.THE_CUBE.clearLocationCache();
		EyeOfNebula.EMPOWERED_PLAYERS.clear();
		LAST_HEALTH.clear();
		EnigmaticItems.SOUL_CRYSTAL.attributeDispatcher.clear();
	}

	/**
	 * Port: was upstream's {@code onConfirmedDeath} ({@code LivingDeathEvent}, LOWEST). Called by
	 * MixinServerPlayer at the head of {@code ServerPlayer.die}, where totems and ALLOW_DEATH
	 * have already had their chance, so the death is final. Upstream's canceled-event branch is
	 * not needed. The cursed-item parts follow with those items.
	 */
	public static void onConfirmedDeath(ServerPlayer player, DamageSource source) {
		// Port: upstream's onProbableDeath (LivingDeathEvent, HIGHEST).
		if (SuperpositionHandler.hasCurio(player, EnigmaticItems.CURSED_RING)) {
			POSTMORTAL_POSESSIONS.put(player, EnigmaticItems.CURSED_RING);
		}

		SuperpositionHandler.setPersistentBoolean(player, "DeathFromEntity", source.getEntity() instanceof LivingEntity);

		if (SuperpositionHandler.hasCurio(player, EnigmaticItems.ENIGMATIC_AMULET) || SuperpositionHandler.hasItem(player, EnigmaticItems.ENIGMATIC_AMULET)) {
			POSTMORTAL_POSESSIONS.put(player, EnigmaticItems.ENIGMATIC_AMULET);
		} else if (SuperpositionHandler.hasCurio(player, EnigmaticItems.ASCENSION_AMULET) || SuperpositionHandler.hasItem(player, EnigmaticItems.ASCENSION_AMULET)) {
			POSTMORTAL_POSESSIONS.put(player, EnigmaticItems.ASCENSION_AMULET);
		}

		if (SuperpositionHandler.hasItem(player, EnigmaticItems.CURSED_STONE)) {
			POSTMORTAL_POSESSIONS.put(player, EnigmaticItems.CURSED_STONE);

			// Port: Forge's Inventory.compartments is private; the same three lists are named here.
			for (List<ItemStack> list : List.of(player.getInventory().items, player.getInventory().armor, player.getInventory().offhand)) {
				for (ItemStack itemstack : list) {
					if (!itemstack.isEmpty() && itemstack.getItem() == EnigmaticItems.CURSED_STONE) {
						itemstack.setCount(0);
					}
				}
			}
		}

		if (SuperpositionHandler.hasCurio(player, EnigmaticItems.ESCAPE_SCROLL)) {
			POSTMORTAL_POSESSIONS.put(player, EnigmaticItems.ESCAPE_SCROLL);

			ItemStack tomeStack = SuperpositionHandler.getCurioStack(player, EnigmaticItems.ESCAPE_SCROLL);
			PermanentItemEntity droppedTomeStack = new PermanentItemEntity(player.level(), player.getX(), player.getY() + (player.getBbHeight() / 2), player.getZ(), tomeStack.copy());
			droppedTomeStack.setPickupDelay(10);
			player.level().addFreshEntity(droppedTomeStack);

			tomeStack.shrink(1);
		}

		if (SuperpositionHandler.hasCurio(player, EnigmaticItems.ELDRITCH_AMULET) && SuperpositionHandler.isTheWorthyOne(player)) {
			EnigmaticItems.ELDRITCH_AMULET.storeInventory(player);
			POSTMORTAL_POSESSIONS.put(player, EnigmaticItems.ELDRITCH_AMULET);
		} else {
			for (int i = 0; i < player.getInventory().armor.size(); i++) {
				ItemStack armor = player.getInventory().armor.get(i);

				if (EnchantmentHelper.getItemEnchantmentLevel(EnigmaticEnchantments.ETERNAL_BINDING, armor) > 0) {
					player.getInventory().armor.set(i, ItemStack.EMPTY);
					SuperpositionHandler.setPersistentTag(player, "EternallyBoundArmor" + i, armor.save(new CompoundTag()));
				}
			}
		}

		destroyRingsInLavaPool(player);
	}

	/**
	 * Port: the lava-pool part of upstream's {@code onCurioDrops} (Curios DropRulesEvent). Trinkets
	 * asks for drop rules once per stack, so this runs once here, before the drops, and empties
	 * the ring slots directly instead of adding a DESTROY override.
	 */
	private static void destroyRingsInLavaPool(ServerPlayer player) {
		if (hadUnholyStone(player) && player.level().dimension() == Level.NETHER) {
			BlockPos deathPos = player.blockPosition();

			if (isThereLava(player.level(), deathPos)) {
				BlockPos surfacePos = deathPos;

				while (true) {
					BlockPos nextAbove = surfacePos.offset(0, 1, 0);
					if (isThereLava(player.level(), nextAbove)) {
						surfacePos = nextAbove;
						continue;
					} else {
						break;
					}
				}

				boolean confirmLavaPool = true;

				for(int i = -3; i <= 2; ++i) {
					final int fi = i;

					boolean checkArea = BlockPos.betweenClosedStream(surfacePos.offset(-3, i, -3), surfacePos.offset(3, i, 3))
							.map(blockPos -> {
								if (fi <= 0)
									return isThereLava(player.level(), blockPos);
								else
									return player.level().isEmptyBlock(blockPos) || isThereLava(player.level(), blockPos);
							})
							.reduce((prevResult, nextElement) -> {
								return prevResult && nextElement;
							}).orElse(false);

					confirmLavaPool = confirmLavaPool && checkArea;
				}

				if (confirmLavaPool) {
					EnigmaticItems.SOUL_CRYSTAL.setLostCrystals(player, EnigmaticItems.SOUL_CRYSTAL.getLostCrystals(player)+1);
					SuperpositionHandler.destroyCurio(player, EnigmaticItems.CURSED_RING);
					SuperpositionHandler.destroyCurio(player, EnigmaticItems.DESOLATION_RING);

					player.level().playSound(null, player.blockPosition(), SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 1.0F, 0.5F);
					SuperpositionHandler.setPersistentBoolean(player, "DestroyedCursedRing", true);
				}
			}
		}
	}

	private static boolean isThereLava(Level world, BlockPos pos) {
		FluidState fluidState = world.getBlockState(pos).getFluidState();
		if (fluidState != null && fluidState.is(FluidTags.LAVA) && fluidState.isSource())
			return true;
		else
			return false;
	}

	private static boolean hadUnholyStone(Player player) {
		return POSTMORTAL_POSESSIONS.containsKey(player) ? POSTMORTAL_POSESSIONS.containsEntry(player, EnigmaticItems.CURSED_STONE) : false;
	}

	/**
	 * Port: the Eldritch Amulet and Eternal Binding parts of upstream's {@code playerClone} ({@code PlayerEvent.Clone}).
	 * Called from EnigmaticLegacyFabric's COPY_FROM hook, after the persisted tag is copied.
	 */
	public static void onPlayerClone(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
		if (alive)
			return;

		if (!EnigmaticItems.ELDRITCH_AMULET.reclaimInventory(oldPlayer, newPlayer)) {
			for (int i = 0; i < 4; i++) {
				Tag tag = SuperpositionHandler.getPersistentTag(oldPlayer, "EternallyBoundArmor" + i, null);

				if (tag instanceof CompoundTag armor) {
					ItemStack stack = ItemStack.of(armor);
					newPlayer.getInventory().armor.set(i, stack);
				}

				SuperpositionHandler.removePersistentTag(newPlayer, "EternallyBoundArmor" + i);
			}
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

			if (player.getMainHandItem().is(EnigmaticItems.THE_INFINITUM) && SuperpositionHandler.isTheWorthyOne(player)) {
				lifesteal += amount * 0.1F;
			}

			if (SuperpositionHandler.isTheWorthyOne(player) && SuperpositionHandler.hasCurio(player, EnigmaticItems.ELDRITCH_AMULET)) {
				lifesteal += amount * 0.15F;
			}

			if (player.getMainHandItem().is(EnigmaticItems.ELDRITCH_PAN) && SuperpositionHandler.isTheWorthyOne(player)) {
				lifesteal += amount * EldritchPan.lifeSteal.getValue();
			}

			if (player.hasEffect(EnigmaticEffects.GROWING_BLOODLUST)) {
				int amplifier = 1 + player.getEffect(EnigmaticEffects.GROWING_BLOODLUST).getAmplifier();
				lifesteal += amount * (GrowingBloodlustEffect.lifestealBoost.getValue() * amplifier);
			}

			// TODO: Pact of Infinite Avarice term.

			if (lifesteal > 0) {
				player.heal(lifesteal);
			}

			float hungersteal = 0F;

			if (player.getMainHandItem().is(EnigmaticItems.ELDRITCH_PAN) && SuperpositionHandler.isTheWorthyOne(player)) {
				hungersteal += EldritchPan.hungerSteal.getValue();
			}

			if (hungersteal > 0) {
				boolean noHunger = SuperpositionHandler.cannotHunger(player);

				if (entity instanceof ServerPlayer victim) {
					FoodData victimFood = victim.getFoodData();
					FoodData attackerFood = player.getFoodData();

					int foodSteal = Math.min((int) Math.ceil(hungersteal), victimFood.getFoodLevel());
					float saturationSteal = Math.min(hungersteal / 5F, victimFood.getSaturationLevel());

					victimFood.setSaturation(victimFood.getSaturationLevel() - saturationSteal);
					victimFood.setFoodLevel(victimFood.getFoodLevel() - foodSteal);

					if (noHunger) {
						player.heal((float) foodSteal / 2);
					} else {
						attackerFood.eat(foodSteal, saturationSteal);
					}
				} else {
					if (noHunger) {
						player.heal(hungersteal / 2);
					} else {
						player.getFoodData().eat((int) Math.ceil(hungersteal), hungersteal / 5F);
					}
				}
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
	 * Port: the first-join gift and permadeath parts of upstream's {@code PlayerLoggedInEvent} handler. Fabric
	 * calls it from {@code ServerPlayConnectionEvents.JOIN}.
	 */
	public static void onPlayerJoin(ServerPlayer player) {
		if (SuperpositionHandler.isPermanentlyDead(player)) {
			Services.NETWORK.sendToPlayer(player, new PacketPermadeath());
			SuperpositionHandler.setCurrentWorldFractured(true);
		} else {
			SuperpositionHandler.setCurrentWorldFractured(false);
		}

		EnigmaticItems.FORBIDDEN_FRUIT.syncConsumedFruit(player);
		MagnetRing.syncMagnetEffects(player);
		EnigmaticItems.COSMIC_SCROLL.onPlayerJoin(player);
		EnigmaticItems.SOUL_CRYSTAL.updatePlayerSoulMap(player);

		if (EnigmaticItems.FORBIDDEN_FRUIT.haveConsumedFruit(player)) {
			ForbiddenFruitTrigger.INSTANCE.trigger(player);
		}

		/*
		 * Eh, annoying defaults.
		 */

		if (!SuperpositionHandler.hasPersistentTag(player, NBT_KEY_PATCHOULIFORCE)) {
			Services.NETWORK.sendToPlayer(player, new PacketPatchouliForce());
			SuperpositionHandler.setPersistentBoolean(player, NBT_KEY_PATCHOULIFORCE, true);
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

		/*
		 * Another one for Ring of the Seven Curses.
		 */

		if (OmniconfigHandler.isItemEnabled(EnigmaticItems.CURSED_RING))
			if (SuperpositionHandler.getPersistentTag(player, NBT_KEY_CURSEDGIFT, null) == null) {
				ItemStack cursedRingStack = new ItemStack(EnigmaticItems.CURSED_RING);

				if (!CursedRing.ultraHardcore.getValue()) {
					if (player.getInventory().getItem(7).isEmpty()) {
						player.getInventory().setItem(7, cursedRingStack);
					} else {
						if (!player.getInventory().add(cursedRingStack)) {
							ItemEntity dropRing = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), cursedRingStack);
							player.level().addFreshEntity(dropRing);
						}
					}
				} else {
					Services.ACCESSORIES.tryForceEquip(player, cursedRingStack);
				}

				SuperpositionHandler.setPersistentBoolean(player, NBT_KEY_CURSEDGIFT, true);
			}

		/*
		 * Handlers for fixing missing Curios slots upong joining the world.
		 */

		if (SuperpositionHandler.hasAdvancement(player, new ResourceLocation(EnigmaticLegacy.MODID, "main/discover_spellstone"))) {
			SuperpositionHandler.unlockSpecialSlot("spellstone", player);
			SuperpositionHandler.setPersistentBoolean(player, NBT_KEY_ENABLESPELLSTONE, true);
		}

		if (SuperpositionHandler.hasAdvancement(player, new ResourceLocation(EnigmaticLegacy.MODID, "main/discover_scroll"))) {
			SuperpositionHandler.unlockSpecialSlot("scroll", player);
			SuperpositionHandler.setPersistentBoolean(player, NBT_KEY_ENABLESCROLL, true);
		}
	}

	/**
	 * Port: upstream's onAdvancement (AdvancementEvent). Called from MixinPlayerAdvancements
	 * once the advancement is earned.
	 */
	public static void onAdvancement(ServerPlayer player, ResourceLocation advancement) {
		String id = advancement.toString();

		/*
		 * Specific handlers for Patchouli book entries.
		 */

		if (id.startsWith(EnigmaticLegacy.MODID + ":book/")) {
			Services.NETWORK.sendToPlayer(player, new PacketSetEntryState(false, id.replace("book/", "")));
		}

		/*
		 * Handler for permanently unlocking Curio slots to player once they obtain
		 * respective advancement.
		 */

		if (id.equals(EnigmaticLegacy.MODID + ":main/discover_spellstone")) {
			Services.NETWORK.sendToPlayer(player, new PacketSlotUnlocked("spellstone"));
			SuperpositionHandler.unlockSpecialSlot("spellstone", player);
			SuperpositionHandler.setPersistentBoolean(player, NBT_KEY_ENABLESPELLSTONE, true);
		} else if (id.equals(EnigmaticLegacy.MODID + ":main/discover_scroll")) {
			Services.NETWORK.sendToPlayer(player, new PacketSlotUnlocked("scroll"));
			SuperpositionHandler.unlockSpecialSlot("scroll", player);
			SuperpositionHandler.setPersistentBoolean(player, NBT_KEY_ENABLESCROLL, true);
		}
	}

	/**
	 * Port: the slot part of upstream's onPlayerRespawn (PlayerRespawnEvent). Fabric calls it
	 * from {@code ServerPlayerEvents.AFTER_RESPAWN}.
	 */
	public static void onPlayerRespawnSlots(ServerPlayer player, boolean endConquered) {
		if (!endConquered && !player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
			if (SuperpositionHandler.hasPersistentTag(player, NBT_KEY_ENABLESCROLL)) {
				SuperpositionHandler.unlockSpecialSlot("scroll", player);
			}
			if (SuperpositionHandler.hasPersistentTag(player, NBT_KEY_ENABLESPELLSTONE)) {
				SuperpositionHandler.unlockSpecialSlot("spellstone", player);
			}
		}
	}

	/**
	 * Port: upstream's {@code ItemCraftedEvent} handler. Called from {@code MixinResultSlot}.
	 */
	public static void onItemCrafted(Player player, ItemStack crafted, CraftingContainer inventory) {
		if (player != null && !player.level().isClientSide) {
			if ((inventory.countItem(EnigmaticItems.ENCHANTMENT_TRANSPOSER) == 1 || inventory.countItem(EnigmaticItems.CURSE_TRANSPOSER) == 1) && crafted.getItem() == Items.ENCHANTED_BOOK) {
				player.level().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, (float) (0.9F + (Math.random() * 0.1F)));
			} else if (crafted.getItem() == EnigmaticItems.CURSED_STONE) {
				player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, (float) (0.9F + (Math.random() * 0.1F)));
			}
		}
	}

	/**
	 * Port: the End city part of upstream's {@code LootTableLoadEvent} handler. Fabric calls it
	 * from {@code LootTableEvents.MODIFY}; {@code addPool} adds the pool to the table.
	 * Also adds the spellstone, "epic", literature (Revelation Tome), library and underwater ruin pools.
	 */
	public static void onLootTablesLoaded(ResourceLocation id, Consumer<LootPool> addPool) {
		if (!OmniconfigHandler.customDungeonLootEnabled.getValue())
			return;

		LootPool overworldLiterature = !OmniconfigHandler.isItemEnabled(EnigmaticItems.TATTERED_TOME) ? null :
			SuperpositionHandler.constructLootPool("overworldLiterature", -7F, 2F,
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.TATTERED_TOME, 100).apply(LootFunctionRevelation.of(UniformGenerator.between(1, 3), UniformGenerator.between(50, 500))));

		LootPool netherLiterature = !OmniconfigHandler.isItemEnabled(EnigmaticItems.WITHERED_TOME) ? null :
			SuperpositionHandler.constructLootPool("netherLiterature", -7F, 2F,
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.WITHERED_TOME, 100).apply(LootFunctionRevelation.of(UniformGenerator.between(1, 3), UniformGenerator.between(100, 700))));

		LootPool endLiterature = !OmniconfigHandler.isItemEnabled(EnigmaticItems.CORRUPTED_TOME) ? null :
			SuperpositionHandler.constructLootPool("endLiterature", -7F, 2F,
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.CORRUPTED_TOME, 100).apply(LootFunctionRevelation.of(UniformGenerator.between(1, 4), UniformGenerator.between(200, 1000))));

		if (id.equals(BuiltInLootTables.END_CITY_TREASURE)) {
			LootPool epic = SuperpositionHandler.constructLootPool("epic", 1F, 2F,
					LootItem.lootTableItem(Items.ENDER_PEARL).setWeight(40).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))),
					LootItem.lootTableItem(Items.ENDER_EYE).setWeight(20).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))),
					LootItem.lootTableItem(Items.GLISTERING_MELON_SLICE).setWeight(30).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))),
					LootItem.lootTableItem(Items.GOLDEN_CARROT).setWeight(30).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))),
					LootItem.lootTableItem(Items.PHANTOM_MEMBRANE).setWeight(25).apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 7))),
					LootItem.lootTableItem(Items.ENCHANTING_TABLE).setWeight(10),
					LootItem.lootTableItem(Items.CAKE).setWeight(15),
					LootItem.lootTableItem(Items.END_CRYSTAL).setWeight(7),
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.LORE_INSCRIBER, 10),
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.RECALL_POTION, 15),
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.MENDING_MIXTURE, 40),
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.ASTRAL_DUST, 85, 1F, 4F),
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.ETHERIUM_ORE, 60, 1F, 2F),
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.EXTRADIMENSIONAL_EYE, 20));

			addPool.accept(epic);

			if (endLiterature != null) {
				addPool.accept(endLiterature);
			}
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
			ItemStack fireResistancePotion = new ItemStack(Items.POTION);
			fireResistancePotion = PotionUtils.setPotion(fireResistancePotion, Potions.LONG_FIRE_RESISTANCE);

			LootPool epic = SuperpositionHandler.constructLootPool("epic", 1F, 2F,
					SuperpositionHandler.itemEntryBuilderED(Items.GOLDEN_PICKAXE, 10, 25F, 30F, 1.0F, 1.0F),
					SuperpositionHandler.itemEntryBuilderED(Items.GOLDEN_AXE, 10, 25F, 30F, 1.0F, 1.0F),
					SuperpositionHandler.itemEntryBuilderED(Items.GOLDEN_SWORD, 10, 25F, 30F, 1.0F, 1.0F),
					SuperpositionHandler.itemEntryBuilderED(Items.GOLDEN_SHOVEL, 10, 25F, 30F, 1.0F, 1.0F),
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.VOID_STONE, 8),
					LootItem.lootTableItem(Items.EMERALD).setWeight(30).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 7F))),
					LootItem.lootTableItem(Items.WITHER_ROSE).setWeight(25).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 4F))),
					LootItem.lootTableItem(Items.GHAST_TEAR).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2F))),
					LootItem.lootTableItem(Items.LAVA_BUCKET).setWeight(30),
					LootItem.lootTableItem(Items.POTION).setWeight(15).apply(SetNbtFunction.setTag(fireResistancePotion.getTag())),
					SuperpositionHandler.getBastionChests().contains(id) ? LootItem.lootTableItem(EnigmaticItems.FORBIDDEN_FRUIT).setWeight(4) : null
					);

			if (!id.equals(BuiltInLootTables.BASTION_TREASURE)) {
				addPool.accept(epic);
			} else {
				LootPool scroll = SuperpositionHandler.constructLootPool("darkest_scroll", 0F, 1F,
						LootItem.lootTableItem(EnigmaticItems.DARKEST_SCROLL).setWeight(100).apply(SetItemCountFunction.setCount(UniformGenerator.between(1F, 1F)))
						);

				addPool.accept(scroll);
			}

			if (netherLiterature != null) {
				addPool.accept(netherLiterature);
			}

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

		// Port: upstream also has a Desert Pyramid branch (Insignia pool and End literature), but it
		// sits after the overworld branch in an else-if chain and the pyramid is an overworld dungeon,
		// so it never runs upstream. It is left out here.
		if (SuperpositionHandler.getOverworldDungeons().contains(id)) {
			LootPool epic = SuperpositionHandler.constructLootPool("epic", 1F, 2F,
					SuperpositionHandler.itemEntryBuilderED(Items.IRON_PICKAXE, 10, 20F, 30F, 1.0F, 0.8F),
					SuperpositionHandler.itemEntryBuilderED(Items.IRON_AXE, 10, 20F, 30F, 1.0F, 0.8F),
					SuperpositionHandler.itemEntryBuilderED(Items.IRON_SWORD, 10, 20F, 30F, 1.0F, 0.8F),
					SuperpositionHandler.itemEntryBuilderED(Items.IRON_SHOVEL, 10, 20F, 30F, 1.0F, 0.8F),
					SuperpositionHandler.itemEntryBuilderED(Items.BOW, 10, 20F, 30F, 1.0F, 0.8F),
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.IRON_RING, 20),
					LootItem.lootTableItem(EnigmaticItems.COMMON_POTION).setWeight(20).apply(SetNbtFunction.setTag(PotionHelper.createAdvancedPotion(EnigmaticItems.COMMON_POTION, EnigmaticPotions.HASTE).getTag())),
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.MAGNET_RING, 8),
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.UNHOLY_GRAIL, 1),
					SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.LORE_INSCRIBER, 5),
					SuperpositionHandler.getWaterDungeons().contains(id) || id.equals(BuiltInLootTables.PILLAGER_OUTPOST) ? null : SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.EARTH_HEART, 7),
					LootItem.lootTableItem(Items.CLOCK).setWeight(10),
					LootItem.lootTableItem(Items.COMPASS).setWeight(10),
					LootItem.lootTableItem(Items.EMERALD).setWeight(20).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 4F))),
					LootItem.lootTableItem(Items.SLIME_BALL).setWeight(20).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 10F))),
					LootItem.lootTableItem(Items.LEATHER).setWeight(35).apply(SetItemCountFunction.setCount(UniformGenerator.between(3.0F, 8F))),
					LootItem.lootTableItem(Items.PUMPKIN_PIE).setWeight(25).apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0F, 16F))));

			addPool.accept(epic);

			if (overworldLiterature != null && !id.equals(BuiltInLootTables.SHIPWRECK_SUPPLY)) {
				addPool.accept(overworldLiterature);
			}
		}

		if (SuperpositionHandler.getLibraries().contains(id)) {
			LootPool special = SuperpositionHandler.constructLootPool("el_special", 2F, 3F,
					LootItem.lootTableItem(EnigmaticItems.THICC_SCROLL).setWeight(20).apply(SetItemCountFunction.setCount(UniformGenerator.between(2F, 6F))),
					LootItem.lootTableItem(EnigmaticItems.LORE_FRAGMENT).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(1F, 2F))));

			addPool.accept(special);

			if (OmniconfigHandler.isItemEnabled(EnigmaticItems.TATTERED_TOME)) {
				addPool.accept(SuperpositionHandler.constructLootPool("literature", -4F, 3F,
						SuperpositionHandler.createOptionalLootEntry(EnigmaticItems.TATTERED_TOME, 100).apply(LootFunctionRevelation.of(UniformGenerator.between(1, 3), UniformGenerator.between(50, 500)))));
			}
		}

		if (id.equals(BuiltInLootTables.UNDERWATER_RUIN_BIG) || id.equals(BuiltInLootTables.UNDERWATER_RUIN_SMALL)) {
			LootPool special = SuperpositionHandler.constructLootPool("el_special", -5F, 1F,
					LootItem.lootTableItem(Items.TRIDENT)
					.apply(SetItemDamageFunction.setDamage(UniformGenerator.between(0.5F, 1.0F)))
					.apply(EnchantWithLevelsFunction.enchantWithLevels(UniformGenerator.between(15F, 40F)).allowTreasure()));

			addPool.accept(special);
		}
	}

	/**
	 * Port: the arthropod part of upstream's {@code EntityJoinLevelEvent} handler. Fabric calls it
	 * from {@code ServerEntityEvents.ENTITY_LOAD}.
	 */
	public static void onEntityLoad(Entity entity) {
		if (entity instanceof PathfinderMob mob && mob.getMobType() == MobType.ARTHROPOD) {
			((AccessorMob) mob).enigmaticlegacy$getGoalSelector().addGoal(3, new AvoidEntityGoal<>(mob, Player.class,
					(targetEntity) -> targetEntity instanceof Player && SuperpositionHandler.hasAntiInsectAcknowledgement((Player) targetEntity),
					6, 1, 1.3, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test));
		}
	}

	/**
	 * Port: was the Forge {@code LivingGetProjectileEvent} handler (Ceaseless). Fabric calls it
	 * from MixinPlayer at every return of {@code Player.getProjectile}.
	 *
	 * @return the projectile stack to use.
	 */
	public static ItemStack onGetProjectile(ItemStack weapon, ItemStack arrows) {
		if (weapon.getItem() instanceof CrossbowItem && EnchantmentHelper.getItemEnchantmentLevel(EnigmaticEnchantments.CEASELESS, weapon) > 0) {
			if (arrows.isEmpty() && CeaselessEnchantment.allowNoArrow.getValue())
				return new ItemStack(Items.ARROW, 64);
			else if (arrows.getItem() instanceof ArrowItem) {
				ItemStack arrowsCopy = arrows.copy();
				arrowsCopy.setCount(64);
				return arrowsCopy;
			}
		}

		return arrows;
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
	 * player targets also go through {@link #onPlayerHurt}. Returning 0 stands in for
	 * {@code event.setCanceled(true)}.
	 *
	 * @return the new damage amount.
	 */
	public static float onLivingHurt(LivingEntity victim, DamageSource source, float amount) {
		if (source.getEntity() instanceof LivingEntity attacker) {
			Entity immediateSource = source.getDirectEntity();

			if (immediateSource instanceof ThrownTrident || immediateSource instanceof LivingEntity) {
				ItemStack mainhandStack = ItemStack.EMPTY;

				if (immediateSource instanceof ThrownTrident trident) {
					// Port: tridentItem is private in vanilla, so it goes through an accessor.
					ItemStack tridentItem = ((AccessorThrownTrident) trident).enigmaticlegacy$getTridentItem();

					if (tridentItem != null) {
						mainhandStack = tridentItem;
					}
				} else {
					mainhandStack = ((LivingEntity) immediateSource).getMainHandItem();
				}

				int torrentLevel = EnchantmentHelper.getItemEnchantmentLevel(EnigmaticEnchantments.TORRENT, mainhandStack);
				int wrathLevel = EnchantmentHelper.getItemEnchantmentLevel(EnigmaticEnchantments.WRATH, mainhandStack);

				if (torrentLevel > 0) {
					amount += EnigmaticEnchantments.TORRENT.bonusDamageByCreature(attacker, victim, torrentLevel);
				}

				if (wrathLevel > 0) {
					amount += EnigmaticEnchantments.WRATH.bonusDamageByCreature(attacker, victim, wrathLevel);
				}
			}
		}

		if (source.getEntity() instanceof Player attacker) {
			boolean bypass = source.getDirectEntity() == attacker && (attacker.getMainHandItem().is(EnigmaticItems.THE_TWIST)
					|| attacker.getMainHandItem().is(EnigmaticItems.THE_INFINITUM)
					|| attacker.getMainHandItem().is(EnigmaticItems.ELDRITCH_PAN));

			if (!bypass && (victim instanceof Monster || victim instanceof EnderDragon) && SuperpositionHandler.isTheCursedOne(attacker)) {
				amount *= CursedRing.monsterDamageDebuff.getValue().asModifierInverted();
			}

			float damageBoost = 0F;

			if (SuperpositionHandler.hasCurio(attacker, EnigmaticItems.BERSERK_CHARM)) {
				damageBoost += amount * (SuperpositionHandler.getMissingHealthPool(attacker) * (float) BerserkEmblem.attackDamage.getValue());
			}

			if (SuperpositionHandler.hasCurio(attacker, EnigmaticItems.CURSED_SCROLL)) {
				damageBoost += amount * (CursedScroll.damageBoost.getValue().asModifier() * SuperpositionHandler.getCurseAmount(attacker));
			}

			amount += damageBoost;

			if (source.getDirectEntity() == attacker && source.is(DamageTypes.PLAYER_ATTACK) && attacker.getMainHandItem().is(EnigmaticItems.THE_TWIST)
					&& SuperpositionHandler.isTheCursedOne(attacker) && OmniconfigHandler.isBossOrPlayer(victim)) {
				amount += amount * TheTwist.bossDamageBonus.getValue().asModifier(false);
			}

			if (source.getDirectEntity() == attacker && source.is(DamageTypes.PLAYER_ATTACK) && attacker.getMainHandItem().is(EnigmaticItems.THE_INFINITUM)
					&& SuperpositionHandler.isTheWorthyOne(attacker) && OmniconfigHandler.isBossOrPlayer(victim)) {
				amount += amount * TheInfinitum.bossDamageBonus.getValue().asModifier(false);
			}

			if (source.getDirectEntity() == attacker && source.is(DamageTypes.PLAYER_ATTACK) && attacker.getMainHandItem().is(EnigmaticItems.ENDER_SLAYER)
					&& SuperpositionHandler.isTheCursedOne(attacker)) {
				if (attacker instanceof ServerPlayer) {
					if (victim instanceof ServerPlayer targetPlayer) {
						targetPlayer.getCooldowns().addCooldown(Items.ENDER_PEARL, 400);
						targetPlayer.getCooldowns().addCooldown(EnigmaticItems.RECALL_POTION, 400);
						targetPlayer.getCooldowns().addCooldown(EnigmaticItems.TWISTED_MIRROR, 400);

						if (SuperpositionHandler.hasCurio(targetPlayer, EnigmaticItems.EYE_OF_NEBULA)
								|| SuperpositionHandler.hasCurio(targetPlayer, EnigmaticItems.THE_CUBE)) {
							SuperpositionHandler.setSpellstoneCooldown(targetPlayer, 400);
						}
					}

					if (victim instanceof EnderMan || victim instanceof Shulker) {
						EntityPersistentData.get(victim).putInt("ELTeleportBlock", 400);
					}
				}

				if (EnigmaticItems.ENDER_SLAYER.isEndDweller(victim)) {
					if (attacker.level().dimension().equals(Level.END)) {
						if (victim instanceof EnderMan && RegisteredMeleeAttack.getRegisteredAttackStregth(attacker) >= 1F) {
							amount = (amount + 100F) * 10F;
						}

						EntityPersistentData.get(victim).putBoolean("EnderSlayerVictim", true);
					}

					amount += amount * EnderSlayer.endDamageBonus.getValue().asModifier(false);
				}
			}

			if (source.getDirectEntity() == attacker && source.is(DamageTypes.PLAYER_ATTACK)) {
				ItemStack mainhandStack = attacker.getMainHandItem();
				int slayerLevel = EnchantmentHelper.getItemEnchantmentLevel(EnigmaticEnchantments.SLAYER, mainhandStack);

				if (slayerLevel > 0) {
					amount += EnigmaticEnchantments.SLAYER.bonusDamageByCreature(attacker, victim, slayerLevel);

					if (victim instanceof Monster) {
						int i = 20 + attacker.getRandom().nextInt(10 * slayerLevel);
						victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, i, 3));
					}
				}

				if (EnigmaticEnchantmentHelper.hasNemesisCurseEnchantment(mainhandStack)) {
					float supposedDamage = amount * 0.35F;

					attacker.hurt(attacker.damageSources().source(EnigmaticDamageTypes.NEMESIS_CURSE, victim), supposedDamage);
				}
			}

			if (SuperpositionHandler.hasCurio(attacker, EnigmaticItems.EYE_OF_NEBULA) && SuperpositionHandler.isMagic(source)) {
				amount *= EyeOfNebula.magicBoost.getValue().asModifier(true);
			}

			if (EyeOfNebula.EMPOWERED_PLAYERS.remove(attacker.getUUID())) {
				amount *= EyeOfNebula.attackEmpower.getValue().asModifier(true);
			}
		}

		if (victim instanceof TamableAnimal pet && pet.isTame()) {
			LivingEntity owner = pet.getOwner();

			if (owner instanceof Player && SuperpositionHandler.hasItem((Player)owner, EnigmaticItems.HUNTER_GUIDEBOOK)) {
				if (owner.level() == pet.level() && owner.distanceTo(pet) <= HunterGuidebook.effectiveDistance.getValue()) {
					owner.hurt(source, SuperpositionHandler.hasItem((Player)owner, EnigmaticItems.ANIMAL_GUIDEBOOK) ? (amount*HunterGuidebook.synergyDamageReduction.getValue().asModifierInverted()) : amount);
					return 0;
				}
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

		// Port: upstream onLivingHurtFinal, a LOWEST-priority LivingHurtEvent subscriber.
		// A 0 here means an earlier term canceled the hurt, and Forge skips canceled events.
		if (victim instanceof ServerPlayer player && source.getEntity() != null && amount > 0) {
			int sorrowLevel = 0;

			for (ItemStack armor : player.getInventory().armor) {
				sorrowLevel += EnchantmentHelper.getItemEnchantmentLevel(EnigmaticEnchantments.SORROW, armor);
			}

			for (int i = 0; i < sorrowLevel; i++) {
				EnigmaticEnchantments.SORROW.maybeApplyDebuff(player, amount);
			}
		}

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

		if (source.getEntity() instanceof ServerPlayer player && entity instanceof WitherBoss) {
			int killedWither = SuperpositionHandler.getPersistentInteger(player, "TimesKilledWither", 0);

			if (killedWither <= 0) {
				Quote.BREATHES_RELIEVED.play(player, 140);
				killedWither++;
			} else if (killedWither == 1) {
				Quote.APPALING_PRESENCE.play(player, 140);
				killedWither++;
			} else if (killedWither == 2) {
				Quote.TERRIFYING_FORM.play(player, 140);
				killedWither++;
			} else if (killedWither > 2 && killedWither < 5) {
				killedWither++;
			} else if (killedWither == 4) {
				Quote.WHETHER_IT_IS.play(player, 140);
				killedWither++;
			}

			SuperpositionHandler.setPersistentInteger(player, "TimesKilledWither", killedWither);
		}

		if (source.getDirectEntity() instanceof ServerPlayer attacker) {
			ItemStack weapon = attacker.getMainHandItem();

			if (weapon.is(EnigmaticItems.ELDRITCH_PAN)) {
				ResourceLocation killedType = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());

				if (EldritchPan.addKillIfNotPresent(weapon, killedType)) {
					attacker.sendSystemMessage(Component.translatable("message.enigmaticlegacy.eldritch_pan_buff")
							.withStyle(ChatFormatting.GOLD));
				}
			}
		}
	}

	/**
	 * Port: the quote and cursed-world parts of upstream's onPlayerRespawn (PlayerRespawnEvent).
	 * Fabric calls it from {@code ServerPlayerEvents.AFTER_RESPAWN}.
	 */
	public static void onPlayerRespawnQuotes(ServerPlayer player, boolean endConquered) {
		if (!endConquered) {
			if (SuperpositionHandler.getPersistentBoolean(player, "DestroyedCursedRing", false)) {
				SuperpositionHandler.removePersistentTag(player, "DestroyedCursedRing");
				Quote.getRandom(Quote.RING_DESTRUCTION).playIfUnlocked(player, 10);
			} else if (THEY_SEE_ME_ROLLIN.nextDouble() > 0.2) {
				if (SuperpositionHandler.getPersistentBoolean(player, "DeathFromEntity", false)) {
					Quote.getRandom(Quote.DEATH_QUOTES_ENTITY).playIfUnlocked(player, 10);
				} else {
					Quote.getRandom(Quote.DEATH_QUOTES).playIfUnlocked(player, 10);
				}
			}

			SuperpositionHandler.setCurrentWorldCursed(SuperpositionHandler.isTheCursedOne(player));
		}
	}

	/**
	 * Port: upstream's onEndPortal (EndPortalActivatedEvent). Called from MixinEnderEyeItem.
	 */
	public static void onEndPortal(ServerPlayer player) {
		Quote.END_DOORSTEP.playOnceIfUnlocked(player, 40);
	}

	/**
	 * Port: upstream's onEntitySummon (SummonedEntityEvent). Called from MixinSummonedEntityTrigger.
	 */
	public static void onEntitySummon(ServerPlayer player, Entity summoned) {
		if (summoned instanceof WitherBoss) {
			Quote.COUNTLESS_DEAD.playOnceIfUnlocked(player, 20);
		} else if (summoned instanceof EnderDragon) {
			Quote.HORRIBLE_EXISTENCE.playOnceIfUnlocked(player, 100);
		}
	}

	/**
	 * Port: upstream's onEnteredBlock (EnterBlockEvent). Called from MixinEnterBlockTrigger.
	 */
	public static void onEnteredBlock(ServerPlayer player, BlockState state) {
		if (state.getBlock() == Blocks.END_GATEWAY) {
			Quote.I_WANDERED.playOnceIfUnlocked(player, 160);
		}
	}

	/**
	 * Port: the quote part of upstream's onPlayerTravel (PlayerChangedDimensionEvent).
	 */
	public static void onPlayerTravelQuotes(ServerPlayer player) {
		if (player.level().dimension() == Level.NETHER) {
			Quote.SULFUR_AIR.playOnceIfUnlocked(player, 240);
		} else if (player.level().dimension() == Level.END) {
			Quote.TORTURED_ROCKS.playOnceIfUnlocked(player, 240);
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
		onLivingDropsLowest(entity, source, drops, recentlyHit);
	}

	/** Upstream onLivingDrops (NORMAL), Pact of Infinite Avarice and Axe of Executioner parts. */
	private static void onLivingDropsNormal(LivingEntity entity, DamageSource source, Collection<ItemEntity> drops,
			int lootingLevel, boolean recentlyHit) {
		if (recentlyHit && source.getEntity() instanceof Player killer && SuperpositionHandler.isTheCursedOne(killer)) {
			if (SuperpositionHandler.hasCurio(killer, EnigmaticItems.AVARICE_SCROLL)
					&& THEY_SEE_ME_ROLLIN.nextInt(100) < AvariceScroll.emeraldChance.getValue()) {
				addDrop(entity, drops, new ItemStack(Items.EMERALD, 1));
			}

			if (entity instanceof EnderDragon) {
				if (SuperpositionHandler.isTheWorthyOne(killer)) {
					int heartsGained = SuperpositionHandler.getPersistentInteger(killer, "AbyssalHeartsGained", 0);

					if (heartsGained < 5) {
						((IAbyssalHeartBearer) entity).dropAbyssalHeart(killer);
					}
				}
			}

			if (CursedRing.enableSpecialDrops.getValue()) {
				addCursedRingDrops(entity, drops);
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
	 * Upstream onLivingDropsLowest: Escape Scroll, Extradimensional Vessel, Soul Crystal and soul
	 * loss parts, plus the Eldritch Pan cooking.
	 */
	private static void onLivingDropsLowest(LivingEntity entity, DamageSource source, Collection<ItemEntity> drops,
			boolean recentlyHit) {
		Level level = entity.level();

		/*
		 * Eldritch Pan cooking logic.
		 */

		if (source.getDirectEntity() instanceof ServerPlayer player)
			if (SuperpositionHandler.isTheWorthyOne(player))
				if (player.getMainHandItem().is(EnigmaticItems.ELDRITCH_PAN)
						|| player.getOffhandItem().is(EnigmaticItems.ELDRITCH_PAN))
					for (ItemEntity drop : new ArrayList<>(drops)) {
						ItemStack stack = drop.getItem();
						Optional<SmeltingRecipe> optional = level.getRecipeManager().getRecipeFor(
								RecipeType.SMELTING, new SimpleContainer(stack), level);

						optional.ifPresent(recipe -> {
							ItemStack result = recipe.getResultItem(level.registryAccess());

							if (!result.isEmpty()) {
								ItemStack cooked = result.copyWithCount(stack.getCount() * result.getCount());

								drops.remove(drop);
								addDrop(entity, drops, cooked);
							}
						});
					}

		// Ender Slayer XP conversion. Port: clearing the list stands in for upstream's event cancel.
		if (recentlyHit && source.getEntity() instanceof Player killer && SuperpositionHandler.isTheCursedOne(killer)
				&& entity instanceof EnderMan
				&& entity.level().dimension().equals(Level.END)
				&& EntityPersistentData.get(entity).getBoolean("EnderSlayerVictim")) {
			for (ItemEntity drop : drops) {
				if (drop.getItem().is(Items.ENDER_PEARL)) {
					entity.level().addFreshEntity(new ExperienceOrb(entity.level(), entity.getX(), entity.getY(), entity.getZ(), 10));
				} else if (drop.getItem().is(Items.ENDER_EYE)) {
					entity.level().addFreshEntity(new ExperienceOrb(entity.level(), entity.getX(), entity.getY(), entity.getZ(), 20));
				}
			}

			drops.clear();
		}

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
				BlockPos respawnPos = player.getRespawnPosition();
				ServerLevel respawnLevel = player.getServer().getLevel(player.getRespawnDimension());
				boolean isEndAnchor = false;

				// Port: null checks added; upstream assumes a respawn point is set.
				if (respawnPos != null && respawnLevel != null && respawnLevel.getBlockState(respawnPos).is(EnigmaticBlocks.END_ANCHOR)) {
					dimPoint = new DimensionalPosition(respawnPos.getX() + 0.5, respawnPos.getY() + 1.5,
							respawnPos.getZ() + 0.5, respawnLevel);
					isEndAnchor = true;
				}

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

			boolean hadCursedRing = POSTMORTAL_POSESSIONS.containsEntry(player, EnigmaticItems.CURSED_RING);

			if (hadEnigmaticAmulet(player) && !drops.isEmpty() && EnigmaticItems.ENIGMATIC_AMULET.isVesselEnabled()) {
				ItemStack soulCrystal = SuperpositionHandler.canDropSoulCrystal(player, hadCursedRing) ? EnigmaticItems.SOUL_CRYSTAL.createCrystalFrom(player) : null;
				ItemStack storageCrystal = EnigmaticItems.STORAGE_CRYSTAL.storeDropsOnCrystal(drops, player, soulCrystal);
				PermanentItemEntity droppedStorageCrystal = new PermanentItemEntity(dimPoint.world, dimPoint.getPosX(), dimPoint.getPosY() + 1.5, dimPoint.getPosZ(), storageCrystal);
				droppedStorageCrystal.setOwnerId(player.getUUID());
				dimPoint.world.addFreshEntity(droppedStorageCrystal);
				EnigmaticLegacy.LOGGER.info("Summoned Extradimensional Storage Crystal for " + player.getGameProfile().getName() + " at X: " + dimPoint.getPosX() + ", Y: " + dimPoint.getPosY() + ", Z: " + dimPoint.getPosZ());
				drops.clear();

				SoulArchive.getInstance().addItem(droppedStorageCrystal);
			} else if (SuperpositionHandler.canDropSoulCrystal(player, hadCursedRing)) {
				ItemStack soulCrystal = EnigmaticItems.SOUL_CRYSTAL.createCrystalFrom(player);
				PermanentItemEntity droppedSoulCrystal = new PermanentItemEntity(dimPoint.world, dimPoint.getPosX(), dimPoint.getPosY() + 1.5, dimPoint.getPosZ(), soulCrystal);
				droppedSoulCrystal.setOwnerId(player.getUUID());
				dimPoint.world.addFreshEntity(droppedSoulCrystal);
				EnigmaticLegacy.LOGGER.info("Teared Soul Crystal from " + player.getGameProfile().getName() + " at X: " + dimPoint.getPosX() + ", Y: " + dimPoint.getPosY() + ", Z: " + dimPoint.getPosZ());

				SoulArchive.getInstance().addItem(droppedSoulCrystal);
			}

			if (SuperpositionHandler.isPermanentlyDead(player)) {
				Services.NETWORK.sendToPlayer(player, new PacketPermadeath());
				SuperpositionHandler.setCurrentWorldFractured(true);
			}

			ResourceLocation soulLossAdvancement = new ResourceLocation(EnigmaticLegacy.MODID, "book/soul_loss");

			if (SuperpositionHandler.isAffectedBySoulLoss(player, hadCursedRing)) {
				SuperpositionHandler.grantAdvancement(player, soulLossAdvancement);
			} else if (SuperpositionHandler.hasAdvancement(player, soulLossAdvancement)) {
				SuperpositionHandler.revokeAdvancement(player, soulLossAdvancement);
			}

			POSTMORTAL_POSESSIONS.removeAll(player);
		} else if (entity instanceof Player) {
			POSTMORTAL_POSESSIONS.removeAll(entity);
		}
	}

	/** Upstream addDrop. The drop spawns with the rest of the list after the handlers run. */
	/**
	 * Ring of the Seven Curses special drops. Port: split out of upstream's onLivingDrops.
	 */
	private static void addCursedRingDrops(LivingEntity killed, Collection<ItemEntity> drops) {
		if (killed.getClass() == Shulker.class) {
			addDropWithChance(killed, drops, new ItemStack(EnigmaticItems.ASTRAL_DUST, 1), 20);
		} else if (killed.getClass() == Skeleton.class || killed.getClass() == Stray.class) {
			addDrop(killed, drops, getRandomSizeStack(Items.ARROW, 3, 15));
		} else if (killed.getClass() == Zombie.class || killed.getClass() == Husk.class) {
			addDropWithChance(killed, drops, getRandomSizeStack(Items.SLIME_BALL, 1, 3), 25);
		} else if (killed.getClass() == Spider.class || killed.getClass() == CaveSpider.class) {
			addDrop(killed, drops, getRandomSizeStack(Items.STRING, 2, 12));
		} else if (killed.getClass() == Guardian.class) {
			addDropWithChance(killed, drops, new ItemStack(Items.NAUTILUS_SHELL, 1), 15);
			addDrop(killed, drops, getRandomSizeStack(Items.PRISMARINE_CRYSTALS, 2, 5));
		} else if (killed instanceof ElderGuardian) {
			addDrop(killed, drops, getRandomSizeStack(Items.PRISMARINE_CRYSTALS, 4, 16));
			addDrop(killed, drops, getRandomSizeStack(Items.PRISMARINE_SHARD, 7, 28));
			addOneOf(killed, drops,
					new ItemStack(EnigmaticItems.GUARDIAN_HEART, 1),
					new ItemStack(Items.HEART_OF_THE_SEA, 1),
					new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1),
					new ItemStack(Items.ENDER_EYE, 1),
					EnchantmentHelper.enchantItem(killed.getRandom(), new ItemStack(Items.TRIDENT, 1), 25+THEY_SEE_ME_ROLLIN.nextInt(15), true));
		} else if (killed.getClass() == EnderMan.class) {
			addDropWithChance(killed, drops, getRandomSizeStack(Items.ENDER_EYE, 1, 2), 40);
		} else if (killed.getClass() == Blaze.class) {
			addDrop(killed, drops, getRandomSizeStack(Items.BLAZE_POWDER, 0, 5));
		} else if (killed.getClass() == ZombifiedPiglin.class) {
			addDropWithChance(killed, drops, getRandomSizeStack(Items.GOLD_INGOT, 1, 3), 40);
			addDropWithChance(killed, drops, getRandomSizeStack(Items.GLOWSTONE_DUST, 1, 7), 30);
		} else if (killed.getClass() == Witch.class) {
			addDropWithChance(killed, drops, new ItemStack(Items.GHAST_TEAR, 1), 30);
			addDropWithChance(killed, drops, getRandomSizeStack(Items.PHANTOM_MEMBRANE, 1, 3), 50);
		} else if (killed.getClass() == Pillager.class || killed.getClass() == Vindicator.class) {
			addDrop(killed, drops, getRandomSizeStack(Items.EMERALD, 0, 4));
		} else if (killed.getClass() == Villager.class) {
			addDrop(killed, drops, getRandomSizeStack(Items.EMERALD, 2, 6));
		} else if (killed.getClass() == Creeper.class) {
			addDrop(killed, drops, getRandomSizeStack(Items.GUNPOWDER, 4, 12));
		} else if (killed.getClass() == PiglinBrute.class) {
			addDropWithChance(killed, drops, new ItemStack(Items.NETHERITE_SCRAP, 1), 20);
		} else if (killed.getClass() == Evoker.class) {
			addDrop(killed, drops, new ItemStack(Items.TOTEM_OF_UNDYING, 1));
			addDrop(killed, drops, getRandomSizeStack(Items.EMERALD, 5, 20));
			addDropWithChance(killed, drops, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 1), 10);
			addDropWithChance(killed, drops, getRandomSizeStack(Items.ENDER_PEARL, 1, 3), 30);
			addDropWithChance(killed, drops, getRandomSizeStack(Items.BLAZE_ROD, 2, 4), 30);
			addDropWithChance(killed, drops, getRandomSizeStack(Items.EXPERIENCE_BOTTLE, 4, 10), 50);
		} else if (killed.getClass() == WitherSkeleton.class) {
			addDrop(killed, drops, getRandomSizeStack(Items.BLAZE_POWDER, 0, 3));
			addDropWithChance(killed, drops, new ItemStack(Items.GHAST_TEAR, 1), 20);
			addDropWithChance(killed, drops, new ItemStack(Items.NETHERITE_SCRAP, 1), 7);
		} else if (killed.getClass() == Ghast.class) {
			addDrop(killed, drops, getRandomSizeStack(Items.PHANTOM_MEMBRANE, 1, 4));
		} else if (killed.getClass() == Drowned.class) {
			addDropWithChance(killed, drops, getRandomSizeStack(Items.LAPIS_LAZULI, 1, 3), 30);
		} else if (killed.getClass() == Vex.class) {
			addDrop(killed, drops, getRandomSizeStack(Items.GLOWSTONE_DUST, 0, 2));
			addDropWithChance(killed, drops, new ItemStack(Items.PHANTOM_MEMBRANE, 1), 30);
		} else if (killed.getClass() == Phantom.class || killed.getClass() == Silverfish.class) {
			// NO-OP
		} else if (killed.getClass() == Piglin.class) {
			addDropWithChance(killed, drops, getRandomSizeStack(Items.GOLD_INGOT, 2, 4), 50);
		} else if (killed.getClass() == Ravager.class) {
			addDrop(killed, drops, getRandomSizeStack(Items.EMERALD, 3, 10));
			addDrop(killed, drops, getRandomSizeStack(Items.LEATHER, 2, 7));
			addDropWithChance(killed, drops, getRandomSizeStack(Items.DIAMOND, 0, 4), 50);
		} else if (killed.getClass() == MagmaCube.class) {
			addDropWithChance(killed, drops, new ItemStack(Items.BLAZE_POWDER, 1), 50);
		} else if (killed.getClass() == Chicken.class) {
			addDropWithChance(killed, drops, new ItemStack(Items.EGG, 1), 50);
		} else if (killed instanceof WitherBoss) {
			addDrop(killed, drops, getRandomSizeStack(EnigmaticItems.EVIL_ESSENCE, 1, 4));
		}
	}

	private static void addDropWithChance(LivingEntity entity, Collection<ItemEntity> drops, ItemStack stack, int chance) {
		if (THEY_SEE_ME_ROLLIN.nextInt(100) < chance) {
			addDrop(entity, drops, stack);
		}
	}

	private static void addOneOf(LivingEntity entity, Collection<ItemEntity> drops, ItemStack... itemStacks) {
		int chosenStack = THEY_SEE_ME_ROLLIN.nextInt(itemStacks.length);
		addDrop(entity, drops, itemStacks[chosenStack]);
	}

	private static ItemStack getRandomSizeStack(Item item, int minAmount, int maxAmount) {
		return new ItemStack(item, minAmount + THEY_SEE_ME_ROLLIN.nextInt(maxAmount - minAmount + 1));
	}

	/**
	 * Port: upstream's LivingKnockBackEvent handler, Cursed Ring part. Called from
	 * MixinLivingEntity at the head of knockback, where Forge fires the event.
	 */
	public static double onLivingKnockback(LivingEntity entity, double strength) {
		Float knockbackPower = KNOCKBACK_THAT_BASTARD.remove(entity);

		if (knockbackPower != null) {
			strength *= knockbackPower;
		}

		if (entity instanceof Player && SuperpositionHandler.hasCurio(entity, EnigmaticItems.CURSED_RING)) {
			strength *= CursedRing.knockbackDebuff.getValue().asModifier();
		}

		return strength;
	}

	/**
	 * Port: upstream's LivingExperienceDropEvent handlers. The Monster Charm and Cursed Ring
	 * bonuses both come from the original amount, as in upstream.
	 */
	public static int onExperienceDrop(LivingEntity entity, Player player, int experience) {
		int dropped = MonsterCharm.modifyExperience(entity, player, experience);

		if (player != null && SuperpositionHandler.hasCurio(player, EnigmaticItems.CURSED_RING)) {
			dropped += (int) (experience * CursedRing.experienceBonus.getValue().asMultiplier());
		}

		return dropped;
	}

	/** Port: upstream's EnchantmentLevelSetEvent handler, as the bonus a table at this spot gets. */
	public static int getEnchantingBonus(Level level, BlockPos where) {
		int radius = 16;
		List<Player> players = level.getEntitiesOfClass(Player.class, new AABB(where.offset(-radius, -radius, -radius), where.offset(radius, radius, radius)));

		for (Player player : players)
			if (SuperpositionHandler.isTheCursedOne(player))
				return CursedRing.enchantingBonus.getValue();

		return 0;
	}

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

	private static boolean hadEnigmaticAmulet(Player player) {
		return POSTMORTAL_POSESSIONS.containsEntry(player, EnigmaticItems.ENIGMATIC_AMULET) || POSTMORTAL_POSESSIONS.containsEntry(player, EnigmaticItems.ASCENSION_AMULET);
	}

}
