package com.aizistral.enigmaticlegacy.client;

import com.aizistral.enigmaticlegacy.items.EnigmaticElytra;
import com.aizistral.enigmaticlegacy.objects.ElytraBoostData;
import com.aizistral.enigmaticlegacy.objects.Vector3;
import com.aizistral.enigmaticlegacy.packets.server.PacketUpdateElytraBoosting;
import com.aizistral.enigmaticlegacy.platform.Services;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;

/**
 * Port: the client parts of upstream's EnigmaticElytra (handleBoosting and the client
 * {@code PlayerTickEvent} handler). handleBoosting is wired into
 * {@link EnigmaticElytra#clientBoostHandler}; the particle tick runs from the Fabric client tick.
 */
public class EnigmaticElytraClient {
	private static boolean isBoosting;

	public static void handleBoosting(Player player) {
		if (Minecraft.getInstance().player != player)
			return;

		if (Minecraft.getInstance().options.keyJump.isDown() && EnigmaticElytra.boostPlayer(player)) {
			if (!isBoosting) {
				Services.NETWORK.sendToServer(new PacketUpdateElytraBoosting(isBoosting = true));
			}
		} else {
			if (isBoosting) {
				Services.NETWORK.sendToServer(new PacketUpdateElytraBoosting(isBoosting = false));
			}
		}
	}

	public static void onClientTick(Minecraft mc) {
		if (mc.level == null || mc.isPaused())
			return;

		for (Player player : mc.level.players()) {
			if (!ElytraBoostData.isBoosting(player)) {
				continue;
			}

			if (!player.isFallFlying()) {
				if (player == mc.player) {
					handleBoosting(player);
				} else {
					ElytraBoostData.setClient(player.getUUID(), false);
				}

				continue;
			}

			int amount = 3;
			double rangeModifier = 0.1;

			for (int counter = 0; counter <= amount; counter++) {
				Vector3 vec = Vector3.fromEntityCenter(player);
				vec = vec.add(Math.random() - 0.5, -1.0 + Math.random() - 0.5, Math.random() - 0.5);
				player.level().addParticle(ParticleTypes.DRAGON_BREATH, true, vec.x, vec.y, vec.z, ((Math.random()-0.5D)*2.0D)*rangeModifier, ((Math.random()-0.5D)*2.0D)*rangeModifier, ((Math.random()-0.5D)*2.0D)*rangeModifier);
			}
		}
	}

	public static void onDisconnect() {
		isBoosting = false;
		ElytraBoostData.clearClient();
	}
}
