package com.aizistral.enigmaticlegacy.client;

import java.util.ArrayList;
import java.util.List;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Port: upstream's PacketSlotUnlocked client handler, its SlotUnlockedToast, and the toast
 * queue from EnigmaticEventHandler (SCHEDULED_TOASTS, DEFERRED_TOASTS, client tick), in one
 * client-only class. A plain tick counter stands in for upstream's per-player CooldownMap.
 */
public class SlotUnlockedToastClient {
	private static final List<Toast> SCHEDULED_TOASTS = new ArrayList<>();
	private static int deferTicks = 0;

	public static void onSlotUnlocked(String type) {
		ItemStack stack;

		if (type.equals("ring"))
			stack = new ItemStack(EnigmaticItems.IRON_RING);
		else if (type.equals("scroll"))
			stack = new ItemStack(EnigmaticItems.THICC_SCROLL);
		else if (type.equals("spellstone"))
			stack = new ItemStack(EnigmaticItems.VOID_PEARL);
		else
			stack = new ItemStack(EnigmaticItems.IRON_RING);

		SCHEDULED_TOASTS.add(new SlotUnlockedToast(stack, type));
		deferTicks = 5;
	}

	public static void onClientTick(Minecraft client) {
		if (client.player == null || deferTicks <= 0)
			return;

		if (--deferTicks == 1 && !SCHEDULED_TOASTS.isEmpty()) {
			client.gui.toastManager().addToast(SCHEDULED_TOASTS.remove(0));

			if (!SCHEDULED_TOASTS.isEmpty()) {
				deferTicks = 5;
			}
		}
	}

	public static void onDisconnect() {
		SCHEDULED_TOASTS.clear();
		deferTicks = 0;
	}

	/**
	 * Special type of Toast that alerts player when they unlock new Curio slot.
	 * @author Integral
	 */
	public static class SlotUnlockedToast implements Toast {
		private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, "textures/gui/enigmatic_toasts.png");
		private final ItemStack drawnStack;
		private final String identifier;
		private Visibility visibility = Visibility.HIDE;

		public SlotUnlockedToast(ItemStack stack, String id) {
			this.drawnStack = stack;
			this.identifier = id;
		}

		// Port: 26.3 toasts split upstream's render into update (visibility) and extractRenderState (drawing).
		@Override
		public Visibility getWantedVisibility() {
			return this.visibility;
		}

		@Override
		public void update(ToastManager manager, long delta) {
			this.visibility = delta >= 5000L ? Visibility.HIDE : Visibility.SHOW;
		}

		@Override
		public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long delta) {
			graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, 0, 0, 160, 43, 256, 256);
			graphics.text(font, I18n.get("enigmaticlegacy.toasts.slotUnlocked.title",
					I18n.get("enigmaticlegacy.curiotype." + this.identifier)), 7, 7, -11534256);
			graphics.text(font, I18n.get("enigmaticlegacy.toasts.slotUnlocked.text1"), 30, 18, -16777216);
			graphics.text(font, I18n.get("enigmaticlegacy.toasts.slotUnlocked.text2"), 30, 28, -16777216);
			graphics.fakeItem(this.drawnStack, 8, 18);
		}

		// Port: upstream's 43-pixel texture region; vanilla's default height (32) would overlap the next toast.
		@Override
		public int height() {
			return 43;
		}
	}

}
