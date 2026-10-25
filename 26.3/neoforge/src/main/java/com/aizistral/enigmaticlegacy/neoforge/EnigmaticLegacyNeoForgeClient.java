package com.aizistral.enigmaticlegacy.neoforge;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.client.BlazingCoreClient;
import com.aizistral.enigmaticlegacy.client.EnigmaticElytraClient;
import com.aizistral.enigmaticlegacy.client.EnigmaticItemModelProperties;
import com.aizistral.enigmaticlegacy.client.InfinitumClient;
import com.aizistral.enigmaticlegacy.client.QuoteHandler;
import com.aizistral.enigmaticlegacy.client.SlotUnlockedToastClient;
import com.aizistral.enigmaticlegacy.client.renderers.EndAnchorRenderer;
import com.aizistral.enigmaticlegacy.client.renderers.EnigmaticElytraLayer;
import com.aizistral.enigmaticlegacy.client.renderers.PermanentItemRenderer;
import com.aizistral.enigmaticlegacy.client.renderers.UltimateWitherSkullRenderer;
import com.aizistral.enigmaticlegacy.config.OmniconfigSync;
import com.aizistral.enigmaticlegacy.dev.DevPlaytest;
import com.aizistral.enigmaticlegacy.dev.DevSelfCheck;
import com.aizistral.enigmaticlegacy.gui.EnderChestInventoryButton;
import com.aizistral.enigmaticlegacy.gui.PermadeathScreen;
import com.aizistral.enigmaticlegacy.gui.ToggleMagnetEffectsButton;
import com.aizistral.enigmaticlegacy.gui.containers.LoreInscriberScreen;
import com.aizistral.enigmaticlegacy.handlers.EnigmaticKeybindHandler;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.items.CursedRing;
import com.aizistral.enigmaticlegacy.items.EnigmaticElytra;
import com.aizistral.enigmaticlegacy.neoforge.platform.NeoForgeNetwork;
import com.aizistral.enigmaticlegacy.packets.clients.PacketCosmicRevive;
import com.aizistral.enigmaticlegacy.packets.server.PacketEnchantingGUI;
import com.aizistral.enigmaticlegacy.packets.server.PacketEnderRingKey;
import com.aizistral.enigmaticlegacy.packets.server.PacketToggleMagnetEffects;
import com.aizistral.enigmaticlegacy.platform.Services;
import com.aizistral.enigmaticlegacy.platform.network.PacketContext;
import com.aizistral.enigmaticlegacy.registries.EnigmaticEntities;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.enigmaticlegacy.registries.EnigmaticMenus;
import com.aizistral.enigmaticlegacy.registries.EnigmaticTiles;
import com.aizistral.etherium.client.ShieldAuraLayer;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * NeoForge client entry point (unofficial port, see CHANGES.md). Mirrors the Fabric client
 * entry point; upstream Forge used the same events.
 */
