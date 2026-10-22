package com.aizistral.enigmaticlegacy.packets.clients;

import java.util.function.Supplier;

import com.aizistral.enigmaticlegacy.platform.network.PacketContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Packet for forcing the client player to look at a point in space.
 * @author Integral
 */

// Port: NetworkEvent.Context -> PacketContext; @OnlyIn removed (only the client runs handle).
public class PacketPlayerSetlook {
	private double x, y, z;

	public PacketPlayerSetlook(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static void encode(PacketPlayerSetlook msg, FriendlyByteBuf buf) {
		buf.writeDouble(msg.x);
		buf.writeDouble(msg.y);
		buf.writeDouble(msg.z);
	}

	public static PacketPlayerSetlook decode(FriendlyByteBuf buf) {
		return new PacketPlayerSetlook(buf.readDouble(), buf.readDouble(), buf.readDouble());
	}

	public static void handle(PacketPlayerSetlook msg, Supplier<PacketContext> ctx) {
		ctx.get().enqueueWork(() -> {
			lookAt(msg.x, msg.y, msg.z, Minecraft.getInstance().player);
		});

		ctx.get().setPacketHandled(true);
	}

	/**
	 * Port: upstream's SuperpositionHandler.lookAt, moved here so that the common
	 * SuperpositionHandler never references the client-only LocalPlayer class.
	 */
	private static void lookAt(double px, double py, double pz, LocalPlayer me) {
		double dirx = me.getX() - px;
		double diry = me.getY() - py;
		double dirz = me.getZ() - pz;

		double len = Math.sqrt(dirx * dirx + diry * diry + dirz * dirz);

		dirx /= len;
		diry /= len;
		dirz /= len;

		double pitch = Math.asin(diry);
		double yaw = Math.atan2(dirz, dirx);

		// to degree
		pitch = pitch * 180.0 / Math.PI;
		yaw = yaw * 180.0 / Math.PI;

		yaw += 90f;
		me.setXRot((float) pitch);
		me.setYRot((float) yaw);
	}
}
