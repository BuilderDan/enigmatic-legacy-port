package com.aizistral.enigmaticlegacy.packets.clients;

import java.util.function.Supplier;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.platform.network.PacketContext;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

// Port: NetworkEvent.Context -> PacketContext. Upstream's ClientProxy.displayReviveAnimation and
// EnigmaticEventHandler.scheduledCubeRevive moved here; only the client runs handle and onClientTick.
public class PacketCosmicRevive {
	/** Ticks until the client player's own Cube revive animation plays, or -1 for none. */
	public static int scheduledCubeRevive = -1;

	private int entityID;
	private int reviveType;

	public PacketCosmicRevive(int entityID, int reviveType) {
		this.entityID = entityID;
		this.reviveType = reviveType;
	}

	public static void encode(PacketCosmicRevive msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.entityID);
		buf.writeInt(msg.reviveType);
	}

	public static PacketCosmicRevive decode(FriendlyByteBuf buf) {
		return new PacketCosmicRevive(buf.readInt(), buf.readInt());
	}

	public static void handle(PacketCosmicRevive msg, Supplier<PacketContext> ctx) {
		ctx.get().enqueueWork(() -> {
			if (msg.reviveType == 0) {
				displayReviveAnimation(msg.entityID, msg.reviveType);
			} else {
				Player player = Minecraft.getInstance().player;
				Entity entity = player.level().getEntity(msg.entityID);

				if (entity == player) {
					scheduledCubeRevive = 5;
				} else {
					displayReviveAnimation(msg.entityID, msg.reviveType);
				}
			}
		});

		ctx.get().setPacketHandled(true);
	}

	/** Port: the client-player branch of upstream's EnigmaticEventHandler.onPlayerTick. */
	public static void onClientTick(Minecraft minecraft) {
		if (minecraft.player != null && scheduledCubeRevive > 0 && !(minecraft.screen instanceof ReceivingLevelScreen)) {
			scheduledCubeRevive--;

			if (scheduledCubeRevive == 0) {
				displayReviveAnimation(minecraft.player.getId(), 1);
				scheduledCubeRevive = -1;
			}
		}
	}

	private static void displayReviveAnimation(int entityID, int reviveType) {
		Player player = Minecraft.getInstance().player;
		Entity entity = player.level().getEntity(entityID);

		if (entity != null) {
			Item item = reviveType == 0 ? EnigmaticItems.COSMIC_SCROLL : EnigmaticItems.THE_CUBE;
			Minecraft.getInstance().particleEngine.createTrackingEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);

			int amount = 50;

			for (int counter = 0; counter <= amount; counter++) {
				player.level().addParticle(ParticleTypes.FLAME, true, entity.getX() + (Math.random() - 0.5), entity.getY() + (entity.getBbHeight()/2) + (Math.random() - 0.5), entity.getZ() + (Math.random() - 0.5), (Math.random() - 0.5D) * 0.2, (Math.random() - 0.5D) * 0.2, (Math.random() - 0.5D) * 0.2);
			}

			player.level().playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, entity.getSoundSource(), 1.0F, 1.0F, false);

			if (entity == player) {
				ItemStack stack = SuperpositionHandler.getCurioStack(player, item);

				if (stack == null) {
					stack = new ItemStack(item, 1);
				}

				Minecraft.getInstance().gameRenderer.displayItemActivation(stack);
			}
		}
	}

}