public class EnigmaticLegacyNeoForgeClient {
	/** Port: 26.3 key categories are records; the lang key is key.category.enigmaticlegacy.main. */
	private static final KeyMapping.Category KEY_CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, "main"));
	public static final EnigmaticKeybindHandler KEYBIND_HANDLER = new EnigmaticKeybindHandler();

	public static void init(IEventBus modBus) {
		EnigmaticLegacyNeoForge.LOGGER.info("Enigmatic Legacy (NeoForge port) client initializing");
		NeoForgeNetwork.clientSender = payload -> ClientPacketDistributor.sendToServer(payload);

		modBus.addListener(RegisterClientPayloadHandlersEvent.class, event -> NeoForgeNetwork.CLIENT_BOUND
				.forEach(packet -> registerClientHandler(event, packet)));
		modBus.addListener(RegisterKeyMappingsEvent.class, EnigmaticLegacyNeoForgeClient::registerKeybinds);
		modBus.addListener(RegisterGuiLayersEvent.class, event -> event.registerAboveAll(
				Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, "quote_subtitles"),
				(graphics, delta) -> QuoteHandler.INSTANCE.onOverlayRender(graphics)));
		modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, EnigmaticLegacyNeoForgeClient::registerRenderers);
		modBus.addListener(EntityRenderersEvent.AddLayers.class, EnigmaticLegacyNeoForgeClient::registerLayers);
		modBus.addListener(RegisterMenuScreensEvent.class, event ->
				event.register(EnigmaticMenus.LORE_INSCRIBER_CONTAINER, LoreInscriberScreen::new));

		IEventBus bus = NeoForge.EVENT_BUS;
		bus.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> {
			OmniconfigSync.onClientLoggedOut();
			BlazingCoreClient.onDisconnect();
			InfinitumClient.onDisconnect();
			EnigmaticElytraClient.onDisconnect();
			SlotUnlockedToastClient.onDisconnect();
		});
		bus.addListener(ClientTickEvent.Pre.class, event -> KEYBIND_HANDLER.onKeyInput(Minecraft.getInstance()));
		bus.addListener(ClientTickEvent.Post.class, event -> {
			Minecraft client = Minecraft.getInstance();
			BlazingCoreClient.onClientTick(client);
			InfinitumClient.onClientTick(client);
			PacketCosmicRevive.onClientTick(client);
			EnigmaticElytraClient.onClientTick(client);
			SlotUnlockedToastClient.onClientTick(client);
			QuoteHandler.INSTANCE.onClientTick(client);
		});
		bus.addListener(ScreenEvent.Init.Post.class, EnigmaticLegacyNeoForgeClient::onScreenInit);
		bus.addListener(ScreenEvent.Render.Post.class, event -> QuoteHandler.INSTANCE.onScreenRender(event.getGuiGraphics()));
		// Port: upstream EnigmaticEventHandler.onTooltip (Architect's Favor lines, cursed item concealment).
		bus.addListener(ItemTooltipEvent.class, event -> {
			EnigmaticItems.COSMIC_SCROLL.onTooltip(event.getItemStack(), event.getEntity(), event.getToolTip());
			concealCursedTooltip(event.getItemStack(), event.getEntity(), event.getToolTip());
		});

		EnigmaticElytra.clientBoostHandler = EnigmaticElytraClient::handleBoosting;
		// Port: item properties are 26.3 item model definitions; the blocking state uses minecraft:using_item.
		EnigmaticItemModelProperties.register();
		DevSelfCheck.register();
		DevPlaytest.register();
	}

	private static <M> void registerClientHandler(RegisterClientPayloadHandlersEvent event, NeoForgeNetwork.Packet<M> packet) {
		event.register(packet.type(), (payload, ctx) -> {
			var context = new PacketContext(Minecraft.getInstance(), null);
			packet.handler().accept(payload.msg(), () -> context);
		});
	}

	private static void registerKeybinds(RegisterKeyMappingsEvent event) {
		// Port: upstream EnigmaticLegacy.registerKeybinds.
		event.registerCategory(KEY_CATEGORY);
		KEYBIND_HANDLER.enderRingKey = new KeyMapping("key.enderRing", InputConstants.KEY_I, KEY_CATEGORY);
		KEYBIND_HANDLER.xpScrollKey = new KeyMapping("key.xpScroll", InputConstants.KEY_J, KEY_CATEGORY);
		KEYBIND_HANDLER.spellstoneAbilityKey = new KeyMapping("key.spellstoneAbility", InputConstants.KEY_K, KEY_CATEGORY);
		event.register(KEYBIND_HANDLER.enderRingKey);
		event.register(KEYBIND_HANDLER.xpScrollKey);
		event.register(KEYBIND_HANDLER.spellstoneAbilityKey);
	}

	private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(EnigmaticEntities.ULTIMATE_WITHER_SKULL, UltimateWitherSkullRenderer::new);
		event.registerEntityRenderer(EnigmaticEntities.PERMANENT_ITEM_ENTITY, PermanentItemRenderer::new);
		event.registerEntityRenderer(EnigmaticEntities.ENIGMATIC_POTION, ThrownItemRenderer::new);
		event.registerBlockEntityRenderer(EnigmaticTiles.END_ANCHOR, EndAnchorRenderer::new);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void registerLayers(EntityRenderersEvent.AddLayers event) {
		for (PlayerModelType skin : event.getSkins()) {
			LivingEntityRenderer renderer = event.getPlayerRenderer(skin);

			if (renderer != null) {
				renderer.addLayer(new ShieldAuraLayer(renderer));
				renderer.addLayer(new EnigmaticElytraLayer(renderer, event.getEntityModels()));
			}
		}

		if (event.getRenderer(EntityTypes.ARMOR_STAND) instanceof LivingEntityRenderer armorStand) {
			armorStand.addLayer(new EnigmaticElytraLayer(armorStand, event.getEntityModels()));
		}
	}

	/**
	 * Port: upstream EnigmaticEventHandler.onAnvilOpen (permadeath screen lock) and
	 * onInventoryGuiInit (inventory buttons). The Quark mini-button shift is dropped.
	 */
	private static void onScreenInit(ScreenEvent.Init.Post event) {
		var screen = event.getScreen();

		if (PermadeathScreen.active != null && screen != PermadeathScreen.active) {
			Minecraft.getInstance().gui.setScreen(PermadeathScreen.active);
		}

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
		event.addListener(enderButton);

		ToggleMagnetEffectsButton magnetButton = new ToggleMagnetEffectsButton(gui, 0, 0, 20, 18, 21, 0, 19, texture,
				input -> Services.NETWORK.sendToServer(new PacketToggleMagnetEffects()));
		magnetButton.updatePosition(isCreative);
		event.addListener(magnetButton);
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
