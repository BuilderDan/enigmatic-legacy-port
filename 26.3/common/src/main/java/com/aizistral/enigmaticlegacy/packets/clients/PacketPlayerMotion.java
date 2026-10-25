package com.aizistral.enigmaticlegacy.packets.clients;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.function.Supplier;

import com.aizistral.enigmaticlegacy.platform.network.PacketContext;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Packet for setting the client player's motion.
 * @author Integral
 */

// Port: NetworkEvent.Context -> PacketContext; @OnlyIn -> @Environment(EnvType.CLIENT) on handleClient (Fabric does not strip lambdas).
public class PacketPlayerMotion {
	private double x, y, z;

	public PacketPlayerMotion(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static void encode(PacketPlayerMotion msg, FriendlyByteBuf buf) {
		buf.writeDouble(msg.x);
		buf.writeDouble(msg.y);
		buf.writeDouble(msg.z);
	}

	public static PacketPlayerMotion decode(FriendlyByteBuf buf) {
		return new PacketPlayerMotion(buf.readDouble(), buf.readDouble(), buf.readDouble());
	}

	public static void handle(PacketPlayerMotion msg, Supplier<PacketContext> ctx) {
		ctx.get().enqueueWork(() -> handleClient(msg));

		ctx.get().setPacketHandled(true);
	}

	@Environment(EnvType.CLIENT)
	private static void handleClient(PacketPlayerMotion msg) {
		Minecraft.getInstance().player.setDeltaMovement(msg.x, msg.y, msg.z);
	}
}
