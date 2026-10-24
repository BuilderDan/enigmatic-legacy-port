package com.aizistral.enigmaticlegacy.fabric.platform;

import java.nio.file.Path;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.platform.PlatformHelper;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

/**
 * Port-only. Fabric implementation of {@link PlatformHelper}.
 */
public class FabricPlatformHelper implements PlatformHelper {
	@Nullable
	private static volatile MinecraftServer server;

	/** Tracks the running server. Called from the main entry point. */
	public static void init() {
		ServerLifecycleEvents.SERVER_STARTING.register(s -> server = s);
		ServerLifecycleEvents.SERVER_STOPPED.register(s -> server = null);
	}

	@Override
	public String getLoaderName() {
		return "Fabric";
	}

	@Override
	public boolean isModLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	@Override
	public boolean isPhysicalClient() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
	}

	@Override
	public Path getConfigDir() {
		return FabricLoader.getInstance().getConfigDir();
	}

	@Override
	public Path getGameDir() {
		return FabricLoader.getInstance().getGameDir();
	}

	@Nullable
	static MinecraftServer currentServer() {
		return server;
	}

	@Override
	@Nullable
	public MinecraftServer getServer() {
		return server;
	}
}
