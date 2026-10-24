package com.aizistral.enigmaticlegacy.packets.clients;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import java.util.Random;
import java.util.function.Supplier;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.client.fx.PermanentItemPickupParticle;
import com.aizistral.enigmaticlegacy.platform.network.PacketContext;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

/**
 * Packet for playing respective sound and animation on client side,
 * whenever item is picked up by player.
 * @author Integral
 */

// Port: NetworkEvent.Context -> PacketContext; ClientProxy.handleItemPickup inlined into handle.
public class PacketHandleItemPickup {
	private static final Random RANDOM = new Random();
	private int item_id;
	private int pickuper_id;

	public PacketHandleItemPickup(int itemID, int pickuperID) {
		this.item_id = itemID;
		this.pickuper_id = pickuperID;
	}

	public static void encode(PacketHandleItemPickup msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.item_id);
		buf.writeInt(msg.pickuper_id);
	}

	public static PacketHandleItemPickup decode(FriendlyByteBuf buf) {
		return new PacketHandleItemPickup(buf.readInt(), buf.readInt());
	}

	public static void handle(PacketHandleItemPickup msg, Supplier<PacketContext> ctx) {
		ctx.get().enqueueWork(() -> {
			handleItemPickup(msg.pickuper_id, msg.item_id);
		});

		ctx.get().setPacketHandled(true);
	}

	// Port: was ClientProxy.handleItemPickup. Upstream's argument order is kept as is.
	@Environment(EnvType.CLIENT)
	private static void handleItemPickup(int pickuper_id, int item_id) {
		try {
			Minecraft mc = Minecraft.getInstance();
			Entity pickuper = mc.level.getEntity(pickuper_id);
			Entity entity = mc.level.getEntity(item_id);

			mc.particleEngine.add(new PermanentItemPickupParticle(mc.getEntityRenderDispatcher(), mc.renderBuffers(), mc.level, pickuper, entity));
			mc.level.playLocalSound(pickuper.getX(), pickuper.getY(), pickuper.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, (RANDOM.nextFloat() - RANDOM.nextFloat()) * 1.4F + 2.0F, false);
		} catch (Throwable ex) {
			Exception log = new Exception("Unknown error when rendering permanent item pickup", ex);
			EnigmaticLegacy.LOGGER.catching(log);
		}
	}

}
