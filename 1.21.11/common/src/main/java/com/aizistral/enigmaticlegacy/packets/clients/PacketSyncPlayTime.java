package com.aizistral.enigmaticlegacy.packets.clients;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.UUID;
import java.util.function.Supplier;

import com.aizistral.enigmaticlegacy.api.capabilities.IPlaytimeCounter;
import com.aizistral.enigmaticlegacy.platform.network.PacketContext;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

// Port: NetworkEvent.Context -> PacketContext; @OnlyIn -> @Environment(EnvType.CLIENT) on the client handle method (Fabric does not strip lambdas).
public class PacketSyncPlayTime {
	private UUID playerID;
	private long timeWithCurses, timeWithoutCurses;

	public PacketSyncPlayTime(UUID playerID, long timeWithCurses, long timeWithoutCurses) {
		this.playerID = playerID;
		this.timeWithCurses = timeWithCurses;
		this.timeWithoutCurses =  timeWithoutCurses;
	}

	public static void encode(PacketSyncPlayTime msg, FriendlyByteBuf buf) {
		buf.writeUUID(msg.playerID);
		buf.writeLong(msg.timeWithCurses);
		buf.writeLong(msg.timeWithoutCurses);
	}

	public static PacketSyncPlayTime decode(FriendlyByteBuf buf) {
		return new PacketSyncPlayTime(buf.readUUID(), buf.readLong(), buf.readLong());
	}

	public static void handle(PacketSyncPlayTime msg, Supplier<PacketContext> ctx) {
		ctx.get().enqueueWork(() -> {
			handle(msg.playerID, msg.timeWithCurses, msg.timeWithoutCurses);
		});

		ctx.get().setPacketHandled(true);
	}

	@Environment(EnvType.CLIENT)
	private static void handle(UUID playerID, long timeWithCurses, long timeWithoutCurses) {
		// Port: a loop, not a stream; Fabric does not strip lambdas typed with client classes.
		for (Player player : Minecraft.getInstance().level.players()) {
			if (player.getUUID().equals(playerID)) {
				var counter = IPlaytimeCounter.get(player);
				counter.setTimeWithCurses(timeWithCurses);
				counter.setTimeWithoutCurses(timeWithoutCurses);
				break;
			}
		}
	}
}
