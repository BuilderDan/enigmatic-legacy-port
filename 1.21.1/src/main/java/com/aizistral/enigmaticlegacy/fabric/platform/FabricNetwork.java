package com.aizistral.enigmaticlegacy.fabric.platform;

import java.util.function.Consumer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.platform.network.NetworkPlatform;
import com.aizistral.enigmaticlegacy.platform.network.PacketContext;
import com.aizistral.enigmaticlegacy.platform.network.PacketDirection;
import com.aizistral.enigmaticlegacy.platform.network.TargetPoint;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Port-only. {@link NetworkPlatform} on Fabric API networking. Each packet class gets
 * its own channel, {@code enigmaticlegacy:<lowercase simple class name>}.
 * <p>
 * Port: 1.21 Fabric networking sends typed payloads, so each packet gets a {@link Payload}
 * type whose stream codec wraps the upstream encoder and decoder.
 */
public class FabricNetwork implements NetworkPlatform {
	public record Packet<M>(CustomPacketPayload.Type<Payload<M>> type, StreamCodec<RegistryFriendlyByteBuf, Payload<M>> codec,
			BiConsumer<M, Supplier<PacketContext>> handler) {}

	/** Carries one decoded upstream message. */
	public record Payload<M>(CustomPacketPayload.Type<Payload<M>> type, M msg) implements CustomPacketPayload {}

	private static final Map<Class<?>, Packet<?>> PACKETS = new HashMap<>();
	/** Client-bound packets; the client entry point registers their receivers. */
	public static final List<Packet<?>> CLIENT_BOUND = new ArrayList<>();
	/** Set by the client entry point; sends a server-bound packet. */
	public static Consumer<CustomPacketPayload> clientSender = payload -> {
		throw new IllegalStateException("Cannot send packets to the server from a dedicated server");
	};

	@Override
	public <M> void register(Class<M> type, PacketDirection direction, BiConsumer<M, FriendlyByteBuf> encoder,
			Function<FriendlyByteBuf, M> decoder, BiConsumer<M, Supplier<PacketContext>> handler) {
		var id = ResourceLocation.fromNamespaceAndPath(EnigmaticLegacy.MODID, type.getSimpleName().toLowerCase(Locale.ROOT));
		CustomPacketPayload.Type<Payload<M>> payloadType = new CustomPacketPayload.Type<>(id);
		StreamCodec<RegistryFriendlyByteBuf, Payload<M>> codec = StreamCodec.of((buf, payload) -> encoder.accept(payload.msg(), buf),
				buf -> new Payload<>(payloadType, decoder.apply(buf)));
		var packet = new Packet<>(payloadType, codec, handler);

		if (PACKETS.putIfAbsent(type, packet) != null)
			throw new IllegalStateException("Packet " + type.getName() + " is already registered");

		if (direction == PacketDirection.TO_CLIENT) {
			PayloadTypeRegistry.playS2C().register(payloadType, codec);
			CLIENT_BOUND.add(packet);
		} else {
			PayloadTypeRegistry.playC2S().register(payloadType, codec);
			ServerPlayNetworking.registerGlobalReceiver(payloadType, (payload, ctx) -> {
				var context = new PacketContext(ctx.server(), ctx.player());
				handler.accept(payload.msg(), () -> context);
			});
		}
	}

	@SuppressWarnings("unchecked")
	private static <M> Payload<M> payload(Object msg, Packet<?> packet) {
		return new Payload<>(((Packet<M>) packet).type(), (M) msg);
	}

	private static Packet<?> packet(Object msg) {
		Packet<?> packet = PACKETS.get(msg.getClass());

		if (packet == null)
			throw new IllegalArgumentException("Unregistered packet " + msg.getClass().getName());

		return packet;
	}

	private static void send(Iterable<ServerPlayer> players, Object msg) {
		Packet<?> packet = packet(msg);

		for (ServerPlayer player : players) {
			ServerPlayNetworking.send(player, payload(msg, packet));
		}
	}

	@Override
	public void sendToPlayer(ServerPlayer player, Object msg) {
		send(List.of(player), msg);
	}

	@Override
	public void sendToServer(Object msg) {
		Packet<?> packet = packet(msg);
		clientSender.accept(payload(msg, packet));
	}

	@Override
	public void sendToNear(TargetPoint point, Object msg) {
		MinecraftServer server = FabricPlatformHelper.currentServer();
		ServerLevel level = server != null ? server.getLevel(point.dimension()) : null;

		if (level == null)
			return;

		List<ServerPlayer> players = new ArrayList<>(PlayerLookup.around(level, new Vec3(point.x(), point.y(), point.z()), point.radius()));
		players.remove(point.excluded());
		send(players, msg);
	}

	@Override
	public void sendToAll(Object msg) {
		MinecraftServer server = FabricPlatformHelper.currentServer();

		if (server != null) {
			send(PlayerLookup.all(server), msg);
		}
	}

	@Override
	public void sendToTrackingAndSelf(Entity entity, Object msg) {
		List<ServerPlayer> players = new ArrayList<>(PlayerLookup.tracking(entity));

		if (entity instanceof ServerPlayer player && !players.contains(player)) {
			players.add(player);
		}

		send(players, msg);
	}
}
