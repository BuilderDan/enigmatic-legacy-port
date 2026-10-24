package com.aizistral.enigmaticlegacy.gui;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import com.aizistral.enigmaticlegacy.mixin.AccessorAbstractContainerScreen;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;

// Port: CuriosScreen checks dropped (Trinkets adds its slots to the vanilla inventory screen);
// getGuiLeft/getGuiTop go through AccessorAbstractContainerScreen.
// Port: extends Button, since 1.21 ImageButton only takes WidgetSprites; renderWidget draws the texture as before.
public abstract class PlayerInventoryButton extends Button {
	protected final AbstractContainerScreen<?> parentGui;
	protected final ResourceLocation resourceLocation;
	protected int xTexStart = 0, yTexStart = 0, textureWidth = 0, textureHeight = 0, yDiffTex = 0;
	protected boolean isRecipeBookVisible = false;

	public PlayerInventoryButton(AbstractContainerScreen<?> gui, int xIn, int yIn, int widthIn, int heightIn, int xTexStartIn, int yTexStartIn, int yDiffTextIn, ResourceLocation resourceLocationIn, Button.OnPress onPressIn) {
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
	public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.active = true;

		if (this.parentGui instanceof InventoryScreen inventory) {
			boolean lastVisible = this.isRecipeBookVisible;
			this.isRecipeBookVisible = inventory.getRecipeBookComponent().isVisible();

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
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, this.resourceLocation);
			int i = this.yTexStart;
			if (this.isHoveredOrFocused()) {
				i += this.yDiffTex;
			}

			RenderSystem.enableDepthTest();
			graphics.blit(this.resourceLocation, this.getX(), this.getY(), this.xTexStart, i, this.width, this.height,
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
