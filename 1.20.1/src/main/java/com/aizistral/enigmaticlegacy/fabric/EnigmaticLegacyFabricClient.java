package com.aizistral.enigmaticlegacy.fabric;

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

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

/**
 * Fabric client entry point (unofficial port, see CHANGES.md).
 */
public class EnigmaticLegacyFabricClient implements ClientModInitializer {
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
		QuoteHandler.register();
		EnigmaticElytra.clientBoostHandler = EnigmaticElytraClient::handleBoosting;
		registerKeybinds();
		registerInventoryButtons();
		registerPermadeathScreenLock();
		registerItemProperties();
		registerRenderers();
		SeparateTransformsModel.register();
		DevSelfCheck.register();
	}

	/**
	 * Port: the PermadeathScreen part of upstream EnigmaticEventHandler.onAnvilOpen (ScreenEvent.Init).
	 * While the permadeath screen is active, any other screen that opens is replaced by it.
	 */
	private static void registerPermadeathScreenLock() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (PermadeathScreen.active != null && screen != PermadeathScreen.active) {
				client.setScreen(PermadeathScreen.active);
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
			ResourceLocation texture = new ResourceLocation(EnigmaticLegacy.MODID, "textures/gui/ender_chest_button.png");

			EnderChestInventoryButton enderButton = new EnderChestInventoryButton(gui, 0, 0, 20, 18, 0, 0, 19, texture,
					input -> Services.NETWORK.sendToServer(new PacketEnderRingKey(true)));
			enderButton.updatePosition(isCreative);
			Screens.getButtons(screen).add(enderButton);

			ToggleMagnetEffectsButton magnetButton = new ToggleMagnetEffectsButton(gui, 0, 0, 20, 18, 21, 0, 19, texture,
					input -> Services.NETWORK.sendToServer(new PacketToggleMagnetEffects()));
			magnetButton.updatePosition(isCreative);
			Screens.getButtons(screen).add(magnetButton);
		});
	}

	private static void registerItemProperties() {
		// Port: upstream EnigmaticAmulet.registerVariants (FMLClientSetupEvent).
		ItemProperties.register(EnigmaticItems.ENIGMATIC_AMULET, new ResourceLocation(EnigmaticLegacy.MODID, "enigmatic_amulet_color"),
				(stack, world, entity, seed) -> ItemNBTHelper.getFloat(stack, EnigmaticAmulet.amuletColorTag, 0F));
		EnigmaticItems.SOUL_COMPASS.registerVariants();
		EnigmaticItems.ENIGMATIC_EYE.registerVariants();
		// Port: upstream ClientProxy.registerBlockingProperty (FMLClientSetupEvent).
		registerBlockingProperty(EnigmaticItems.INFERNAL_SHIELD);
		registerBlockingProperty(EnigmaticItems.ELDRITCH_PAN);
		// Port: upstream ClientProxy (FMLClientSetupEvent).
		ItemProperties.register(EnigmaticItems.THE_INFINITUM, new ResourceLocation(EnigmaticLegacy.MODID, "the_infinitum_open"),
				InfinitumClient::getOpenProperty);
	}

	private static void registerBlockingProperty(Item item) {
		ItemProperties.register(item, new ResourceLocation("blocking"),
				(stack, world, entity, seed) -> entity != null && entity.isUsingItem()
						&& entity.getUseItem() == stack ? 1 : 0);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void registerRenderers() {
		// Port: upstream adds ShieldAuraLayer to the player skin renderers in EntityRenderersEvent.AddLayers.
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			if (entityType == EntityType.PLAYER) {
				registrationHelper.register(new ShieldAuraLayer((LivingEntityRenderer) entityRenderer, context.getModelSet()));
			}

			// Port: upstream adds EnigmaticElytraLayer to players and armor stands in AddLayers.
			if (entityType == EntityType.PLAYER || entityType == EntityType.ARMOR_STAND) {
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
		KEYBIND_HANDLER.enderRingKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.enderRing", GLFW.GLFW_KEY_I, "key.categories.enigmaticLegacy"));
		KEYBIND_HANDLER.xpScrollKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.xpScroll", GLFW.GLFW_KEY_J, "key.categories.enigmaticLegacy"));
		KEYBIND_HANDLER.spellstoneAbilityKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.spellstoneAbility", GLFW.GLFW_KEY_K, "key.categories.enigmaticLegacy"));
		// Port: upstream registers this in EntityRenderersEvent.RegisterRenderers.
		EntityRendererRegistry.register(EnigmaticEntities.ULTIMATE_WITHER_SKULL, UltimateWitherSkullRenderer::new);
		EntityRendererRegistry.register(EnigmaticEntities.PERMANENT_ITEM_ENTITY, ctx -> new PermanentItemRenderer(ctx, ctx.getItemRenderer()));
		MenuScreens.register(EnigmaticMenus.LORE_INSCRIBER_CONTAINER, LoreInscriberScreen::new);
		EntityRendererRegistry.register(EnigmaticEntities.ENIGMATIC_POTION, ThrownItemRenderer::new);

		// Port: upstream registers this in EntityRenderersEvent.RegisterRenderers.
		BlockEntityRenderers.register(EnigmaticTiles.END_ANCHOR, EndAnchorRenderer::new);

		// Port: Fabric ignores the models' Forge-only "render_type": "cutout", so the layer is set here.
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), EnigmaticBlocks.MASSIVE_LAMP,
				EnigmaticBlocks.MASSIVE_SHROOMLAMP, EnigmaticBlocks.MASSIVE_REDSTONELAMP, EnigmaticBlocks.BIG_LAMP,
				EnigmaticBlocks.BIG_SHROOMLAMP, EnigmaticBlocks.BIG_REDSTONELAMP, EnigmaticBlocks.COSMIC_CAKE,
				EnigmaticBlocks.END_ANCHOR);

		// Port: Fabric color provider instead of RegisterColorHandlersEvent
		ColorProviderRegistry.ITEM.register((stack, color) -> color > 0 ? -1 : PotionHelper.getColor(stack),
				EnigmaticItems.ULTIMATE_POTION, EnigmaticItems.ULTIMATE_POTION_SPLASH,
				EnigmaticItems.ULTIMATE_POTION_LINGERING, EnigmaticItems.COMMON_POTION,
				EnigmaticItems.COMMON_POTION_SPLASH, EnigmaticItems.COMMON_POTION_LINGERING);
		ClientTickEvents.START_CLIENT_TICK.register(KEYBIND_HANDLER::onKeyInput);
		// Port: Fabric tooltip callback instead of ItemTooltipEvent (Architect's Favor lines).
		ItemTooltipCallback.EVENT.register((stack, context, lines) ->
				EnigmaticItems.COSMIC_SCROLL.onTooltip(stack, Minecraft.getInstance().player, lines));
		// Port: upstream EnigmaticEventHandler.onTooltip (cursed item concealment), same callback.
		ItemTooltipCallback.EVENT.register((stack, context, lines) ->
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
