package com.aizistral.enigmaticlegacy.neoforge.platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.platform.network.NetworkPlatform;
import com.aizistral.enigmaticlegacy.platform.network.PacketContext;
import com.aizistral.enigmaticlegacy.platform.network.PacketDirection;
import com.aizistral.enigmaticlegacy.platform.network.TargetPoint;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Port-only. {@link NetworkPlatform} on NeoForge payload networking. Each packet class gets
 * its own channel, {@code enigmaticlegacy:<lowercase simple class name>}.
 * <p>
 * Packets are collected during mod init and handed to NeoForge in
 * {@link RegisterPayloadHandlersEvent}. Client-bound handlers are registered by the client
 * entry point, so client classes never load on a dedicated server.
 */
public class NeoForgeNetwork implements NetworkPlatform {
	public record Packet<M>(CustomPacketPayload.Type<Payload<M>> type, StreamCodec<RegistryFriendlyByteBuf, Payload<M>> codec,
			PacketDirection direction, BiConsumer<M, Supplier<PacketContext>> handler) {}

	/** Carries one decoded upstream message. */
	public record Payload<M>(CustomPacketPayload.Type<Payload<M>> type, M msg) implements CustomPacketPayload {}

	private static final Map<Class<?>, Packet<?>> PACKETS = new HashMap<>();
	private static final List<Packet<?>> ORDERED = new ArrayList<>();
	/** Client-bound packets; the client entry point registers their handlers. */
	public static final List<Packet<?>> CLIENT_BOUND = new ArrayList<>();
	/** Set by the client entry point; sends a server-bound packet. */
	public static Consumer<CustomPacketPayload> clientSender = payload -> {
		throw new IllegalStateException("Cannot send packets to the server from a dedicated server");
	};
	private static boolean frozen = false;

	@Override
	public <M> void register(Class<M> type, PacketDirection direction, BiConsumer<M, FriendlyByteBuf> encoder,
			Function<FriendlyByteBuf, M> decoder, BiConsumer<M, Supplier<PacketContext>> handler) {
		if (frozen)
			throw new IllegalStateException("Packet " + type.getName() + " registered after RegisterPayloadHandlersEvent");

		var id = Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, type.getSimpleName().toLowerCase(Locale.ROOT));
		CustomPacketPayload.Type<Payload<M>> payloadType = new CustomPacketPayload.Type<>(id);
		StreamCodec<RegistryFriendlyByteBuf, Payload<M>> codec = StreamCodec.of((buf, payload) -> encoder.accept(payload.msg(), buf),
				buf -> new Payload<>(payloadType, decoder.apply(buf)));
		var packet = new Packet<>(payloadType, codec, direction, handler);

		if (PACKETS.putIfAbsent(type, packet) != null)
			throw new IllegalStateException("Packet " + type.getName() + " is already registered");

		ORDERED.add(packet);

		if (direction == PacketDirection.TO_CLIENT) {
			CLIENT_BOUND.add(packet);
		}
	}

	/** Mod bus listener; registers every collected packet with NeoForge. */
	public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
		frozen = true;
		PayloadRegistrar registrar = event.registrar("1");
		ORDERED.forEach(packet -> registerPayload(registrar, packet));
	}

	private static <M> void registerPayload(PayloadRegistrar registrar, Packet<M> packet) {
		if (packet.direction() == PacketDirection.TO_CLIENT) {
			registrar.playToClient(packet.type(), packet.codec());
		} else {
			registrar.playToServer(packet.type(), packet.codec(), (payload, ctx) -> {
				ServerPlayer player = (ServerPlayer) ctx.player();
				var context = new PacketContext(player.level().getServer(), player);
				packet.handler().accept(payload.msg(), () -> context);
			});
		}
	}

	@SuppressWarnings("unchecked")
	private static <M> Payload<M> payload(Object msg) {
		Packet<?> packet = PACKETS.get(msg.getClass());

		if (packet == null)
			throw new IllegalArgumentException("Unregistered packet " + msg.getClass().getName());

		return new Payload<>(((Packet<M>) packet).type(), (M) msg);
	}

	@Override
	public void sendToPlayer(ServerPlayer player, Object msg) {
		PacketDistributor.sendToPlayer(player, payload(msg));
	}

	@Override
	public void sendToServer(Object msg) {
		clientSender.accept(payload(msg));
	}

	@Override
	public void sendToNear(TargetPoint point, Object msg) {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		ServerLevel level = server != null ? server.getLevel(point.dimension()) : null;

		if (level != null) {
			PacketDistributor.sendToPlayersNear(level, point.excluded(), point.x(), point.y(), point.z(), point.radius(), payload(msg));
		}
	}

	@Override
	public void sendToAll(Object msg) {
		if (ServerLifecycleHooks.getCurrentServer() != null) {
			PacketDistributor.sendToAllPlayers(payload(msg));
		}
	}

	@Override
	public void sendToTrackingAndSelf(Entity entity, Object msg) {
		PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload(msg));
	}
}
