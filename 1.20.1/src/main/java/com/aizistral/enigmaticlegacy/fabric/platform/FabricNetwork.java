package com.aizistral.enigmaticlegacy.fabric.platform;

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

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
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
 */
public class FabricNetwork implements NetworkPlatform {
	public record Packet<M>(ResourceLocation id, BiConsumer<M, FriendlyByteBuf> encoder,
			Function<FriendlyByteBuf, M> decoder, BiConsumer<M, Supplier<PacketContext>> handler) {}

	private static final Map<Class<?>, Packet<?>> PACKETS = new HashMap<>();
	/** Client-bound packets; the client entry point registers their receivers. */
	public static final List<Packet<?>> CLIENT_BOUND = new ArrayList<>();
	/** Set by the client entry point; sends a server-bound packet. */
	public static BiConsumer<ResourceLocation, FriendlyByteBuf> clientSender = (id, buf) -> {
		throw new IllegalStateException("Cannot send packets to the server from a dedicated server");
	};

	@Override
	public <M> void register(Class<M> type, PacketDirection direction, BiConsumer<M, FriendlyByteBuf> encoder,
			Function<FriendlyByteBuf, M> decoder, BiConsumer<M, Supplier<PacketContext>> handler) {
		var id = new ResourceLocation(EnigmaticLegacy.MODID, type.getSimpleName().toLowerCase(Locale.ROOT));
		var packet = new Packet<>(id, encoder, decoder, handler);

		if (PACKETS.putIfAbsent(type, packet) != null)
			throw new IllegalStateException("Packet " + type.getName() + " is already registered");

		if (direction == PacketDirection.TO_CLIENT) {
			CLIENT_BOUND.add(packet);
		} else {
			ServerPlayNetworking.registerGlobalReceiver(id, (server, player, listener, buf, sender) -> {
				// The buffer is released after this call, so decode on the network thread.
				M msg = decoder.apply(buf);
				var context = new PacketContext(server, player);
				handler.accept(msg, () -> context);
			});
		}
	}

	@SuppressWarnings("unchecked")
	private static <M> FriendlyByteBuf encode(M msg, Packet<?> packet) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		((Packet<M>) packet).encoder().accept(msg, buf);
		return buf;
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
			ServerPlayNetworking.send(player, packet.id(), encode(msg, packet));
		}
	}

	@Override
	public void sendToPlayer(ServerPlayer player, Object msg) {
		send(List.of(player), msg);
	}

	@Override
	public void sendToServer(Object msg) {
		Packet<?> packet = packet(msg);
		clientSender.accept(packet.id(), encode(msg, packet));
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
