package com.aizistral.enigmaticlegacy.fabric;

import com.aizistral.enigmaticlegacy.dev.DevPlaytest;
import com.aizistral.enigmaticlegacy.dev.DevSelfCheck;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

import net.minecraft.world.entity.EntityTypes;
import com.aizistral.enigmaticlegacy.gui.PermadeathScreen;
import com.aizistral.enigmaticlegacy.gui.EnderChestInventoryButton;
import com.aizistral.enigmaticlegacy.gui.ToggleMagnetEffectsButton;
import com.aizistral.enigmaticlegacy.packets.server.PacketEnderRingKey;
import com.aizistral.enigmaticlegacy.packets.server.PacketToggleMagnetEffects;
import com.aizistral.enigmaticlegacy.platform.Services;
import com.aizistral.enigmaticlegacy.packets.server.PacketEnchantingGUI;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import com.aizistral.enigmaticlegacy.client.QuoteHandler;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.items.CursedRing;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.aizistral.enigmaticlegacy.client.BlazingCoreClient;
import com.aizistral.enigmaticlegacy.client.SlotUnlockedToastClient;
import com.aizistral.enigmaticlegacy.client.InfinitumClient;
import com.aizistral.enigmaticlegacy.client.EnigmaticItemModelProperties;
import com.aizistral.enigmaticlegacy.client.EnigmaticElytraClient;
import com.aizistral.enigmaticlegacy.client.renderers.EnigmaticElytraLayer;
import com.aizistral.enigmaticlegacy.items.EnigmaticElytra;
import com.aizistral.enigmaticlegacy.packets.clients.PacketCosmicRevive;
import com.aizistral.enigmaticlegacy.config.OmniconfigSync;
import com.aizistral.enigmaticlegacy.fabric.platform.client.FabricClientNetwork;
import com.aizistral.enigmaticlegacy.client.renderers.EndAnchorRenderer;
import com.aizistral.enigmaticlegacy.client.renderers.PermanentItemRenderer;
import com.aizistral.enigmaticlegacy.client.renderers.UltimateWitherSkullRenderer;
import com.aizistral.enigmaticlegacy.handlers.EnigmaticKeybindHandler;
import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.items.EnigmaticAmulet;
import com.aizistral.enigmaticlegacy.registries.EnigmaticBlocks;
import com.aizistral.enigmaticlegacy.registries.EnigmaticEntities;
import com.aizistral.enigmaticlegacy.registries.EnigmaticTiles;
import com.aizistral.enigmaticlegacy.registries.EnigmaticMenus;
import com.aizistral.enigmaticlegacy.gui.containers.LoreInscriberScreen;
import com.aizistral.enigmaticlegacy.helpers.PotionHelper;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.etherium.client.ShieldAuraLayer;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.Identifier;

/**
 * Fabric client entry point (unofficial port, see CHANGES.md).
 */
