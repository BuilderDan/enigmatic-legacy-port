package com.aizistral.enigmaticlegacy.handlers;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.api.capabilities.IPlaytimeCounter;
import com.aizistral.enigmaticlegacy.api.generic.ConfigurableItem;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.config.ConfigHolders;
import com.aizistral.enigmaticlegacy.objects.Vector3;
import com.aizistral.enigmaticlegacy.platform.Services;
import com.aizistral.omniconfig.Configuration;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

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
}
