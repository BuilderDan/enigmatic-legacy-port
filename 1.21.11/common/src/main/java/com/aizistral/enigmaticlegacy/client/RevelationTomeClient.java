package com.aizistral.enigmaticlegacy.client;

import net.minecraft.resources.Identifier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;

/**
 * Port: upstream's ClientProxy.pushRevelationToast and its RevelationTomeToast, in one
 * client-only class. RevelationTome only calls into it on the logical client.
 */
public class RevelationTomeClient {

	public static void pushToast(ItemStack tome, int xp, int revelation) {
		Minecraft.getInstance().getToastManager().addToast(new RevelationTomeToast(tome, xp, revelation));
	}

	public static class RevelationTomeToast implements Toast {
		private final ItemStack tome;
		private final int xpPoints;
		private final int revelationPoints;
		private Visibility visibility = Visibility.HIDE;

		public RevelationTomeToast(ItemStack tome, int xp, int revelation) {
			this.tome = tome.copy();
			this.xpPoints = xp;
			this.revelationPoints = revelation;
		}

		// Port: 1.21.11 toasts split upstream's render into update (visibility) and render (drawing).
		@Override
		public Visibility getWantedVisibility() {
			return this.visibility;
		}

		@Override
		public void update(ToastManager manager, long delta) {
			this.visibility = delta >= 5000L ? Visibility.HIDE : Visibility.SHOW;
		}

		@Override
		public void render(GuiGraphics graphics, Font font, long delta) {
			// Port: 1.21 toasts use sprites; upstream drew the recipe toast row of the old atlas.
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("toast/recipe"), 0, 0, 160, 32);

			graphics.drawString(font, I18n.get("enigmaticlegacy.toasts.revelationTome.title", this.xpPoints), 30, 7, -11534256);
			graphics.drawString(font, I18n.get("enigmaticlegacy.toasts.revelationTome.text", this.revelationPoints), 30, 17, -16777216);
			graphics.renderFakeItem(this.tome, 8, 8);
		}
	}

}
