package com.aizistral.enigmaticlegacy.handlers;

import java.util.Set;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;
import com.aizistral.enigmaticlegacy.api.items.ISpellstone;
import com.aizistral.enigmaticlegacy.items.generic.ItemSpellstoneCurio;
import com.aizistral.enigmaticlegacy.items.CursedRing;
import com.aizistral.enigmaticlegacy.registries.EnigmaticEnchantments;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Items;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.Projectile;
import com.aizistral.enigmaticlegacy.mixin.AccessorLargeFireball;
import com.aizistral.enigmaticlegacy.items.EldritchPan;
import java.util.List;
import java.io.File;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.api.capabilities.IPlaytimeCounter;
import com.aizistral.enigmaticlegacy.api.generic.ConfigurableItem;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.config.ConfigHolders;
import com.aizistral.enigmaticlegacy.config.OmniconfigHandler;
import com.aizistral.enigmaticlegacy.helpers.AdvancedSpawnLocationHelper;
import com.aizistral.enigmaticlegacy.objects.DimensionalPosition;
import com.aizistral.enigmaticlegacy.objects.EnigmaticTransience;
import com.aizistral.enigmaticlegacy.objects.PersistentData;
import com.aizistral.enigmaticlegacy.objects.Vector3;
import com.aizistral.enigmaticlegacy.packets.clients.PacketPortalParticles;
import com.aizistral.enigmaticlegacy.packets.clients.PacketRecallParticles;
import com.aizistral.enigmaticlegacy.packets.clients.PacketUpdateCompass;
import com.aizistral.enigmaticlegacy.platform.Services;
import com.aizistral.enigmaticlegacy.platform.network.TargetPoint;
import com.aizistral.omniconfig.Configuration;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.aizistral.enigmaticlegacy.api.generic.Tuple;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.UniformContainerBase;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import com.aizistral.enigmaticlegacy.items.GolemHeart;
import com.aizistral.enigmaticlegacy.items.InfernalShield;
import com.aizistral.enigmaticlegacy.registries.EnigmaticEffects;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.item.ItemUseAnimation;
import com.aizistral.enigmaticlegacy.items.TheAcknowledgment;
import java.util.Map.Entry;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Port of upstream's SuperpositionHandler. Methods are added as the code that calls them
 * is ported; port changes are marked with "Port:" comments.
 */
public class SuperpositionHandler {
	public static final Random RANDOM = new Random();
	public static final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz".toUpperCase().toCharArray();
	public static final char[] UPPERCASE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
	public static final char[] LOWERCASE_LETTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
	public static final char[] NUMBERS = "0123456789".toCharArray();
	public static final char[] SPECIAL_SYMBOLS = "-+=(){}[]':;./,<>*&^%$#@!?~".toCharArray();

	public static boolean areWeDedicatedServer() {
		return !Services.PLATFORM.isPhysicalClient();
	}

	/**
	 * Are we a remote server for specified player. Always true if we are
	 * a dedicated server; also true if we are integrated one but provided player
	 * does not host us.
	 * @param player Player in question
	 */

	public static boolean areWeRemoteServer(Player player) {
		if (areWeDedicatedServer())
			return true;
		else
			return player.level().getServer() != null && !player.level().getServer().isSingleplayerOwner(player.nameAndId());
	}

	public static void executeOnServer(Consumer<MinecraftServer> action) {
		MinecraftServer server = Services.PLATFORM.getServer();

		if (server != null) {
			action.accept(server);
		}
	}

	/**
	 * Port: Forge scan data is replaced by {@link ConfigHolders}, a fixed list of holder
	 * classes searched by reflection.
	 */
	private static Class<?> findHolder(String className) {
		try {
			return Class.forName(className, false, SuperpositionHandler.class.getClassLoader());
		} catch (ClassNotFoundException ex) {
			return null; // Not ported yet
		}
	}

	public static void dispatchWrapperToHolders(String modid, OmniconfigWrapper wrapper) {
		for (String className : ConfigHolders.CLASSES) {
			Class<?> retainerClass = findHolder(className);

			if (retainerClass == null) {
				continue;
			}

			for (Method method : retainerClass.getDeclaredMethods()) {
				SubscribeConfig annotation = method.getAnnotation(SubscribeConfig.class);

				if (annotation == null || !Modifier.isStatic(method.getModifiers())) {
					continue;
				}

				try {
					if (wrapper.config.getSidedType() != Configuration.SidedConfigType.CLIENT || annotation.receiveClient()) {
						method.setAccessible(true);
						method.invoke(null, wrapper);
					}
				} catch (Exception ex) {
					EnigmaticLegacy.LOGGER.error("Failed to dispatch config to " + className + "#" + method.getName());
					EnigmaticLegacy.LOGGER.catching(ex);
				}
			}
		}
	}

	public static Multimap<String, Field> retainAccessibilityGeneratorMap(String modid) {
		final Multimap<String, Field> accessibilityGeneratorMap = HashMultimap.create();

		for (String className : ConfigHolders.CLASSES) {
			Class<?> retainerClass = findHolder(className);

			if (retainerClass == null) {
				continue;
			}

			for (Field field : retainerClass.getDeclaredFields()) {
				ConfigurableItem annotation = field.getAnnotation(ConfigurableItem.class);

				if (annotation != null && !annotation.value().isEmpty()) {
					accessibilityGeneratorMap.put(annotation.value(), field);
				}
			}
		}

		return accessibilityGeneratorMap;
	}

	public static boolean hasItem(Player player, Item item) {
		// Port: Forge widens Inventory.compartments; Fabric uses the three public lists it holds.
		Inventory inventory = player.getInventory();
		for (List<ItemStack> list : inventoryCompartments(player)) {
			for (ItemStack itemstack : list) {
				if (!itemstack.isEmpty() && itemstack.is(item))
					return true;
			}
		}

		return false;
	}

	public static boolean hasExactStack(Player player, ItemStack stack) {
		// Port: same three lists as hasItem.
		Inventory inventory = player.getInventory();
		for (List<ItemStack> list : inventoryCompartments(player)) {
			for (ItemStack itemstack : list) {
				if (itemstack == stack)
					return true;
			}
		}

		return false;
	}

	public static Optional<Tuple<UUID, BlockPos>> updateSoulCompass(ServerPlayer player) {
		var optional = SoulArchive.getInstance().findNearest(player.level(), player.blockPosition());
		boolean noValid = optional.isEmpty();
		BlockPos pos = noValid ? BlockPos.ZERO : optional.get().getB();

		Services.NETWORK.sendToPlayer(player, new PacketUpdateCompass(pos.getX(), pos.getY(), pos.getZ(), noValid));
		EnigmaticEventHandler.LAST_SOUL_COMPASS_UPDATE.put(player, player.tickCount);
		return optional;
	}

	public static boolean hasCurio(LivingEntity entity, Item curio) {
		// Port: Curios lookup replaced by the accessory service (Trinkets on Fabric).
		return Services.ACCESSORIES.isEquipped(entity, curio);
	}

	public static void destroyCurio(LivingEntity entity, Item curio) {
		// Port: clears every matching accessory slot (upstream's Curios loop skipped the last slot).
		Services.ACCESSORIES.destroy(entity, curio);
	}

	/**
	 * Gets the first stack of the given curio the entity has equipped, or null if there is none.
	 */
	@Nullable
	public static ItemStack getCurioStack(LivingEntity entity, Item curio) {
		// Port: Curios lookup replaced by the accessory service (Trinkets on Fabric).
		return Services.ACCESSORIES.findFirst(entity, stack -> stack.is(curio));
	}

	public static boolean hasEnigmaticElytra(LivingEntity living) {
		return getEnigmaticElytra(living) != null;
	}

