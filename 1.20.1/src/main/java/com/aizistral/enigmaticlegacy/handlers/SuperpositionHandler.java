package com.aizistral.enigmaticlegacy.handlers;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;
import com.aizistral.enigmaticlegacy.api.items.ISpellstone;
import com.aizistral.enigmaticlegacy.items.generic.ItemSpellstoneCurio;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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

	public static boolean isTheBlessedOne(Player player) {
		if (EnigmaticLegacy.SOUL_OF_THE_ARCHITECT.equals(player.getUUID()))
			return true;
		else
			return DevotedBelieversHandler.isDevotedBeliever(player);
	}

	public static boolean isTheCursedOne(Player player) {
		// Port: always false until CursedRing is ported; then
		// Services.ACCESSORIES.isEquipped(player, EnigmaticItems.CURSED_RING).
		return false;
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
		// Port: Architect's Favor (cosmic_scroll) is not ported yet.
		return false;
	}
}
