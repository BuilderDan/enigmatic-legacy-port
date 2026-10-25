package com.aizistral.enigmaticlegacy.packets.server;

import java.util.function.Supplier;

import com.aizistral.enigmaticlegacy.objects.ElytraBoostData;
import com.aizistral.enigmaticlegacy.packets.clients.PacketSyncElytraBoosting;
import com.aizistral.enigmaticlegacy.platform.Services;
import com.aizistral.enigmaticlegacy.platform.network.PacketContext;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class PacketUpdateElytraBoosting {
	private boolean boosting;

	public PacketUpdateElytraBoosting(boolean boosting) {
		this.boosting = boosting;
	}

	public static void encode(PacketUpdateElytraBoosting msg, FriendlyByteBuf buf) {
		buf.writeBoolean(msg.boosting);
	}

	public static PacketUpdateElytraBoosting decode(FriendlyByteBuf buf) {
		return new PacketUpdateElytraBoosting(buf.readBoolean());
	}

	// Port: NetworkEvent.Context -> PacketContext; TransientPlayerData -> ElytraBoostData.
	public static void handle(PacketUpdateElytraBoosting msg, Supplier<PacketContext> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();

			if (msg.boosting) {
				player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_EYE_LAUNCH, SoundSource.AMBIENT, 3.0F, 1.0F);
			}

			if (ElytraBoostData.setServer(player, msg.boosting)) {
				// Port: upstream syncs the whole TransientPlayerData to every client.
				Services.NETWORK.sendToTrackingAndSelf(player, new PacketSyncElytraBoosting(player.getUUID(), msg.boosting));
			}
		});
		ctx.get().setPacketHandled(true);
	}

}
