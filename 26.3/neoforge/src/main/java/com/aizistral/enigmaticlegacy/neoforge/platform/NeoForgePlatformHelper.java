package com.aizistral.enigmaticlegacy.neoforge.platform;

import java.nio.file.Path;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.platform.PlatformHelper;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Port-only. NeoForge implementation of {@link PlatformHelper}.
 */
public class NeoForgePlatformHelper implements PlatformHelper {
	@Override
	public String getLoaderName() {
		return "NeoForge";
	}

	@Override
	public boolean isModLoaded(String modId) {
		return ModList.get().isLoaded(modId);
	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return !FMLEnvironment.isProduction();
	}

	@Override
	public boolean firesLivingDrops() {
		return true;
	}

	@Override
	public boolean isPhysicalClient() {
		return FMLEnvironment.getDist() == Dist.CLIENT;
	}

	@Override
	public Path getConfigDir() {
		return FMLPaths.CONFIGDIR.get();
	}

	@Override
	public Path getGameDir() {
		return FMLPaths.GAMEDIR.get();
	}

	@Override
	@Nullable
	public MinecraftServer getServer() {
		return ServerLifecycleHooks.getCurrentServer();
	}

	@Override
	public CreativeModeTab.Builder creativeTabBuilder() {
		return CreativeModeTab.builder();
	}
}
