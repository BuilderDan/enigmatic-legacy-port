package com.aizistral.enigmaticlegacy.platform;

import net.minecraft.world.item.CreativeModeTab;

import java.nio.file.Path;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.MinecraftServer;

/**
 * Port-only. Replaces Forge's ModList, FMLEnvironment, FMLPaths and ServerLifecycleHooks.
 */
public interface PlatformHelper {
	String getLoaderName();

	boolean isModLoaded(String modId);

	boolean isDevelopmentEnvironment();

	/** True on the physical client (the game window), false on a dedicated server. */
	boolean isPhysicalClient();

	default boolean isDedicatedServer() {
		return !this.isPhysicalClient();
	}

	Path getConfigDir();

	Path getGameDir();

	/** The running logical server, or null when none is running. */
	@Nullable
	MinecraftServer getServer();

	/** A creative tab builder that the loader pages correctly. */
	CreativeModeTab.Builder creativeTabBuilder();

	/**
	 * True when the loader captures death drops and fires its own drops event (NeoForge's
	 * LivingDropsEvent). The common drop capture in MixinLivingEntity then stays off.
	 */
	default boolean firesLivingDrops() {
		return false;
	}
}
