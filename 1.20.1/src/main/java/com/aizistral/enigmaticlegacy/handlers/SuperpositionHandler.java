package com.aizistral.enigmaticlegacy.handlers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.api.generic.ConfigurableItem;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.config.ConfigHolders;
import com.aizistral.enigmaticlegacy.platform.Services;
import com.aizistral.omniconfig.Configuration;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

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
}