	@Nullable
	public static ItemStack getEnigmaticElytra(LivingEntity living) {
		ItemStack stack = living.getItemBySlot(EquipmentSlot.CHEST);

		if (stack.is(EnigmaticItems.ENIGMATIC_ELYTRA))
			return stack;
		else
			return getCurioStack(living, EnigmaticItems.ENIGMATIC_ELYTRA);
	}

	public static <T extends LivingEntity> T getClosestEntity(List<? extends T> entities, Predicate<LivingEntity> predicate, double x, double y, double z) {
		double d0 = -1.0D;
		T t = null;

		for (T t1 : entities) {
			if (predicate.test(t1)) {
				double d1 = t1.distanceToSqr(x, y, z);
				if (d0 == -1.0D || d1 < d0) {
					d0 = d1;
					t = t1;
				}
			}
		}

		return t;
	}

	public static boolean doesObserveEntity(Player player, LivingEntity entity) {
		Vec3 vector3d = player.getViewVector(1.0F).normalize();
		Vec3 vector3d1 = new Vec3(entity.getX() - player.getX(), entity.getEyeY() - player.getEyeY(), entity.getZ() - player.getZ());
		double d0 = vector3d1.length();
		vector3d1 = vector3d1.normalize();
		double d1 = vector3d.dot(vector3d1);
		return d1 > 1.0D - 0.025D / d0 ? player.hasLineOfSight(entity) : false;
	}

	public static double getRandomNegative() {
		return (Math.random()-0.5)*2.0;
	}

	/**
	 * Port: upstream registered through ForgeRegistries.SOUND_EVENTS; Fabric registers
	 * straight into the vanilla registry.
	 */
	public static SoundEvent registerSound(final String soundName) {
		Identifier location = Identifier.fromNamespaceAndPath("enigmaticlegacy", soundName);
		SoundEvent event = SoundEvent.createVariableRangeEvent(location);
		Registry.register(BuiltInRegistries.SOUND_EVENT, location, event);
		return event;
	}

	public static AABB getBoundingBoxAroundEntity(final Entity entity, final double radius) {
		return new AABB(entity.getX() - radius, entity.getY() - radius, entity.getZ() - radius, entity.getX() + radius, entity.getY() + radius, entity.getZ() + radius);
	}

	/**
	 * Builds standart loot pool with any amount of ItemLootEntries.
	 */

	public static LootPool constructLootPool(String poolName, float minRolls, float maxRolls, @Nullable LootPoolEntryContainer.Builder<?>... entries) {
		// Port: vanilla LootPool.Builder has no name (Forge adds it), so poolName is unused.
		LootPool.Builder poolBuilder = LootPool.lootPool();
		poolBuilder.setRolls(ContextIntProviders.between(Math.round(minRolls), Math.round(maxRolls)));

		for (LootPoolEntryContainer.Builder<?> entry : entries) {
			if (entry != null) {
				poolBuilder.add(entry);
			}
		}

		return poolBuilder.build();
	}

	/**
	 * Creates LootItem builder for an item. If item is disabled in config,
	 * returns null instead. Count-sensitive version, allows you to specifiy min-max
	 * amounts of items generated per entry.
	 */

	@Nullable
	public static UniformContainerBase.Builder<?> createOptionalLootEntry(Item item, int weight, float minCount, float maxCount) {
		if (!OmniconfigHandler.isItemEnabled(item))
			return null;

		return LootItem.lootTableItem(item).setWeight(weight).apply(SetItemCountFunction.setCount(ContextIntProviders.between(Math.round(minCount), Math.round(maxCount))));
	}

	/**
	 * Creates LootItem builder for an item. If item is disabled in config,
	 * returns null instead.
	 */

	@Nullable
	public static UniformContainerBase.Builder<?> createOptionalLootEntry(Item item, int weight) {
		if (!OmniconfigHandler.isItemEnabled(item))
			return null;

		return LootItem.lootTableItem(item).setWeight(weight);
	}

	public static UniformContainerBase.Builder<?> itemEntryBuilderED(HolderGetter.Provider registries, Item item, int weight, float enchantLevelMin, float enchantLevelMax, float damageMin, float damageMax) {
		UniformContainerBase.Builder<?> builder = LootItem.lootTableItem(item);

		builder.setWeight(weight);
		builder.apply(SetItemDamageFunction.setDamage(ContextFloatProviders.between(damageMax, damageMin)));
		builder.apply(EnchantWithLevelsFunction.enchantWithLevels(registries.lookupOrThrow(Registries.ENCHANTMENT), ContextIntProviders.between(Math.round(enchantLevelMin), Math.round(enchantLevelMax))));

		return builder;
	}

	public static List<ResourceKey<LootTable>> getLibraries() {
		List<ResourceKey<LootTable>> lootChestList = new ArrayList<ResourceKey<LootTable>>();
		lootChestList.add(BuiltInLootTables.STRONGHOLD_LIBRARY);
		lootChestList.add(BuiltInLootTables.SHIPWRECK_MAP);

		return lootChestList;
	}

	public static List<ResourceKey<LootTable>> getOverworldDungeons() {
		List<ResourceKey<LootTable>> lootChestList = new ArrayList<ResourceKey<LootTable>>();
		lootChestList.add(BuiltInLootTables.SIMPLE_DUNGEON);
		lootChestList.add(BuiltInLootTables.ABANDONED_MINESHAFT);
		lootChestList.add(BuiltInLootTables.STRONGHOLD_CROSSING);
		lootChestList.add(BuiltInLootTables.STRONGHOLD_CORRIDOR);
		lootChestList.add(BuiltInLootTables.DESERT_PYRAMID);
		lootChestList.add(BuiltInLootTables.JUNGLE_TEMPLE);
		lootChestList.add(BuiltInLootTables.IGLOO_CHEST);
		lootChestList.add(BuiltInLootTables.WOODLAND_MANSION);
		lootChestList.add(BuiltInLootTables.UNDERWATER_RUIN_SMALL);
		lootChestList.add(BuiltInLootTables.UNDERWATER_RUIN_BIG);
		lootChestList.add(BuiltInLootTables.SHIPWRECK_SUPPLY);
		lootChestList.add(BuiltInLootTables.PILLAGER_OUTPOST);

		return lootChestList;
	}

	public static boolean hasAntiInsectAcknowledgement(Player player) {
		List<ItemStack> heldItems = Lists.newArrayList(player.getMainHandItem(), player.getOffhandItem());

		for (ItemStack held : heldItems) {
			if (held != null && held.getItem() instanceof TheAcknowledgment) {
				if (EnigmaticEnchantments.getLevel(Enchantments.BANE_OF_ARTHROPODS, held) > 0)
					return true;
			}
		}

		return false;
	}

	public static List<ResourceKey<LootTable>> getBastionChests() {
		List<ResourceKey<LootTable>> lootChestList = new ArrayList<ResourceKey<LootTable>>();
		lootChestList.add(BuiltInLootTables.BASTION_TREASURE);
		lootChestList.add(BuiltInLootTables.BASTION_OTHER);
		lootChestList.add(BuiltInLootTables.BASTION_BRIDGE);
		lootChestList.add(BuiltInLootTables.BASTION_HOGLIN_STABLE);

		return lootChestList;
	}

	public static List<ResourceKey<LootTable>> getNetherDungeons() {
		List<ResourceKey<LootTable>> netherList = new ArrayList<ResourceKey<LootTable>>();

		netherList.add(BuiltInLootTables.NETHER_BRIDGE);
		netherList.add(BuiltInLootTables.BASTION_TREASURE);
		netherList.add(BuiltInLootTables.BASTION_OTHER);
		netherList.add(BuiltInLootTables.BASTION_BRIDGE);
		netherList.add(BuiltInLootTables.BASTION_HOGLIN_STABLE);
		netherList.add(BuiltInLootTables.RUINED_PORTAL);

		return netherList;
	}

