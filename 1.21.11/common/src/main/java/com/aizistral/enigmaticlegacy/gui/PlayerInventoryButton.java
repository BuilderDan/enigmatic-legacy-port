package com.aizistral.enigmaticlegacy.gui;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import com.aizistral.enigmaticlegacy.mixin.AccessorAbstractContainerScreen;
import com.aizistral.enigmaticlegacy.mixin.AccessorAbstractRecipeBookScreen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import com.aizistral.enigmaticlegacy.api.generic.Tuple;

// Port: CuriosScreen checks dropped (Trinkets adds its slots to the vanilla inventory screen);
// getGuiLeft/getGuiTop go through AccessorAbstractContainerScreen.
// Port: extends Button, since 1.21 ImageButton only takes WidgetSprites; extractContents (26.3 renderWidget) draws the texture as before.
public abstract class PlayerInventoryButton extends Button {
	protected final AbstractContainerScreen<?> parentGui;
	protected final Identifier resourceLocation;
	protected int xTexStart = 0, yTexStart = 0, textureWidth = 0, textureHeight = 0, yDiffTex = 0;
	protected boolean isRecipeBookVisible = false;

	public PlayerInventoryButton(AbstractContainerScreen<?> gui, int xIn, int yIn, int widthIn, int heightIn, int xTexStartIn, int yTexStartIn, int yDiffTextIn, Identifier resourceLocationIn, Button.OnPress onPressIn) {
		super(xIn, yIn, widthIn, heightIn, Component.empty(), onPressIn, DEFAULT_NARRATION);
		this.parentGui = gui;

		this.resourceLocation = resourceLocationIn;
		this.xTexStart = xTexStartIn;
		this.yTexStart = yTexStartIn;
		this.textureWidth = 256;
		this.textureHeight = 256;
		this.yDiffTex = yDiffTextIn;
	}

	@Override
	protected void renderContents(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.active = true;

		if (this.parentGui instanceof InventoryScreen inventory) {
			boolean lastVisible = this.isRecipeBookVisible;
			this.isRecipeBookVisible = ((AccessorAbstractRecipeBookScreen) inventory).enigmaticlegacy$getRecipeBookComponent().isVisible();

			if (lastVisible != this.isRecipeBookVisible) {
				this.updatePosition(false);
			}
		} else if (this.parentGui instanceof CreativeModeInventoryScreen gui) {
			if (!gui.isInventoryOpen()) {
				this.active = false;
				return;
			}
		}

		if (this.beforeRender(graphics, mouseX, mouseY, partialTicks)) {
			int i = this.yTexStart;
			if (this.isHoveredOrFocused()) {
				i += this.yDiffTex;
			}

			graphics.blit(RenderPipelines.GUI_TEXTURED, this.resourceLocation, this.getX(), this.getY(), this.xTexStart, i, this.width, this.height,
					this.textureWidth, this.textureHeight);
		}
	}

	/** Port-only: upstream repeats this in renderWidget and in its ScreenEvent handler. */
	public void updatePosition(boolean creative) {
		Tuple<Integer, Integer> offsets = this.getOffsets(creative);
		AccessorAbstractContainerScreen gui = (AccessorAbstractContainerScreen) this.parentGui;
		this.setPosition(gui.enigmaticlegacy$getLeftPos() + offsets.getA(), gui.enigmaticlegacy$getTopPos() + offsets.getB());
	}

	protected abstract boolean beforeRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks);

	public abstract Tuple<Integer, Integer> getOffsets(boolean creative);

}
