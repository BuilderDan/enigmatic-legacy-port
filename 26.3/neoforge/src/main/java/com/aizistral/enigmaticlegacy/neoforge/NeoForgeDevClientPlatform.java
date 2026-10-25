package com.aizistral.enigmaticlegacy.neoforge;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.aizistral.enigmaticlegacy.dev.DevClientPlatform;
import com.aizistral.enigmaticlegacy.handlers.EnigmaticKeybindHandler;
import com.aizistral.enigmaticlegacy.neoforge.mixin.AccessorCreativeModeInventoryScreen;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.CreativeTabsScreenPage;
import net.neoforged.neoforge.common.NeoForge;

/** Port-only: NeoForge client hooks for the dev playtest harness. */
public class NeoForgeDevClientPlatform implements DevClientPlatform {
	@Override
	public void onClientTick(Consumer<Minecraft> tick) {
		NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> tick.accept(Minecraft.getInstance()));
	}

	@Override
	public void hideGameMessages(Predicate<String> filter) {
		NeoForge.EVENT_BUS.addListener(ClientChatReceivedEvent.System.class, event -> {
			if (filter.test(event.getMessage().getString())) {
				event.setCanceled(true);
			}
		});
	}

	@Override
	public EnigmaticKeybindHandler keybindHandler() {
		return EnigmaticLegacyNeoForgeClient.KEYBIND_HANDLER;
	}

	@Override
	public InputConstants.Key boundKey(KeyMapping mapping) {
		return mapping.getKey();
	}

	@Override
	public void showCreativePage(CreativeModeInventoryScreen screen, CreativeModeTab tab) {
		// NeoForge splits creative tabs into pages; switch to the one that holds the tab.
		for (CreativeTabsScreenPage page : ((AccessorCreativeModeInventoryScreen) screen).enigmaticlegacy$getPages()) {
			if (page.getVisibleTabs().contains(tab)) {
				screen.setCurrentPage(page);
				return;
			}
		}
	}
}
