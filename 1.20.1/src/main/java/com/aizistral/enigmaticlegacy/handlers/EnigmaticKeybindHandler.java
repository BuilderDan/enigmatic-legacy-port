package com.aizistral.enigmaticlegacy.handlers;

import com.aizistral.enigmaticlegacy.packets.server.PacketEnderRingKey;
import com.aizistral.enigmaticlegacy.packets.server.PacketSpellstoneKey;
import com.aizistral.enigmaticlegacy.packets.server.PacketXPScrollKey;
import com.aizistral.enigmaticlegacy.platform.Services;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

/**
 * Class for creating and handling keybinds on the client.
 * @author Integral
 */

// Port: the loader registers the key mappings and calls onKeyInput at the start of each client tick
// (upstream: RegisterKeyMappingsEvent and TickEvent.ClientTickEvent). The Angel's Blessing
// double jump returns with its item.
public class EnigmaticKeybindHandler {
	public KeyMapping enderRingKey;
	public KeyMapping xpScrollKey;
	public KeyMapping spellstoneAbilityKey;

	public void onKeyInput(Minecraft minecraft) {
		if (!minecraft.isWindowActive() || minecraft.player == null)
			return;

		if (this.enderRingKey.consumeClick()) {
			Services.NETWORK.sendToServer(new PacketEnderRingKey(true));
		}

		if (this.xpScrollKey.consumeClick()) {
			Services.NETWORK.sendToServer(new PacketXPScrollKey(true));
		}

		if (this.spellstoneAbilityKey.isDown() && SuperpositionHandler.hasCurio(minecraft.player, EnigmaticItems.ENIGMATIC_ITEM)) {
			Services.NETWORK.sendToServer(new PacketSpellstoneKey(true));
		} else if (this.spellstoneAbilityKey.consumeClick() && SuperpositionHandler.hasSpellstone(minecraft.player)) {
			Services.NETWORK.sendToServer(new PacketSpellstoneKey(true));
		}
	}

}
