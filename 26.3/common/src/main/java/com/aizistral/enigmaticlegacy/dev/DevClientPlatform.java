package com.aizistral.enigmaticlegacy.dev;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticKeybindHandler;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;

/**
 * Port-only, dev clients only. The client half of {@link DevPlatform}, kept apart so a dedicated
 * server never loads client classes.
 */
public interface DevClientPlatform {
	void onClientTick(Consumer<Minecraft> tick);

	/** Hides incoming game (non-chat) messages the filter accepts. */
	void hideGameMessages(Predicate<String> filter);

	EnigmaticKeybindHandler keybindHandler();

	InputConstants.Key boundKey(KeyMapping mapping);

	/** Shows the creative page that holds the tab; the harness then selects the tab itself. */
	void showCreativePage(CreativeModeInventoryScreen screen, CreativeModeTab tab);
}