	public static List<ResourceKey<LootTable>> getEarthenDungeons() {
		List<ResourceKey<LootTable>> lootChestList = new ArrayList<ResourceKey<LootTable>>();

		lootChestList.add(BuiltInLootTables.SIMPLE_DUNGEON);
		lootChestList.add(BuiltInLootTables.ABANDONED_MINESHAFT);
		lootChestList.add(BuiltInLootTables.VILLAGE_ARMORER);

		return lootChestList;
	}

	public static List<ResourceKey<LootTable>> getWaterDungeons() {
		List<ResourceKey<LootTable>> lootChestList = new ArrayList<ResourceKey<LootTable>>();

		lootChestList.add(BuiltInLootTables.UNDERWATER_RUIN_BIG);
		lootChestList.add(BuiltInLootTables.UNDERWATER_RUIN_SMALL);
		lootChestList.add(BuiltInLootTables.SHIPWRECK_TREASURE);
		lootChestList.add(BuiltInLootTables.BURIED_TREASURE);

		return lootChestList;
	}

	public static List<ResourceKey<LootTable>> getAirDungeons() {
		List<ResourceKey<LootTable>> lootChestList = new ArrayList<ResourceKey<LootTable>>();

		lootChestList.add(BuiltInLootTables.VILLAGE_TEMPLE);

		return lootChestList;
	}

	public static List<ResourceKey<LootTable>> getEnderDungeons() {
		List<ResourceKey<LootTable>> lootChestList = new ArrayList<ResourceKey<LootTable>>();

		lootChestList.add(BuiltInLootTables.END_CITY_TREASURE);

		return lootChestList;
	}

	public static List<ResourceKey<LootTable>> getMergedAir$EarthenDungeons() {
		List<ResourceKey<LootTable>> lootChestList = new ArrayList<ResourceKey<LootTable>>();

		lootChestList.add(BuiltInLootTables.DESERT_PYRAMID);
		lootChestList.add(BuiltInLootTables.JUNGLE_TEMPLE);

		return lootChestList;
	}

	public static List<ResourceKey<LootTable>> getMergedEnder$EarthenDungeons() {
		List<ResourceKey<LootTable>> lootChestList = new ArrayList<ResourceKey<LootTable>>();

		lootChestList.add(BuiltInLootTables.STRONGHOLD_CORRIDOR);
		lootChestList.add(BuiltInLootTables.STRONGHOLD_CROSSING);

		return lootChestList;
	}

	public static boolean isMagic(DamageSource source) {
		return source.is(DamageTypes.MAGIC) || source.is(DamageTypes.WITHER) || source.is(DamageTypes.WITHER_SKULL)
				|| source.is(DamageTypes.DRAGON_BREATH) || source.is(DamageTypes.INDIRECT_MAGIC) || source.is(DamageTypes.THORNS);
	}

	public static boolean isTheBlessedOne(Player player) {
		if (EnigmaticLegacy.SOUL_OF_THE_ARCHITECT.equals(player.getUUID()))
			return true;
		else
			return DevotedBelieversHandler.isDevotedBeliever(player);
	}

	public static ItemStack maybeApplyEternalBinding(ItemStack stack, RegistryAccess registries) {
		if (Math.random() < 0.5)
			if (EnigmaticEnchantments.getLevel(Enchantments.BINDING_CURSE, stack) > 0) {
				Holder<Enchantment> eternalBinding = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(EnigmaticEnchantments.ETERNAL_BINDING);
				EnchantmentHelper.updateEnchantments(stack, map -> {
					int level = EnigmaticEnchantments.getLevel(Enchantments.BINDING_CURSE, stack);
					map.removeIf(holder -> holder.is(Enchantments.VANISHING_CURSE) || holder.is(Enchantments.BINDING_CURSE));
					map.set(eternalBinding, level);
				});
			}
		return stack;
	}

	public static ItemStack mergeEnchantments(ItemStack input, ItemStack mergeFrom, boolean overmerge, boolean onlyTreasure) {
		ItemStack returnedStack = input.copy();
		ItemEnchantments.Mutable inputEnchants = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(returnedStack));
		ItemEnchantments mergedEnchants = EnchantmentHelper.getEnchantmentsForCrafting(mergeFrom);

		for (Holder<Enchantment> mergedEnchant : mergedEnchants.keySet()) {
			if (mergedEnchant != null) {
				int inputEnchantLevel = inputEnchants.getLevel(mergedEnchant);
				int mergedEnchantLevel = mergedEnchants.getLevel(mergedEnchant);
				int maxLevel = mergedEnchant.value().getMaxLevel();

				if (!overmerge) {
					mergedEnchantLevel = inputEnchantLevel == mergedEnchantLevel ? (mergedEnchantLevel + 1 > maxLevel ? maxLevel : mergedEnchantLevel + 1) : Math.max(mergedEnchantLevel, inputEnchantLevel);
				} else {
					mergedEnchantLevel = inputEnchantLevel > 0 ? Math.max(mergedEnchantLevel, inputEnchantLevel) + 1 : Math.max(mergedEnchantLevel, inputEnchantLevel);
					mergedEnchantLevel = Math.min(mergedEnchantLevel, 10);
				}

				boolean compatible = mergedEnchant.value().canEnchant(input);
				if (input.is(Items.ENCHANTED_BOOK)) {
					compatible = true;
				}

				for (Holder<Enchantment> originalEnchant : inputEnchants.keySet()) {
					if (!originalEnchant.equals(mergedEnchant) && !Enchantment.areCompatible(mergedEnchant, originalEnchant)) {
						compatible = false;
					}
				}

				if (compatible) {
					if (!onlyTreasure || mergedEnchant.is(EnchantmentTags.TREASURE) || mergedEnchant.is(EnchantmentTags.CURSE)) {
						inputEnchants.set(mergedEnchant, mergedEnchantLevel);
					}
				}
			}
		}

