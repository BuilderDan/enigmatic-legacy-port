package com.aizistral.enigmaticlegacy.platform.network;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Port-only stand-in for Forge's SimpleChannel and PacketDistributor. Packets keep
 * upstream's static encode/decode/handle methods; only the context type changes.
 */
public interface NetworkPlatform {
	/**
	 * Registers a packet type. Call for every packet during mod init, on both sides.
	 * The channel id is {@code enigmaticlegacy:<class name in lower case>}.
	 */
	<M> void register(Class<M> type, PacketDirection direction, BiConsumer<M, FriendlyByteBuf> encoder,
			Function<FriendlyByteBuf, M> decoder, BiConsumer<M, Supplier<PacketContext>> handler);

	/** Upstream {@code PacketDistributor.PLAYER}. */
	void sendToPlayer(ServerPlayer player, Object message);

	/** Upstream {@code sendToServer}. Client side only. */
	void sendToServer(Object message);

	/** Upstream {@code PacketDistributor.NEAR}. */
	void sendToNear(TargetPoint point, Object message);

	/** Upstream {@code PacketDistributor.ALL}. */
	void sendToAll(Object message);

	/** Every player tracking the entity, plus the entity itself if it is a player. */
	void sendToTrackingAndSelf(Entity entity, Object message);
}
