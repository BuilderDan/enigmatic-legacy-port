package com.aizistral.enigmaticlegacy.handlers;

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
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.api.capabilities.IPlaytimeCounter;
import com.aizistral.enigmaticlegacy.api.generic.ConfigurableItem;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.config.ConfigHolders;
import com.aizistral.enigmaticlegacy.config.OmniconfigHandler;
import com.aizistral.enigmaticlegacy.helpers.AdvancedSpawnLocationHelper;
import com.aizistral.enigmaticlegacy.objects.DimensionalPosition;
import com.aizistral.enigmaticlegacy.objects.PersistentData;
import com.aizistral.enigmaticlegacy.objects.Vector3;
import com.aizistral.enigmaticlegacy.packets.clients.PacketPortalParticles;
import com.aizistral.enigmaticlegacy.packets.clients.PacketRecallParticles;
import com.aizistral.enigmaticlegacy.platform.Services;
import com.aizistral.enigmaticlegacy.platform.network.TargetPoint;
import com.aizistral.omniconfig.Configuration;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import com.aizistral.enigmaticlegacy.items.GolemHeart;
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
	public static final UUID SCROLL_SLOT_UUID = UUID.fromString("ae465e52-ffc2-4f57-b09a-066aa0cea3d4");
	public static final UUID SPELLSTONE_SLOT_UUID = UUID.fromString("63df175a-0d6d-4163-8ef1-218bcb42feba");
	public static final UUID RING_SLOT_UUID = UUID.fromString("76012386-aa31-4c17-8d6a-e9dd29affcb0");
	public static final UUID CHARM_SLOT_UUID = UUID.fromString("485121e7-b670-45dc-b014-4c8b8f62283d");
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
			return player.getServer() != null && !player.getServer().isSingleplayerOwner(player.getGameProfile());
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
		for (List<ItemStack> list : List.of(inventory.items, inventory.armor, inventory.offhand)) {
			for (ItemStack itemstack : list) {
				if (!itemstack.isEmpty() && itemstack.is(item))
					return true;
			}
		}

		return false;
	}

	public static boolean hasCurio(LivingEntity entity, Item curio) {
		// Port: Curios lookup replaced by the accessory service (Trinkets on Fabric).
		return Services.ACCESSORIES.isEquipped(entity, curio);
	}

	/**
	 * Gets the first stack of the given curio the entity has equipped, or null if there is none.
	 */
	@Nullable
	public static ItemStack getCurioStack(LivingEntity entity, Item curio) {
		// Port: Curios lookup replaced by the accessory service (Trinkets on Fabric).
		return Services.ACCESSORIES.findFirst(entity, stack -> stack.is(curio));
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
		poolBuilder.setRolls(UniformGenerator.between(minRolls, maxRolls));

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
	public static LootPoolSingletonContainer.Builder<?> createOptionalLootEntry(Item item, int weight, float minCount, float maxCount) {
		if (!OmniconfigHandler.isItemEnabled(item))
			return null;

		return LootItem.lootTableItem(item).setWeight(weight).apply(SetItemCountFunction.setCount(UniformGenerator.between(minCount, maxCount)));
	}

	/**
	 * Creates LootItem builder for an item. If item is disabled in config,
	 * returns null instead.
	 */

	@Nullable
	public static LootPoolSingletonContainer.Builder<?> createOptionalLootEntry(Item item, int weight) {
		if (!OmniconfigHandler.isItemEnabled(item))
			return null;

		return LootItem.lootTableItem(item).setWeight(weight);
	}

	public static LootPoolSingletonContainer.Builder<?> itemEntryBuilderED(Item item, int weight, float enchantLevelMin, float enchantLevelMax, float damageMin, float damageMax) {
		LootPoolSingletonContainer.Builder<?> builder = LootItem.lootTableItem(item);

		builder.setWeight(weight);
		builder.apply(SetItemDamageFunction.setDamage(UniformGenerator.between(damageMax, damageMin)));
		builder.apply(EnchantWithLevelsFunction.enchantWithLevels(UniformGenerator.between(enchantLevelMin, enchantLevelMax)).allowTreasure());

		return builder;
	}

	public static List<ResourceLocation> getLibraries() {
		List<ResourceLocation> lootChestList = new ArrayList<ResourceLocation>();
		lootChestList.add(BuiltInLootTables.STRONGHOLD_LIBRARY);
		lootChestList.add(BuiltInLootTables.SHIPWRECK_MAP);

		return lootChestList;
	}

	public static List<ResourceLocation> getOverworldDungeons() {
		List<ResourceLocation> lootChestList = new ArrayList<ResourceLocation>();
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
				if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BANE_OF_ARTHROPODS, held) > 0)
					return true;
			}
		}

		return false;
	}

	public static List<ResourceLocation> getBastionChests() {
		List<ResourceLocation> lootChestList = new ArrayList<ResourceLocation>();
		lootChestList.add(BuiltInLootTables.BASTION_TREASURE);
		lootChestList.add(BuiltInLootTables.BASTION_OTHER);
		lootChestList.add(BuiltInLootTables.BASTION_BRIDGE);
		lootChestList.add(BuiltInLootTables.BASTION_HOGLIN_STABLE);

		return lootChestList;
	}

	public static List<ResourceLocation> getNetherDungeons() {
		List<ResourceLocation> netherList = new ArrayList<ResourceLocation>();

		netherList.add(BuiltInLootTables.NETHER_BRIDGE);
		netherList.add(BuiltInLootTables.BASTION_TREASURE);
		netherList.add(BuiltInLootTables.BASTION_OTHER);
		netherList.add(BuiltInLootTables.BASTION_BRIDGE);
		netherList.add(BuiltInLootTables.BASTION_HOGLIN_STABLE);
		netherList.add(BuiltInLootTables.RUINED_PORTAL);

		return netherList;
	}

	public static List<ResourceLocation> getEarthenDungeons() {
		List<ResourceLocation> lootChestList = new ArrayList<ResourceLocation>();

		lootChestList.add(BuiltInLootTables.SIMPLE_DUNGEON);
		lootChestList.add(BuiltInLootTables.ABANDONED_MINESHAFT);
		lootChestList.add(BuiltInLootTables.VILLAGE_ARMORER);

		return lootChestList;
	}

	public static List<ResourceLocation> getWaterDungeons() {
		List<ResourceLocation> lootChestList = new ArrayList<ResourceLocation>();

		lootChestList.add(BuiltInLootTables.UNDERWATER_RUIN_BIG);
		lootChestList.add(BuiltInLootTables.UNDERWATER_RUIN_SMALL);
		lootChestList.add(BuiltInLootTables.SHIPWRECK_TREASURE);
		lootChestList.add(BuiltInLootTables.BURIED_TREASURE);

		return lootChestList;
	}

	public static List<ResourceLocation> getAirDungeons() {
		List<ResourceLocation> lootChestList = new ArrayList<ResourceLocation>();

		lootChestList.add(BuiltInLootTables.VILLAGE_TEMPLE);

		return lootChestList;
	}

	public static List<ResourceLocation> getEnderDungeons() {
		List<ResourceLocation> lootChestList = new ArrayList<ResourceLocation>();

		lootChestList.add(BuiltInLootTables.END_CITY_TREASURE);

		return lootChestList;
	}

	public static List<ResourceLocation> getMergedAir$EarthenDungeons() {
		List<ResourceLocation> lootChestList = new ArrayList<ResourceLocation>();

		lootChestList.add(BuiltInLootTables.DESERT_PYRAMID);
		lootChestList.add(BuiltInLootTables.JUNGLE_TEMPLE);

		return lootChestList;
	}

	public static List<ResourceLocation> getMergedEnder$EarthenDungeons() {
		List<ResourceLocation> lootChestList = new ArrayList<ResourceLocation>();

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

	// Port: upstream writes the cursed flag to EnigmaticTransience (the main menu's cursed
	// splash). That file and its title screen hook are not ported, so this is a no-op for now.
	public static void setCurrentWorldCursed(boolean cursed) {
		// TODO port EnigmaticTransience
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

			allInventories.addAll(player.getInventory().items);
			allInventories.addAll(player.getInventory().offhand);

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
		return stack1.getItem() == stack2.getItem() && ItemStack.isSameItemSameTags(stack1, stack2);
	}

	/**
	 * @return Multiplier for amount of particles based on client settings.
	 */
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

		int cx = player.chunkPosition().x, cz = player.chunkPosition().z;

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
	 * the accessory service (Trinkets on Fabric). Same UUIDs, so unlocks stay one per slot type.
	 */
	public static boolean unlockSpecialSlot(String slot, Player player) {
		if (!slot.equals("scroll") && !slot.equals("spellstone") && !slot.equals("ring") && !slot.equals("charm"))
			throw new IllegalArgumentException("Slot type '" + slot + "' is not supported!");

		UUID id = slot.equals("scroll") ? SCROLL_SLOT_UUID : (slot.equals("spellstone") ? SPELLSTONE_SLOT_UUID
				: (slot.equals("ring") ? RING_SLOT_UUID : CHARM_SLOT_UUID));

		return Services.ACCESSORIES.unlockSlot(player, slot, id, "Masterslot");
	}

	/**
	 * Port: upstream used Forge's changeDimension(level, ITeleporter) with RealSmoothTeleporter,
	 * which kept the player's position. Vanilla teleportTo across levels does the same.
	 */
	public static void sendToDimension(ServerPlayer player, ResourceKey<Level> dimension) {
		if (!player.level().dimension().equals(dimension)) {
			ServerLevel world = SuperpositionHandler.getWorld(dimension);
			if (world != null) {
				player.teleportTo(world, player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
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
		ServerLevel destinationWorld = vec.isPresent() ? respawnWorld : serverPlayer.server.overworld();

		if (!serverPlayer.level().equals(destinationWorld)) {
			// Port: upstream used Forge's changeDimension(level, ITeleporter) with RealSmoothTeleporter,
			// which only kept the player's position (no portal search). Vanilla teleportTo across
			// levels does the same; the exact spot is set right below either way.
			serverPlayer.teleportTo(destinationWorld, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), serverPlayer.getYRot(), serverPlayer.getXRot());
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
			AdvancedSpawnLocationHelper.fuckBackToSpawn(serverPlayer.serverLevel(), serverPlayer);
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
		ServerLevel destinationWorld = currentDimensionRespawnCoords.isPresent() ? respawnWorld : serverPlayer.server.overworld();

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
			trueVec = new Vec3(destinationWorld.getSharedSpawnPos().getX() + 0.5, destinationWorld.getSharedSpawnPos().getY() + 0.5, destinationWorld.getSharedSpawnPos().getZ() + 0.5);

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
		return theTag instanceof ByteTag ? ((ByteTag) theTag).getAsByte() != 0 : expectedValue;
	}

	public static void setPersistentInteger(Player player, String tag, int value) {
		SuperpositionHandler.setPersistentTag(player, tag, IntTag.valueOf(value));
	}

	public static int getPersistentInteger(Player player, String tag, int expectedValue) {
		Tag theTag = SuperpositionHandler.getPersistentTag(player, tag, IntTag.valueOf(expectedValue));
		return theTag instanceof IntTag ? ((IntTag) theTag).getAsInt() : expectedValue;
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
				player.getCooldowns().addCooldown(spellstone, newValue);
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
	public static void addPotionTooltip(List<MobEffectInstance> list, ItemStack itemIn, List<Component> lores, float durationFactor) {
		List<Pair<Attribute, AttributeModifier>> list1 = Lists.newArrayList();
		if (list.isEmpty()) {
			lores.add((Component.translatable("effect.none")).withStyle(ChatFormatting.GRAY));
		} else {
			for (MobEffectInstance effectinstance : list) {
				MutableComponent iformattabletextcomponent = Component.translatable(effectinstance.getDescriptionId());
				MobEffect effect = effectinstance.getEffect();
				Map<Attribute, AttributeModifier> map = effect.getAttributeModifiers();
				if (!map.isEmpty()) {
					for (Entry<Attribute, AttributeModifier> entry : map.entrySet()) {
						AttributeModifier attributemodifier = entry.getValue();
						AttributeModifier attributemodifier1 = new AttributeModifier(attributemodifier.getName(), effect.getAttributeModifierValue(effectinstance.getAmplifier(), attributemodifier), attributemodifier.getOperation());
						list1.add(new Pair<>(entry.getKey(), attributemodifier1));
					}
				}

				if (effectinstance.getAmplifier() > 0) {
					iformattabletextcomponent.append(" ").append(Component.translatable("potion.potency." + effectinstance.getAmplifier()));
				}

				if (effectinstance.getDuration() > 20) {
					iformattabletextcomponent.append(" (").append(MobEffectUtil.formatDuration(effectinstance, durationFactor)).append(")");
				}

				lores.add(iformattabletextcomponent.withStyle(effect.getCategory().getTooltipFormatting()));
			}
		}

		if (!list1.isEmpty()) {
			lores.add(CommonComponents.EMPTY);
			lores.add((Component.translatable("potion.whenDrank")).withStyle(ChatFormatting.DARK_PURPLE));

			for (Pair<Attribute, AttributeModifier> pair : list1) {
				AttributeModifier attributemodifier2 = pair.getSecond();
				double d0 = attributemodifier2.getAmount();
				double d1;
				if (attributemodifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributemodifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
					d1 = attributemodifier2.getAmount();
				} else {
					d1 = attributemodifier2.getAmount() * 100.0D;
				}

				if (d0 > 0.0D) {
					lores.add((Component.translatable("attribute.modifier.plus." + attributemodifier2.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(pair.getFirst().getDescriptionId()))).withStyle(ChatFormatting.BLUE));
				} else if (d0 < 0.0D) {
					d1 = d1 * -1.0D;
					lores.add((Component.translatable("attribute.modifier.take." + attributemodifier2.getOperation().toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), Component.translatable(pair.getFirst().getDescriptionId()))).withStyle(ChatFormatting.DARK_RED));
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
		Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
		int totalCurses = 0;

		for (Enchantment enchantment: enchantments.keySet()) {
			if (enchantment.isCurse() && enchantments.get(enchantment) > 0) {
				totalCurses+=1;
			}
		}

		if (stack.getItem() == EnigmaticItems.CURSED_RING) {
			totalCurses+=7;
		}

		return totalCurses;
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
		equipmentStacks.addAll(player.getInventory().armor);
		equipmentStacks.addAll(Services.ACCESSORIES.findAll(player, stack -> true));

		return equipmentStacks;
	}

	public static boolean hasAnyArmor(LivingEntity entity) {
		int armorAmount = 0;

		for (ItemStack stack : entity.getArmorSlots()) {
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

	public static boolean hasAdvancement(ServerPlayer player, ResourceLocation location) {
		try {
			if (player.getAdvancements().getOrStartProgress(player.server.getAdvancements().getAdvancement(location)).isDone())
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

	public static void grantAdvancement(ServerPlayer player, ResourceLocation location) {
		Advancement adv = player.server.getAdvancements().getAdvancement(location);

		for (String criterion : player.getAdvancements().getOrStartProgress(adv).getRemainingCriteria()) {
			player.getAdvancements().award(adv, criterion);
		}

	}

	public static void revokeAdvancement(ServerPlayer player, ResourceLocation location) {
		Advancement adv = player.server.getAdvancements().getAdvancement(location);

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
		boolean keepInventory = player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);

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
