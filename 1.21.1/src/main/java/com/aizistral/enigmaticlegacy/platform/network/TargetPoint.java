package com.aizistral.enigmaticlegacy.platform.network;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * Port-only stand-in for Forge's PacketDistributor.TargetPoint: every player within the
 * radius of a point in a dimension, except the optional excluded player.
 */
public record TargetPoint(@Nullable ServerPlayer excluded, double x, double y, double z, double radius,
		ResourceKey<Level> dimension) {

	public TargetPoint(double x, double y, double z, double radius, ResourceKey<Level> dimension) {
		this(null, x, y, z, radius, dimension);
	}
}
