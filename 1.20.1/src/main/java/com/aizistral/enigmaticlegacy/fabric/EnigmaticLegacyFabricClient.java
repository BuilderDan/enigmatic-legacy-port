package com.aizistral.enigmaticlegacy.fabric;

import com.aizistral.enigmaticlegacy.config.OmniconfigSync;
import com.aizistral.enigmaticlegacy.fabric.platform.client.FabricClientNetwork;
import com.aizistral.enigmaticlegacy.handlers.EnigmaticKeybindHandler;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;

/**
 * Fabric client entry point (unofficial port, see CHANGES.md).
 */
public class EnigmaticLegacyFabricClient implements ClientModInitializer {
	public static final EnigmaticKeybindHandler KEYBIND_HANDLER = new EnigmaticKeybindHandler();

	@Override
	public void onInitializeClient() {
		EnigmaticLegacyFabric.LOGGER.info("Enigmatic Legacy (Fabric port) client initializing");
		FabricClientNetwork.registerReceivers();
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> OmniconfigSync.onClientLoggedOut());
		registerKeybinds();
		DevSelfCheck.register();
	}

	private static void registerKeybinds() {
		// Port: upstream EnigmaticLegacy.registerKeybinds (RegisterKeyMappingsEvent).
		KEYBIND_HANDLER.enderRingKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.enderRing", GLFW.GLFW_KEY_I, "key.categories.enigmaticLegacy"));
		KEYBIND_HANDLER.xpScrollKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.xpScroll", GLFW.GLFW_KEY_J, "key.categories.enigmaticLegacy"));
		ClientTickEvents.START_CLIENT_TICK.register(KEYBIND_HANDLER::onKeyInput);
	}
}
