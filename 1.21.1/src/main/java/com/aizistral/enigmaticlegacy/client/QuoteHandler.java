package com.aizistral.enigmaticlegacy.client;

import java.util.Arrays;

import com.aizistral.enigmaticlegacy.config.OmniconfigHandler;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

public class QuoteHandler {
	public static final QuoteHandler INSTANCE = new QuoteHandler();
	private static final RandomSource RANDOM = RandomSource.create();
	private Quote currentQuote = null;
	private long startedPlaying = -1;
	private int delayTicks = -1;
	private boolean shownExperimentalInfo = false;

	private QuoteHandler() {
		// NO-OP
	}

	/**
	 * Port: upstream subscribes to Forge's PlayerTickEvent, RenderGuiEvent.Post and
	 * ScreenEvent.Render.Post; these are the Fabric equivalents.
	 */
	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(INSTANCE::onClientTick);
		HudRenderCallback.EVENT.register((graphics, partialTick) -> INSTANCE.onOverlayRender(graphics));
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) ->
		ScreenEvents.afterRender(screen).register((s, graphics, mouseX, mouseY, partialTick) ->
		INSTANCE.onScreenRender(graphics)));
	}

	private double getPlayTime() {
		long millis = System.currentTimeMillis() - this.startedPlaying;
		return ((double)millis) / 1000;
	}

	public void playQuote(Quote quote, int delayTicks) {
		if (this.currentQuote == null) {
			this.currentQuote = quote;
			this.delayTicks = delayTicks;
		}
	}

	/**
	 * Port: upstream's PlayerTickEvent fires twice per player tick (START and END phases)
	 * and has no phase check, so the delay counts down twice per tick. The port keeps that
	 * timing by counting down twice per client tick, and skips paused ticks like upstream.
	 */
	private void onClientTick(Minecraft minecraft) {
		if (minecraft.player == null || minecraft.isPaused())
			return;

		for (int i = 0; i < 2; i++) {
			this.onPlayerTick(minecraft);
		}
	}

	private void onPlayerTick(Minecraft minecraft) {
		if (this.delayTicks > 0 && !(minecraft.screen instanceof LevelLoadingScreen)
				&& !(minecraft.screen instanceof ReceivingLevelScreen)) {
			this.delayTicks--;

			if (this.delayTicks == 0) {
				SimpleSoundInstance instance = new SimpleSoundInstance(this.currentQuote.getSound().getLocation(),
						SoundSource.VOICE, 0.7F, 1, RANDOM, false, 0, SoundInstance.Attenuation.NONE, 0, 0, 0, true);

				minecraft.getSoundManager().play(instance);

				this.startedPlaying = System.currentTimeMillis();
			}
		}
	}

	private void onOverlayRender(GuiGraphics graphics) {
		if (Minecraft.getInstance().screen != null || this.currentQuote == null || this.delayTicks > 0)
			return;

		this.drawQuote(graphics, Minecraft.getInstance().getWindow());
	}

	private void onScreenRender(GuiGraphics graphics) {
		if (this.currentQuote != null && this.delayTicks <= 0) {
			this.drawQuote(graphics, Minecraft.getInstance().getWindow());
			Minecraft.getInstance().getSoundManager().resume();
		}
	}

	private void sendExperimentalInfo(Player player) {
		//		if (!this.shownExperimentalInfo) {
		//			player.sendMessage(Component.translatable("message.enigmaticlegacy.voiceover_experimental")
		//					.setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withClickEvent(new ClickEvent(
		//							Action.OPEN_URL, "https://discord.gg/fuWK8ns"))), Util.NIL_UUID);
		//
		//			this.shownExperimentalInfo = true;
		//		}
	}

	private void drawQuote(GuiGraphics graphics, Window window) {
		if (this.currentQuote.getSubtitles().getDuration() - this.getPlayTime() <= 0.1) {
			if (Quote.NARRATOR_INTROS.contains(this.currentQuote) && Minecraft.getInstance().player != null) {
				this.sendExperimentalInfo(Minecraft.getInstance().player);
			}

			this.currentQuote = null;
			this.startedPlaying = this.delayTicks = -1;
			return;
		}

		if (this.getPlayTime() < 0.05)
			return;

		if (OmniconfigHandler.disableQuoteSubtitles.getValue())
			return;

		Subtitles subtitles = this.currentQuote.getSubtitles();

		Font font = Minecraft.getInstance().font;
		String[] text = wrapString(subtitles.getLine(this.getPlayTime()), font, 260);

		int alphaMod = 0xFF;

		if (this.getPlayTime() < 0.5) {
			alphaMod *= this.getPlayTime() / 0.5;
		} else if (this.currentQuote.getSubtitles().getDuration() - this.getPlayTime() < 0.5) {
			alphaMod *= (this.currentQuote.getSubtitles().getDuration() - this.getPlayTime()) / 0.5;
		}

		if (alphaMod < 0) {
			alphaMod = 0xFF;
		}

		PoseStack stack = graphics.pose();
		int width = window.getGuiScaledWidth() / 2 - greatestWidth(font, text) / 2;
		int height = window.getGuiScaledHeight() - 70 - ((font.lineHeight + 2) * (text.length - 1));

		stack.pushPose();
		stack.scale(1F, 1F, 1F);

		int fromX = width, fromY = height, toX = fromX + greatestWidth(font, text),
				toY = fromY + (font.lineHeight * text.length) + 2 * text.length - 1;
		int color1 = 0x000000 | (((int)(alphaMod * 0.266)) << 24);
		int color2 = ChatFormatting.YELLOW.getColor() | ((alphaMod) << 24);

		// Port: upstream draws these with GUIUtils.drawGradientRect; both colors are equal,
		// so a plain fill draws the same box.
		graphics.fill(fromX - 4, fromY - 4, toX + 4, toY + 4, color1);
		graphics.fill(fromX - 6, fromY - 6, toX + 6, toY + 6, color1);
		graphics.fill(fromX - 8, fromY - 8, toX + 8, toY + 8, color1);

		int counter = 0;
		for (String line : text) {
			graphics.drawString(font, line, window.getGuiScaledWidth() / 2 - font.width(line) / 2, height +
					(counter * (font.lineHeight + 2)), color2, true);
			counter++;
		}

		stack.popPose();
	}

	/**
	 * Port: upstream's SuperpositionHandler.wrapString, moved here so the common handler
	 * does not touch the client-only Font class.
	 */
	private static String[] wrapString(String string, Font font, int width) {
		var list = font.getSplitter().splitLines(string, width, Style.EMPTY);
		String[] lines = new String[list.size()];

		for (int i = 0; i < lines.length; i++) {
			FormattedText text = list.get(i);
			lines[i] = text.getString();
		}

		return lines;
	}

	/**
	 * Port: upstream's SuperpositionHandler.greatestWidth, moved here for the same reason.
	 */
	private static int greatestWidth(Font font, String[] lines) {
		return Arrays.stream(lines).mapToInt(font::width).reduce((num1, num2) -> num1 > num2 ? num1 : num2)
				.getAsInt();
	}

}