		EnchantmentHelper.setEnchantments(returnedStack, inputEnchants.toImmutable());
		return returnedStack;
	}

	public static boolean cannotHunger(@Nullable Player player) {
		boolean noHunger = false;

		if (player != null) {
			if (EnigmaticItems.FORBIDDEN_FRUIT.haveConsumedFruit(player)) {
				noHunger = true;
			} else if (player.level().getDifficulty() == Difficulty.PEACEFUL) {
				noHunger = true;
			}
		}

		return noHunger;
	}

	public static boolean isTheCursedOne(Player player) {
		return Services.ACCESSORIES.isEquipped(player, EnigmaticItems.CURSED_RING);
	}

	public static boolean isTheWorthyOne(Player player) {
		if (isTheCursedOne(player)) {
			return getSufferingFraction(player) >= CursedRing.superCursedTime.getValue();
		} else
			return false;
	}

	public static boolean canUnequipBoundRelics(Player player) {
		return player.isCreative() || EnigmaticLegacy.SOUL_OF_THE_ARCHITECT.equals(player.getUUID());
	}

	/**
	 * Port: upstream's IProperShieldUser.isActuallyReallyBlocking, implemented by its
	 * LivingEntity mixin. Same check as vanilla isBlocking, without the 5-tick warm-up.
	 */
	public static boolean isActuallyReallyBlocking(LivingEntity entity) {
		if (entity.isUsingItem() && !entity.getUseItem().isEmpty())
			return entity.getUseItem().getUseAnimation() == ItemUseAnimation.BLOCK && entity.getTicksUsingItem() >= 0;
		else
			return false;
	}

	// Port: upstream's CallbackInfoReturnable parameter was unused and is dropped. Its piercing
	// arrow check is forced off upstream, so it is left out.
	public static boolean onDamageSourceBlocking(LivingEntity blocker, ItemStack useItem, DamageSource source) {
		if (blocker instanceof Player player && useItem != null) {
			if (!isActuallyReallyBlocking(blocker))
				return false;

			if (useItem.getItem() instanceof InfernalShield) {
				if (!source.is(DamageTypeTags.BYPASSES_SHIELD)) {
					Vec3 sourcePos = source.getSourcePosition();

					if (sourcePos != null) {
						Vec3 lookVec = blocker.getViewVector(1.0F);
						Vec3 sourceToSelf = sourcePos.vectorTo(blocker.position()).normalize();
						sourceToSelf = new Vec3(sourceToSelf.x, 0.0D, sourceToSelf.z);

						if (sourceToSelf.dot(lookVec) < 0.0D) {
							int strength = -1;

							if (player.hasEffect(EnigmaticEffects.BLAZING_STRENGTH)) {
								MobEffectInstance effectInstance = player.getEffect(EnigmaticEffects.BLAZING_STRENGTH);
								strength = effectInstance.getAmplifier();
								player.removeEffect(EnigmaticEffects.BLAZING_STRENGTH);
								strength = strength > 2 ? 2 : strength;
							}

							player.addEffect(new MobEffectInstance(EnigmaticEffects.BLAZING_STRENGTH, 1200, strength + 1, true, true));

							if (source.getDirectEntity() instanceof LivingEntity living && living.isAlive()) {
								if (!living.fireImmune() && !(living instanceof Guardian)) {
									StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();

									// Ensure that we will not be caught in any sort of StackOverflow because of thorns-like effects
									if (Arrays.stream(stacktrace).filter(element -> {
										return SuperpositionHandler.class.getName().equals(element.getClassName());
									}).count() < 2) {
										living.setInvulnerableTime(0);
										living.hurt(living.damageSources().source(DamageTypes.ON_FIRE, player), 4F);
										living.igniteForSeconds(4);
										EnigmaticEventHandler.KNOCKBACK_THAT_BASTARD.remove(living);
									}
								}
							}

							return true;
						}
					}
				}
			} else if (useItem.getItem() instanceof EldritchPan) {
				Entity projectile = source.getDirectEntity();

				if (!(projectile instanceof Projectile))
					return false;

				if (!source.is(DamageTypeTags.BYPASSES_SHIELD)) {
					Vec3 sourcePos = source.getSourcePosition();

					if (sourcePos != null) {
						Vec3 lookVec = blocker.getViewVector(1.0F);
						Vec3 sourceToSelf = sourcePos.vectorTo(blocker.position()).normalize();
						sourceToSelf = new Vec3(sourceToSelf.x, 0.0D, sourceToSelf.z);

						if (sourceToSelf.dot(lookVec) < 0.0D) {
							projectile.kill((ServerLevel) projectile.level());

							FoodData data = player.getFoodData();
							data.eat(4, 0.5F);

							player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
									SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F,
									player.level().getRandom().nextFloat() * 0.1F + 0.9F);

							player.gameEvent(GameEvent.EAT);

							if (projectile instanceof LargeFireball fireball) {
								// Port: Forge's access transformer -> AccessorLargeFireball.
								((AccessorLargeFireball) fireball).enigmaticlegacy$setExplosionPower(0);
							}

							if (player.level() instanceof ServerLevel level) {
								Vec3 angle = player.getLookAngle();
								angle.multiply(1, 0, 1).normalize().multiply(0.5, 0.5, 0.5);

								level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM,
										Items.FIRE_CHARGE), player.getX() + angle.x,
										player.getY() + player.getEyeHeight() - 0.1, player.getZ() + angle.z,
										10, 0.3D, 0.3D, 0.3D, 0.03D);
							}

							return true;
						}
					}
				}
			}
		}

		return false;
	}

	// Port: upstream also checks that the singleplayer profile name matches the client's
	// username. An integrated server always runs in the client's own JVM, so that check
	// (and its client proxy call) is dropped.
	public static Optional<MinecraftServer> getSingleplayerServer() {
		MinecraftServer server = Services.PLATFORM.getServer();

		if (server != null && server.isSingleplayer())
			return Optional.of(server);
		else
			return Optional.empty();
	}

	public static void setCurrentWorldCursed(boolean cursed) {
		getSingleplayerServer().ifPresent(server -> {
			File saveFolder = server.getWorldPath(LevelResource.ROOT).toFile();
			EnigmaticTransience transience = EnigmaticTransience.read(saveFolder);
			transience.setCursed(cursed);
			transience.write(saveFolder);
		});
	}

	public static void setCurrentWorldFractured(boolean fractured) {
		getSingleplayerServer().ifPresent(server -> {
			File saveFolder = server.getWorldPath(LevelResource.ROOT).toFile();
			EnigmaticTransience transience = EnigmaticTransience.read(saveFolder);
			transience.setPermanentlyDead(fractured);
			transience.write(saveFolder);
		});
	}

	public static boolean isWorldCursed(File world) {
		return EnigmaticTransience.read(world).isCursed();
	}

	public static boolean isWorldFractured(File world) {
		return EnigmaticTransience.read(world).isPermanentlyDead();
	}

	public static double getSufferingFraction(@Nullable Player player) {
		if (player == null)
			return 0;

		IPlaytimeCounter counter = Services.PLAYER_DATA.getPlaytimeCounter(player);

		if (counter == null)
			return 0;

		long timeWithRing = counter.getTimeWithCurses();
		long timeWithoutRing = counter.getTimeWithoutCurses();

		if (timeWithRing <= 0)
			return 0;
		else if (timeWithoutRing <= 0)
			return 1;

		if (timeWithRing > 100000 || timeWithoutRing > 100000) {
			timeWithRing = timeWithRing / 100;
			timeWithoutRing = timeWithoutRing / 100;

			if (timeWithRing <= 0)
				return 0;
			else if (timeWithoutRing <= 0)
				return 1;
		}

		double ringFraction = timeWithRing / (double) (timeWithoutRing + timeWithRing);
		return roundToPlaces(ringFraction, 3);
	}

	public static String getSufferingTime(@Nullable Player player) {
		double fraction = getSufferingFraction(player);
		double percent = roundToPlaces(100 * fraction, 1);

		if (percent % 1 == 0)
			return (int) percent + "%";
		else
			return percent + "%";
	}

	public static String getNoSufferingTime(@Nullable Player player) {
		double percent = 100 * (1.0 - getSufferingFraction(player));

		if (percent - Math.floor(percent) == 0)
			return (int) percent + "%";
		else
			return percent + "%";
	}

	public static double roundToPlaces(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(Double.toString(value));
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public static boolean isExplosion(DamageSource source) {
		return source.is(DamageTypeTags.IS_EXPLOSION);
	}

	public static String minimizeNumber(double num) {
		if (num % 1 == 0)
			return Integer.toString((int) num);
		else
			return Double.toString(num);
	}

	public static void setEntityMotionFromVector(final Entity entity, final Vector3 originalPosVector, final float modifier) {
		final Vector3 entityVector = Vector3.fromEntityCenter(entity);
		Vector3 finalVector = originalPosVector.subtract(entityVector);

		if (finalVector.mag() > 1.0) {
			finalVector = finalVector.normalize();
		}

		entity.setDeltaMovement(finalVector.x * modifier, finalVector.y * modifier, finalVector.z * modifier);
	}

	public static boolean canPickStack(Player player, ItemStack stack) {
		if (player.getInventory().getFreeSlot() >= 0)
			return true;
		else {
			List<ItemStack> allInventories = new ArrayList<ItemStack>();

			allInventories.addAll(player.getInventory().getNonEquipmentItems());
			allInventories.add(player.getOffhandItem());

			for (ItemStack invStack : allInventories) {
				if (SuperpositionHandler.canMergeStacks(invStack, stack, player.getInventory().getMaxStackSize()))
					return true;
			}
		}

		return false;
	}

	public static boolean canMergeStacks(ItemStack stack1, ItemStack stack2, int invStackLimit) {
		return !stack1.isEmpty() && SuperpositionHandler.stackEqualExact(stack1, stack2) && stack1.isStackable() && stack1.getCount() < stack1.getMaxStackSize() && stack1.getCount() < invStackLimit;
	}

	/**
	 * Checks item, NBT, and meta if the item is not damageable
	 */
	public static boolean stackEqualExact(ItemStack stack1, ItemStack stack2) {
		return stack1.getItem() == stack2.getItem() && ItemStack.isSameItemSameComponents(stack1, stack2);
	}

	/**
	 * @return Multiplier for amount of particles based on client settings.
	 */
	@Environment(EnvType.CLIENT)
	public static float getParticleMultiplier() {
		if (Minecraft.getInstance().options.particles().get() == ParticleStatus.MINIMAL)
			return 0.35F;
		else if (Minecraft.getInstance().options.particles().get() == ParticleStatus.DECREASED)
			return 0.65F;
		else
			return 1.0F;

	}

	public static ServerLevel getOverworld() {
		return SuperpositionHandler.getWorld(Level.OVERWORLD);
	}

	public static ServerLevel getNether() {
		return SuperpositionHandler.getWorld(Level.NETHER);
	}

	public static ServerLevel getEnd() {
		return SuperpositionHandler.getWorld(Level.END);
	}

	@SafeVarargs
	public static <T> T getRandomElement(List<T> list, T... excluding) {
		List<T> newList = new ArrayList<>(list);
		newList.removeAll(Arrays.asList(excluding));

		if (newList.isEmpty())
			throw new IllegalArgumentException("List has no valid elements to choose");
		else if (newList.size() == 1)
			return newList.get(0);
		else
			return newList.get(RANDOM.nextInt(newList.size()));
	}

	// Port: upstream walks ChunkMap.visibleChunkMap, which Fabric cannot reach. Beacon range
	// tops out at 50 blocks, so scanning loaded chunks within 4 chunks of the player covers it.
	public static boolean isInBeaconRange(Player player) {
		if (!(player.level() instanceof ServerLevel level))
			return false;

		int cx = player.chunkPosition().x(), cz = player.chunkPosition().z();

		for (int x = cx - 4; x <= cx + 4; x++) {
			for (int z = cz - 4; z <= cz + 4; z++) {
				net.minecraft.world.level.chunk.LevelChunk chunk = level.getChunkSource().getChunkNow(x, z);

				if (chunk == null) {
					continue;
				}

				for (net.minecraft.world.level.block.entity.BlockEntity tile : chunk.getBlockEntities().values()) {
					if (tile instanceof net.minecraft.world.level.block.entity.BeaconBlockEntity beacon) {
						int levels = ((com.aizistral.enigmaticlegacy.mixin.AccessorBeaconBlockEntity) beacon).enigmaticlegacy$getLevels();

						if (levels <= 0 || beacon.getBeamSections().isEmpty()) {
							continue;
						}

						int range = (levels + 1) * 10;
						double distance = Math.sqrt(beacon.getBlockPos().distToCenterSqr(player.getX(), beacon.getBlockPos().getY(), player.getZ()));

						if (distance <= range)
							return true;
					}
				}
			}
		}

		return false;
	}

	public static ServerLevel getWorld(ResourceKey<Level> key) {
		return Services.PLATFORM.getServer().getLevel(key);
	}

	/**
	 * Port: was {@code EnigmaticLegacy.PROXY.isInVanillaDimension}. Both proxies did the same
	 * dimension check, so it lives here and works on either side.
	 */
	public static boolean isInVanillaDimension(Player player) {
		ResourceKey<Level> dimension = player.level().dimension();
		return dimension.equals(Level.OVERWORLD) || dimension.equals(Level.NETHER) || dimension.equals(Level.END);
	}

	/**
	 * Port: upstream added the Curios "Masterslot" modifier itself; the port goes through
	 * the accessory service (Trinkets on Fabric). One modifier id per slot type, so unlocks stay one per slot type.
	 */
	public static boolean unlockSpecialSlot(String slot, Player player) {
		if (!slot.equals("scroll") && !slot.equals("spellstone") && !slot.equals("ring") && !slot.equals("charm"))
			throw new IllegalArgumentException("Slot type '" + slot + "' is not supported!");

		return Services.ACCESSORIES.unlockSlot(player, slot, Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, "masterslot_" + slot));
	}

	/**
	 * Port: upstream used Forge's changeDimension(level, ITeleporter) with RealSmoothTeleporter,
	 * which kept the player's position. Vanilla teleportTo across levels does the same.
	 */
	public static void sendToDimension(ServerPlayer player, ResourceKey<Level> dimension) {
		if (!player.level().dimension().equals(dimension)) {
			ServerLevel world = SuperpositionHandler.getWorld(dimension);
			if (world != null) {
				player.teleportTo(world, player.getX(), player.getY(), player.getZ(), Set.of(), player.getYRot(), player.getXRot(), false);
			}
		}
	}

	public static ServerLevel backToSpawn(ServerPlayer serverPlayer) {
		ResourceKey<Level> respawnDimension = AdvancedSpawnLocationHelper.getPlayerRespawnDimension(serverPlayer);
		ServerLevel respawnWorld = SuperpositionHandler.getWorld(respawnDimension);

		serverPlayer.level().playSound(null, serverPlayer.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, (float) (0.8F + (Math.random() * 0.2)));

		Services.NETWORK.sendToNear(new TargetPoint(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), 128, serverPlayer.level().dimension()), new PacketPortalParticles(serverPlayer.getX(), serverPlayer.getY() + (serverPlayer.getBbHeight() / 2), serverPlayer.getZ(), 100, 1.25F, false));

		Optional<Vec3> vec = AdvancedSpawnLocationHelper.getValidSpawn(respawnWorld, serverPlayer);
		Optional<Vec3> vec2;
		ServerLevel destinationWorld = vec.isPresent() ? respawnWorld : serverPlayer.level().getServer().overworld();

		if (!serverPlayer.level().equals(destinationWorld)) {
			// Port: upstream used Forge's changeDimension(level, ITeleporter) with RealSmoothTeleporter,
			// which only kept the player's position (no portal search). Vanilla teleportTo across
			// levels does the same; the exact spot is set right below either way.
			serverPlayer.teleportTo(destinationWorld, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), Set.of(), serverPlayer.getYRot(), serverPlayer.getXRot(), false);
		}

		if (!respawnWorld.equals(destinationWorld)) {
			vec2 = AdvancedSpawnLocationHelper.getValidSpawn(destinationWorld, serverPlayer);
		} else {
			vec2 = Optional.empty();
		}

		if (vec.isPresent()) {
			Vec3 trueVec = vec.get();
			serverPlayer.teleportTo(trueVec.x, trueVec.y, trueVec.z);
		} else if (vec2.isPresent()) {
			Vec3 trueVec = vec2.get();
			serverPlayer.teleportTo(trueVec.x, trueVec.y, trueVec.z);
		} else {
			AdvancedSpawnLocationHelper.fuckBackToSpawn(serverPlayer.level(), serverPlayer);
		}

		serverPlayer.level().playSound(null, serverPlayer.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, (float) (0.8F + (Math.random() * 0.2)));

		Services.NETWORK.sendToNear(new TargetPoint(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), 128, serverPlayer.level().dimension()), new PacketRecallParticles(serverPlayer.getX(), serverPlayer.getY() + (serverPlayer.getBbHeight() / 2), serverPlayer.getZ(), 48, false));

		return destinationWorld;
	}

	public static DimensionalPosition getRespawnPoint(ServerPlayer serverPlayer) {
		ResourceKey<Level> respawnDimension = AdvancedSpawnLocationHelper.getPlayerRespawnDimension(serverPlayer);
		ServerLevel respawnWorld = SuperpositionHandler.getWorld(respawnDimension);

		Optional<Vec3> currentDimensionRespawnCoords = AdvancedSpawnLocationHelper.getValidSpawn(respawnWorld, serverPlayer);
		Optional<Vec3> destinationDimensionRespawnCoords;
		ServerLevel destinationWorld = currentDimensionRespawnCoords.isPresent() ? respawnWorld : serverPlayer.level().getServer().overworld();

		if (!respawnWorld.equals(destinationWorld)) {
			destinationDimensionRespawnCoords = AdvancedSpawnLocationHelper.getValidSpawn(destinationWorld, serverPlayer);
		} else {
			destinationDimensionRespawnCoords = Optional.empty();
		}

		Vec3 trueVec;

		if (currentDimensionRespawnCoords.isPresent()) {
			trueVec = currentDimensionRespawnCoords.get();
		} else if (destinationDimensionRespawnCoords.isPresent()) {
			trueVec = destinationDimensionRespawnCoords.get();
		} else {
			trueVec = new Vec3(destinationWorld.getRespawnData().pos().getX() + 0.5, destinationWorld.getRespawnData().pos().getY() + 0.5, destinationWorld.getRespawnData().pos().getZ() + 0.5);

			while (!destinationWorld.getBlockState(BlockPos.containing(trueVec)).isAir() && trueVec.y < 255.0D) {
				trueVec = trueVec.add(0, 1D, 0);
			}
		}

		return new DimensionalPosition(trueVec.x, trueVec.y, trueVec.z, destinationWorld);
	}

	/**
	 * Port: upstream read the {@code PlayerPersisted} compound of Forge's getPersistentData();
	 * the port keeps that compound on the player through MixinPlayer.
	 */
	private static CompoundTag getPersistedCompound(Player player) {
		return ((PersistentData) player).enigmaticlegacy$getPersistedTag();
	}

	/**
	 * Retrieves the given Tag from the player's persistent NBT, or expectedValue if it is missing.
	 */
	public static Tag getPersistentTag(Player player, String tag, Tag expectedValue) {
		CompoundTag persistent = getPersistedCompound(player);

		if (persistent.contains(tag))
			return persistent.get(tag);
		else
			return expectedValue;
	}

	/**
	 * Checks whether player has specified tag in their persistent NBT.
	 */
	public static boolean hasPersistentTag(Player player, String tag) {
		return getPersistedCompound(player).contains(tag);
	}

	/**
	 * Remove tag from the player's persistent NBT.
	 */
	public static void removePersistentTag(Player player, String tag) {
		getPersistedCompound(player).remove(tag);
	}

	/**
	 * Sets the given Tag type to the player's persistent NBT.
	 */
	public static void setPersistentTag(Player player, String tag, Tag value) {
		getPersistedCompound(player).put(tag, value);
	}

	/**
	 * Sets the given boolean tag to the player's persistent NBT.
	 */
	public static void setPersistentBoolean(Player player, String tag, boolean value) {
		SuperpositionHandler.setPersistentTag(player, tag, ByteTag.valueOf(value));
	}

	/**
	 * Retrieves the given boolean tag from the player's persistent NBT.
	 */
	public static boolean getPersistentBoolean(Player player, String tag, boolean expectedValue) {
		Tag theTag = SuperpositionHandler.getPersistentTag(player, tag, ByteTag.valueOf(expectedValue));
		return theTag instanceof ByteTag ? ((ByteTag) theTag).value() != 0 : expectedValue;
	}

	public static void setPersistentInteger(Player player, String tag, int value) {
		SuperpositionHandler.setPersistentTag(player, tag, IntTag.valueOf(value));
	}

	public static int getPersistentInteger(Player player, String tag, int expectedValue) {
		Tag theTag = SuperpositionHandler.getPersistentTag(player, tag, IntTag.valueOf(expectedValue));
		return theTag instanceof IntTag ? ((IntTag) theTag).value() : expectedValue;
	}

	public static boolean isAbsolute(DamageSource source) {
		return source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
	}

	/**
	 * Port: upstream keeps the spellstone cooldown in TransientPlayerData, which also
	 * syncs it to the client. The port only needs it server-side (the vanilla item
	 * cooldown packet shows it on the client), so it lives in this map.
	 */
	private static final Map<Player, Integer> SPELLSTONE_COOLDOWNS = new WeakHashMap<>();

	public static List<ItemStack> getAdvancedCurios(final LivingEntity entity) {
		// Port: Curios lookup replaced by the accessory service (Trinkets on Fabric).
		return Services.ACCESSORIES.findAll(entity, stack -> stack.getItem() instanceof ItemSpellstoneCurio);
	}

	public static boolean hasAdvancedCurios(final LivingEntity entity) {
		return SuperpositionHandler.getAdvancedCurios(entity).size() > 0;
	}

	public static boolean hasSpellstone(final LivingEntity entity) {
		return SuperpositionHandler.getSpellstone(entity) != null;
	}

	@Nullable
	public static ItemStack getSpellstone(final LivingEntity entity) {
		// Port: Curios lookup replaced by the accessory service (Trinkets on Fabric).
		for (ItemStack stack : Services.ACCESSORIES.getSlotStacks(entity, "spellstone")) {
			if (stack != null && stack.getItem() instanceof ISpellstone)
				return stack;
		}

		return null;
	}

	public static LivingEntity getObservedEntity(Player player, Level world, float range, int maxDist) {
		List<LivingEntity> entities = getObservedEntities(player, world, range, maxDist, true);
		return entities.size() > 0 ? entities.get(0) : null;
	}

	public static List<LivingEntity> getObservedEntities(Player player, Level world, float range, int maxDist, boolean stopWhenFound) {
		Vector3 target = Vector3.fromEntityCenter(player);
		List<LivingEntity> entities = new ArrayList<>();

		for (int distance = 1; distance < maxDist; ++distance) {
			target = target.add(new Vector3(player.getLookAngle()).multiply(distance)).add(0.0, 0.5, 0.0);
			List<LivingEntity> list = player.level().getEntitiesOfClass(LivingEntity.class, new AABB(target.x - range, target.y - range, target.z - range, target.x + range, target.y + range, target.z + range));
			list.removeIf(entity -> entity == player || !player.hasLineOfSight(entity));
			entities.addAll(list);

			if (stopWhenFound && entities.size() > 0) {
				break;
			}
		}

		return entities;
	}

	public static int getSpellstoneCooldown(Player player) {
		return SPELLSTONE_COOLDOWNS.getOrDefault(player, 0);
	}

	public static void setSpellstoneCooldown(Player player, int value) {
		int newValue = Math.max(value, 0);

		if (getSpellstoneCooldown(player) != newValue) {
			SPELLSTONE_COOLDOWNS.put(player, newValue);

			for (Item spellstone : EnigmaticItems.SPELLSTONES) {
				player.getCooldowns().addCooldown(new ItemStack(spellstone), newValue);
			}
		}
	}

	public static void tickSpellstoneCooldown(Player player, int decrementedTicks) {
		// Same as upstream: only the counter drops, the item cooldowns run out on their own.
		SPELLSTONE_COOLDOWNS.put(player, getSpellstoneCooldown(player) - decrementedTicks);
	}

	/**
	 * Checks whether player's spellstone cooldown is greater than zero.
	 */
	public static boolean hasSpellstoneCooldown(Player player) {
		return getSpellstoneCooldown(player) > 0;
	}

	public static void clearSpellstoneCooldowns() {
		SPELLSTONE_COOLDOWNS.clear();
	}

	public static boolean hasArchitectsFavor(Player player) {
		return isTheBlessedOne(player) && SuperpositionHandler.hasCurio(player, EnigmaticItems.COSMIC_SCROLL);
	}

	/** Port: copied from upstream; client-only callers. */
	@Environment(EnvType.CLIENT)
	public static void addPotionTooltip(List<MobEffectInstance> list, ItemStack itemIn, List<Component> lores, float durationFactor) {
		List<Pair<Holder<Attribute>, AttributeModifier>> list1 = Lists.newArrayList();
		// Port: 1.21 formatDuration needs the tick rate; this is client-only, so read it from the client level.
		float tickRate = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.tickRateManager().tickrate() : 20.0F;
		if (list.isEmpty()) {
			lores.add((Component.translatable("effect.none")).withStyle(ChatFormatting.GRAY));
		} else {
			for (MobEffectInstance effectinstance : list) {
				MutableComponent iformattabletextcomponent = Component.translatable(effectinstance.getDescriptionId());
				Holder<MobEffect> effect = effectinstance.getEffect();
				effect.value().createModifiers(effectinstance.getAmplifier(), (attribute, modifier) -> list1.add(new Pair<>(attribute, modifier)));

				if (effectinstance.getAmplifier() > 0) {
					iformattabletextcomponent.append(" ").append(Component.translatable("potion.potency." + effectinstance.getAmplifier()));
				}

				if (effectinstance.getDuration() > 20) {
					iformattabletextcomponent.append(" (").append(MobEffectUtil.formatDuration(effectinstance, durationFactor, tickRate)).append(")");
				}

				lores.add(iformattabletextcomponent.withStyle(effect.value().getCategory().getTooltipFormatting()));
			}
		}

		if (!list1.isEmpty()) {
			lores.add(CommonComponents.EMPTY);
			lores.add((Component.translatable("potion.whenDrank")).withStyle(ChatFormatting.DARK_PURPLE));

			for (Pair<Holder<Attribute>, AttributeModifier> pair : list1) {
				AttributeModifier attributemodifier2 = pair.getSecond();
				double d0 = attributemodifier2.amount();
				double d1;
				if (attributemodifier2.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_BASE && attributemodifier2.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
					d1 = attributemodifier2.amount();
				} else {
					d1 = attributemodifier2.amount() * 100.0D;
				}

				if (d0 > 0.0D) {
					lores.add((Component.translatable("attribute.modifier.plus." + attributemodifier2.operation().id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(pair.getFirst().value().getDescriptionId()))).withStyle(ChatFormatting.BLUE));
				} else if (d0 < 0.0D) {
					d1 = d1 * -1.0D;
					lores.add((Component.translatable("attribute.modifier.take." + attributemodifier2.operation().id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(pair.getFirst().value().getDescriptionId()))).withStyle(ChatFormatting.DARK_RED));
				}
			}
		}

	}

	/**
	 * Attempts to teleport entity at given coordinates, or nearest valid location
	 * on Y axis.
	 *
	 * @return True if successfull, false otherwise.
	 */

	public static boolean validTeleport(Entity entity, double x_init, double y_init, double z_init, Level world, int checkAxis) {
		int x = (int) x_init;
		int y = (int) y_init;
		int z = (int) z_init;

		BlockState block = world.getBlockState(new BlockPos(x, y - 1, z));

		if (!world.isEmptyBlock(new BlockPos(x, y - 1, z)) && block.canOcclude()) {
			for (int counter = 0; counter <= checkAxis; counter++) {
				if (!world.isEmptyBlock(new BlockPos(x, y + counter - 1, z)) && world.getBlockState(new BlockPos(x, y + counter - 1, z)).canOcclude() && world.isEmptyBlock(new BlockPos(x, y + counter, z)) && world.isEmptyBlock(new BlockPos(x, y + counter + 1, z))) {
					world.playSound(null, entity.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, (float) (0.8F + (Math.random() * 0.2D)));

					Services.NETWORK.sendToNear(new TargetPoint(entity.getX(), entity.getY(), entity.getZ(), 128, entity.level().dimension()), new PacketPortalParticles(entity.getX(), entity.getY() + (entity.getBbHeight() / 2), entity.getZ(), 72, 1.0F, false));

					if (entity instanceof ServerPlayer) {
						ServerPlayer player = (ServerPlayer) entity;
						player.teleportTo(x + 0.5, y + counter, z + 0.5);
					} else {
						((LivingEntity) entity).teleportTo(x + 0.5, y + counter, z + 0.5);
					}

					world.playSound(null, entity.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, (float) (0.8F + (Math.random() * 0.2D)));
					Services.NETWORK.sendToNear(new TargetPoint(entity.getX(), entity.getY(), entity.getZ(), 128, entity.level().dimension()), new PacketRecallParticles(entity.getX(), entity.getY() + (entity.getBbHeight() / 2), entity.getZ(), 48, false));
					return true;
				}
			}
		} else {
			for (int counter = 0; counter <= checkAxis; counter++) {
				if (!world.isEmptyBlock(new BlockPos(x, y - counter - 1, z)) && world.getBlockState(new BlockPos(x, y - counter - 1, z)).canOcclude() && world.isEmptyBlock(new BlockPos(x, y - counter, z)) && world.isEmptyBlock(new BlockPos(x, y - counter + 1, z))) {
					world.playSound(null, entity.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, (float) (0.8F + (Math.random() * 0.2D)));
					Services.NETWORK.sendToNear(new TargetPoint(entity.getX(), entity.getY(), entity.getZ(), 128, entity.level().dimension()), new PacketRecallParticles(entity.getX(), entity.getY() + (entity.getBbHeight() / 2), entity.getZ(), 48, false));

					if (entity instanceof ServerPlayer) {
						ServerPlayer player = (ServerPlayer) entity;
						player.teleportTo(x + 0.5, y - counter, z + 0.5);
					} else {
						((LivingEntity) entity).teleportTo(x + 0.5, y - counter, z + 0.5);
					}

					world.playSound(null, entity.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, (float) (0.8F + (Math.random() * 0.2D)));
					Services.NETWORK.sendToNear(new TargetPoint(entity.getX(), entity.getY(), entity.getZ(), 128, entity.level().dimension()), new PacketRecallParticles(entity.getX(), entity.getY() + (entity.getBbHeight() / 2), entity.getZ(), 48, false));
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Attempts to find valid location within given radius and teleport entity
	 * there.
	 *
	 * @return True if teleportation were successfull, false otherwise.
	 */
	public static boolean validTeleportRandomly(Entity entity, Level world, int radius) {
		int d = radius * 2;

		double x = entity.getX() + ((Math.random() - 0.5D) * d);
		double y = entity.getY() + ((Math.random() - 0.5D) * d);
		double z = entity.getZ() + ((Math.random() - 0.5D) * d);
		return SuperpositionHandler.validTeleport(entity, x, y, z, world, radius);
	}

	public static int getCurseAmount(ItemStack stack) {
		ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
		int totalCurses = 0;

		for (Holder<Enchantment> enchantment: enchantments.keySet()) {
			if (enchantment.is(EnchantmentTags.CURSE) && enchantments.getLevel(enchantment) > 0) {
				totalCurses+=1;
			}
		}

		if (stack.getItem() == EnigmaticItems.CURSED_RING) {
			totalCurses+=7;
		}

		return totalCurses;
	}

	public static float getMissingHealthPool(Player player) {
		return (player.getMaxHealth() - Math.min(player.getHealth(), player.getMaxHealth()))/player.getMaxHealth();
	}

	public static int getCurseAmount(Player player) {
		int count = 0;
		boolean ringCounted = false;

		for (ItemStack theStack : getFullEquipment(player)) {
			if (theStack != null) {
				if (theStack.getItem() != EnigmaticItems.CURSED_RING || !ringCounted) {
					count += getCurseAmount(theStack);

					if (theStack.getItem() == EnigmaticItems.CURSED_RING) {
						ringCounted = true;
					}
				}
			}
		}

		return count;
	}

	// Port: Curios stack handlers -> Services.ACCESSORIES.findAll.
	public static List<ItemStack> getFullEquipment(Player player) {
		List<ItemStack> equipmentStacks = Lists.newArrayList();

		equipmentStacks.add(player.getMainHandItem());
		equipmentStacks.add(player.getOffhandItem());
		equipmentStacks.addAll(getArmorStacks(player));
		equipmentStacks.addAll(Services.ACCESSORIES.findAll(player, stack -> true));

		return equipmentStacks;
	}

	/**
	 * Port: 26.3 has no LivingEntity#getArmorSlots; this returns the four humanoid armor stacks.
	 */
	public static List<ItemStack> getArmorStacks(LivingEntity entity) {
		List<ItemStack> stacks = new ArrayList<>(4);

		for (EquipmentSlot slot : EquipmentSlot.VALUES) {
			if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
				stacks.add(entity.getItemBySlot(slot));
			}
		}

		return stacks;
	}

	/**
	 * Port: 26.3 Inventory has no armor/offhand lists; upstream's armor list order (feet first).
	 */
	public static final EquipmentSlot[] ARMOR_SLOTS = { EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD };

	/**
	 * Port: a writable list view over equipment slots, standing in for the old Inventory.armor
	 * and Inventory.offhand lists.
	 */
	public static List<ItemStack> slotView(LivingEntity entity, EquipmentSlot... slots) {
		return new java.util.AbstractList<>() {
			@Override
			public ItemStack get(int index) {
				return entity.getItemBySlot(slots[index]);
			}

			@Override
			public ItemStack set(int index, ItemStack stack) {
				ItemStack old = entity.getItemBySlot(slots[index]);
				entity.setItemSlot(slots[index], stack);
				return old;
			}

			@Override
			public int size() {
				return slots.length;
			}
		};
	}

	/**
	 * Port: main inventory, armor and offhand stacks (the old Inventory compartments).
	 */
	public static List<List<ItemStack>> inventoryCompartments(Player player) {
		return List.of(player.getInventory().getNonEquipmentItems(), slotView(player, ARMOR_SLOTS),
				slotView(player, EquipmentSlot.OFFHAND));
	}

	/**
	 * Port: 26.3 ItemStack has no save/parseOptional; these go through ItemStack.OPTIONAL_CODEC.
	 */
	public static Tag saveStack(RegistryAccess access, ItemStack stack) {
		return ItemStack.OPTIONAL_CODEC.encodeStart(access.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), stack)
				.result().orElseGet(CompoundTag::new);
	}

	public static ItemStack parseStack(RegistryAccess access, Tag tag) {
		return ItemStack.OPTIONAL_CODEC.parse(access.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), tag)
				.result().orElse(ItemStack.EMPTY);
	}

	public static boolean hasAnyArmor(LivingEntity entity) {
		int armorAmount = 0;

		for (ItemStack stack : getArmorStacks(entity)) {
			if (!stack.isEmpty() && !GolemHeart.EXCLUDED_ARMOR.stream().anyMatch(stack::is)) {
				armorAmount++;
			}
		}

		if (armorAmount != 0)
			return true;
		else
			return false;
	}

	/**
	 * Checks whether or not player has completed specified advancement.
	 */

	public static boolean hasAdvancement(ServerPlayer player, Identifier location) {
		try {
			if (player.getAdvancements().getOrStartProgress(player.level().getServer().getAdvancements().get(location)).isDone())
				return true;
		} catch (NullPointerException ex) {
			// Just don't do it lol }
		}

		return false;
	}

	/**
	 * Grant specified advancement to specified player.
	 * Don't forget to specify!
	 */

	public static void grantAdvancement(ServerPlayer player, Identifier location) {
		AdvancementHolder adv = player.level().getServer().getAdvancements().get(location);

		for (String criterion : player.getAdvancements().getOrStartProgress(adv).getRemainingCriteria()) {
			player.getAdvancements().award(adv, criterion);
		}

	}

	public static void revokeAdvancement(ServerPlayer player, Identifier location) {
		AdvancementHolder adv = player.level().getServer().getAdvancements().get(location);

		for (String criterion : player.getAdvancements().getOrStartProgress(adv).getCompletedCriteria()) {
			player.getAdvancements().revoke(adv, criterion);
		}
	}

	public static boolean isPermanentlyDead(Player player) {
		// TODO Proper permadeath screen, Cursed Mode
		return EnigmaticItems.SOUL_CRYSTAL.getLostCrystals(player) >= 10
				&& OmniconfigHandler.maxSoulCrystalLoss.getValue() >= 10;
	}

	public static boolean isAffectedBySoulLoss(Player player, boolean hadRing) {
		int dropMode = OmniconfigHandler.soulCrystalsMode.getValue(); // TODO Use Enum config
		boolean keepInventory = ((ServerLevel) player.level()).getGameRules().get(GameRules.KEEP_INVENTORY);

		if (dropMode == 0)
			return hadRing;
		else if (dropMode == 1)
			return (hadRing || keepInventory);
		else if (dropMode == 2)
			return true;
		else
			return false;
	}

	public static boolean canDropSoulCrystal(Player player, boolean hadRing) {
		if (isAffectedBySoulLoss(player, hadRing)) {
			int maxCrystalLoss = OmniconfigHandler.maxSoulCrystalLoss.getValue();
			return EnigmaticItems.SOUL_CRYSTAL.getLostCrystals(player) < maxCrystalLoss;
		} else
			return false;
	}

	private static boolean contains(char[] array, char ch) {
		for (char ach : array) {
			if (ach == ch)
				return true;
		}

		return false;
	}

	@Environment(EnvType.CLIENT)
	public static void obscureTooltip(List<Component> tooltip) {
		tooltip.replaceAll(component -> Component.literal(obscureString(component.getString())).withStyle(component.getStyle()));
	}

	public static String obscureString(String string) {
		char[] oldArray = string.toCharArray();
		char[] newArray = new char[oldArray.length];
		boolean code = false;

		for (int i = 0; i < oldArray.length; i++) {
			char ch = oldArray[i];
			newArray[i] = ch;

			if (ch == '§') {
				code = true;
				continue;
			} else if (code) {
				code = false;
				continue;
			} else if (ch == ' ') {
				continue;
			}

			char[] replacements = null;

			if (contains(UPPERCASE_LETTERS, ch)) {
				replacements = UPPERCASE_LETTERS;
			} else if (contains(LOWERCASE_LETTERS, ch)) {
				replacements = LOWERCASE_LETTERS;
			} else if (contains(NUMBERS, ch)) {
				replacements = NUMBERS;
			} else {
				replacements = SPECIAL_SYMBOLS;
			}

			ch = replacements[RANDOM.nextInt(replacements.length)];
			newArray[i] = ch;
		}

		return new String(newArray);
	}
}
