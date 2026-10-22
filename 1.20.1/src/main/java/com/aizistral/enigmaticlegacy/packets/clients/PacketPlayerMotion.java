package com.aizistral.enigmaticlegacy.packets.clients;

import java.util.function.Supplier;

import com.aizistral.enigmaticlegacy.platform.network.PacketContext;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Packet for setting the client player's motion.
 * @author Integral
 */

// Port: NetworkEvent.Context -> PacketContext; @OnlyIn removed (only the client runs handle).
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
		ctx.get().enqueueWork(() -> {
			Minecraft.getInstance().player.setDeltaMovement(msg.x, msg.y, msg.z);
		});

		ctx.get().setPacketHandled(true);
	}
}
