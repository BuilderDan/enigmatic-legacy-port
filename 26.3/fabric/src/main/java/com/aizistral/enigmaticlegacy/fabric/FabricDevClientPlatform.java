package com.aizistral.enigmaticlegacy.fabric;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.aizistral.enigmaticlegacy.dev.DevClientPlatform;
import com.aizistral.enigmaticlegacy.handlers.EnigmaticKeybindHandler;
import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.creativetab.v1.FabricCreativeModeInventoryScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;

/** Port-only: Fabric client hooks for the dev playtest harness. */
public class FabricDevClientPlatform implements DevClientPlatform {
	@Override
	public void onClientTick(Consumer<Minecraft> tick) {
		ClientTickEvents.END_CLIENT_TICK.register(tick::accept);
	}

	@Override
	public void hideGameMessages(Predicate<String> filter) {
		ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> !filter.test(message.getString()));
	}

	@Override
	public EnigmaticKeybindHandler keybindHandler() {
		return EnigmaticLegacyFabricClient.KEYBIND_HANDLER;
	}

	@Override
	public InputConstants.Key boundKey(KeyMapping mapping) {
		return KeyMappingHelper.getBoundKeyOf(mapping);
	}

	@Override
	public void showCreativePage(CreativeModeInventoryScreen screen, CreativeModeTab tab) {
		FabricCreativeModeInventoryScreen fabric = (FabricCreativeModeInventoryScreen) screen;
		fabric.switchToPage(fabric.getPage(tab));
	}
}
