package com.aizistral.enigmaticlegacy.packets.clients;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.function.Supplier;

import com.aizistral.enigmaticlegacy.platform.network.PacketContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

/**
 * Packet that forces a deflected projectile's position, motion and rotation on the client.
 */
public class PacketForceArrowRotations {

	private int entityID;
	private double motionX, motionY, motionZ, posX, posY, posZ;
	private float rotationYaw;
	private float rotationPitch;

	public PacketForceArrowRotations(int entityID, float rotationYaw, float rotationPitch, double vecX, double vecY, double vecZ, double posX, double posY, double posZ) {
		this.entityID = entityID;
		this.rotationYaw = rotationYaw;
		this.rotationPitch = rotationPitch;
		this.motionX = vecX;
		this.motionY = vecY;
		this.motionZ = vecZ;
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
	}

	public static void encode(PacketForceArrowRotations msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.entityID);
		buf.writeFloat(msg.rotationYaw);
		buf.writeFloat(msg.rotationPitch);
		buf.writeDouble(msg.motionX);
		buf.writeDouble(msg.motionY);
		buf.writeDouble(msg.motionZ);
		buf.writeDouble(msg.posX);
		buf.writeDouble(msg.posY);
		buf.writeDouble(msg.posZ);
	}

	public static PacketForceArrowRotations decode(FriendlyByteBuf buf) {
		return new PacketForceArrowRotations(buf.readInt(), buf.readFloat(), buf.readFloat(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble());
	}

	public static void handle(PacketForceArrowRotations msg, Supplier<PacketContext> ctx) {
		ctx.get().enqueueWork(() -> handleClient(msg));
		ctx.get().setPacketHandled(true);
	}


	@Environment(EnvType.CLIENT)
	private static void handleClient(PacketForceArrowRotations msg) {
		ClientLevel theWorld = Minecraft.getInstance().level;
		Entity arrow = theWorld.getEntity(msg.entityID);

		if (arrow != null) {
			arrow.addTag("enigmaticlegacy.redirected");

			arrow.moveTo(msg.posX, msg.posY, msg.posZ);
			arrow.setDeltaMovement(msg.motionX, msg.motionY, msg.motionZ);
			arrow.setYRot(msg.rotationYaw);
			arrow.yRotO = msg.rotationYaw;
			arrow.setXRot(msg.rotationPitch);
			arrow.xRotO = msg.rotationPitch;
		}
	}
}