public class EnigmaticLegacyFabricClient implements ClientModInitializer {
	/** Port: 26.3 key categories are registered records; the lang key is key.category.enigmaticlegacy.main. */
	private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, "main"));
	public static final EnigmaticKeybindHandler KEYBIND_HANDLER = new EnigmaticKeybindHandler();

	@Override
	public void onInitializeClient() {
		EnigmaticLegacyFabric.LOGGER.info("Enigmatic Legacy (Fabric port) client initializing");
		FabricClientNetwork.registerReceivers();
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			OmniconfigSync.onClientLoggedOut();
			BlazingCoreClient.onDisconnect();
			InfinitumClient.onDisconnect();
			EnigmaticElytraClient.onDisconnect();
			SlotUnlockedToastClient.onDisconnect();
		});
		ClientTickEvents.END_CLIENT_TICK.register(BlazingCoreClient::onClientTick);
		ClientTickEvents.END_CLIENT_TICK.register(InfinitumClient::onClientTick);
		ClientTickEvents.END_CLIENT_TICK.register(PacketCosmicRevive::onClientTick);
		ClientTickEvents.END_CLIENT_TICK.register(EnigmaticElytraClient::onClientTick);
		ClientTickEvents.END_CLIENT_TICK.register(SlotUnlockedToastClient::onClientTick);
		ClientTickEvents.END_CLIENT_TICK.register(QuoteHandler.INSTANCE::onClientTick);
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, "quote_subtitles"),
				(graphics, delta) -> QuoteHandler.INSTANCE.onOverlayRender(graphics));
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) ->
				ScreenEvents.afterExtract(screen).register((s, graphics, mouseX, mouseY, partialTick) ->
						QuoteHandler.INSTANCE.onScreenRender(graphics)));
		EnigmaticElytra.clientBoostHandler = EnigmaticElytraClient::handleBoosting;
		registerKeybinds();
		registerInventoryButtons();
		registerPermadeathScreenLock();
		// Port: item properties are 26.3 item model definitions; the blocking state uses minecraft:using_item.
		EnigmaticItemModelProperties.register();
		registerRenderers();
		// Port: forge:separate_transforms models are minecraft:display_context selects in the item definitions.
		DevSelfCheck.register();
		DevPlaytest.register();
	}

	/**
	 * Port: the PermadeathScreen part of upstream EnigmaticEventHandler.onAnvilOpen (ScreenEvent.Init).
	 * While the permadeath screen is active, any other screen that opens is replaced by it.
	 */
	private static void registerPermadeathScreenLock() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (PermadeathScreen.active != null && screen != PermadeathScreen.active) {
				client.gui.setScreen(PermadeathScreen.active);
			}
		});
	}

	/**
	 * Port: upstream EnigmaticEventHandler.onInventoryGuiInit (ScreenEvent.Init.Post). The Quark
	 * mini-button shift and Quark matrix enchanter check are dropped (Quark has no Fabric version).
	 */
	private static void registerInventoryButtons() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof EnchantmentScreen) {
				Services.NETWORK.sendToServer(new PacketEnchantingGUI());
			}

			if (!(screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen))
				return;

			AbstractContainerScreen<?> gui = (AbstractContainerScreen<?>) screen;
			boolean isCreative = screen instanceof CreativeModeInventoryScreen;
			Identifier texture = Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, "textures/gui/ender_chest_button.png");

			EnderChestInventoryButton enderButton = new EnderChestInventoryButton(gui, 0, 0, 20, 18, 0, 0, 19, texture,
					input -> Services.NETWORK.sendToServer(new PacketEnderRingKey(true)));
			enderButton.updatePosition(isCreative);
			Screens.getWidgets(screen).add(enderButton);

			ToggleMagnetEffectsButton magnetButton = new ToggleMagnetEffectsButton(gui, 0, 0, 20, 18, 21, 0, 19, texture,
					input -> Services.NETWORK.sendToServer(new PacketToggleMagnetEffects()));
			magnetButton.updatePosition(isCreative);
			Screens.getWidgets(screen).add(magnetButton);
		});
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void registerRenderers() {
		// Port: upstream adds ShieldAuraLayer to the player skin renderers in EntityRenderersEvent.AddLayers.
		LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			if (entityType == EntityTypes.PLAYER) {
				registrationHelper.register(new ShieldAuraLayer((LivingEntityRenderer) entityRenderer));
			}

			// Port: upstream adds EnigmaticElytraLayer to players and armor stands in AddLayers.
			if (entityType == EntityTypes.PLAYER || entityType == EntityTypes.ARMOR_STAND) {
				registrationHelper.register(new EnigmaticElytraLayer((LivingEntityRenderer) entityRenderer, context.getModelSet()));
			}
		});

		// Port: Etherium Armor textures are fully transparent upstream, so the armor is invisible.
		// A no-op renderer gives the same result and skips the vanilla texture lookup, which
		// cannot parse the namespaced material name "enigmaticlegacy:etherium".
		ArmorRenderer.register((matrices, vertexConsumers, stack, entity, slot, light, model) -> {},
				EnigmaticItems.ETHERIUM_HELMET, EnigmaticItems.ETHERIUM_CHESTPLATE,
				EnigmaticItems.ETHERIUM_LEGGINGS, EnigmaticItems.ETHERIUM_BOOTS);
	}

	private static void registerKeybinds() {
		// Port: upstream EnigmaticLegacy.registerKeybinds (RegisterKeyMappingsEvent).
		KEYBIND_HANDLER.enderRingKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.enderRing", InputConstants.KEY_I, KEY_CATEGORY));
		KEYBIND_HANDLER.xpScrollKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.xpScroll", InputConstants.KEY_J, KEY_CATEGORY));
		KEYBIND_HANDLER.spellstoneAbilityKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.spellstoneAbility", InputConstants.KEY_K, KEY_CATEGORY));
		// Port: upstream registers this in EntityRenderersEvent.RegisterRenderers.
		EntityRendererRegistry.register(EnigmaticEntities.ULTIMATE_WITHER_SKULL, UltimateWitherSkullRenderer::new);
		EntityRendererRegistry.register(EnigmaticEntities.PERMANENT_ITEM_ENTITY, PermanentItemRenderer::new);
		MenuScreens.register(EnigmaticMenus.LORE_INSCRIBER_CONTAINER, LoreInscriberScreen::new);
		EntityRendererRegistry.register(EnigmaticEntities.ENIGMATIC_POTION, ThrownItemRenderer::new);

		// Port: upstream registers this in EntityRenderersEvent.RegisterRenderers.
		BlockEntityRenderers.register(EnigmaticTiles.END_ANCHOR, EndAnchorRenderer::new);

		// Port: upstream sets the cutout layer through the models' "render_type"; 26.3 picks the
		// chunk layer from the texture transparency, so no layer map is needed.

		// Port: Fabric color provider instead of RegisterColorHandlersEvent
		// Port: the potion color handler is the enigmaticlegacy:potion tint source in the item definitions.
		ClientTickEvents.START_CLIENT_TICK.register(KEYBIND_HANDLER::onKeyInput);
		// Port: Fabric tooltip callback instead of ItemTooltipEvent (Architect's Favor lines).
		ItemTooltipCallback.EVENT.register((stack, context, type, lines) ->
				EnigmaticItems.COSMIC_SCROLL.onTooltip(stack, Minecraft.getInstance().player, lines));
		// Port: upstream EnigmaticEventHandler.onTooltip (cursed item concealment), same callback.
		ItemTooltipCallback.EVENT.register((stack, context, type, lines) ->
				concealCursedTooltip(stack, Minecraft.getInstance().player, lines));
	}

	private static void concealCursedTooltip(ItemStack stack, @Nullable Player player, List<Component> tooltip) {
		if (player != null && !player.isCreative()) {
			if (stack.is(EnigmaticItems.CURSED_RING)) {
				if (CursedRing.concealAbilities.getValue() && !SuperpositionHandler.isTheCursedOne(player)) {
					SuperpositionHandler.obscureTooltip(tooltip);
				}
			} else if (stack.getItem() instanceof ICursed) {
				if (!SuperpositionHandler.isTheCursedOne(player)) {
					tooltip.replaceAll(component -> {
						if (component.getContents() instanceof TranslatableContents loc) {
							if (loc.getKey().startsWith("tooltip.enigmaticlegacy.cursedOnesOnly"))
								return component;
						}

						return Component.literal(SuperpositionHandler.obscureString(component.getString()))
								.withStyle(component.getStyle());
					});
				}
			}
		}
	}
}
