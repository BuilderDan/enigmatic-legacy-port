package com.aizistral.enigmaticlegacy.client;

import java.io.File;

import com.aizistral.enigmaticlegacy.gui.PermadeathScreen;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;

/**
 * Port: upstream's ClientProxy.displayPermadeathScreen and the client-only
 * SuperpositionHandler.getSaveFolder / getAltInfo, in one client-only class. Fabric has no
 * {@code @OnlyIn} stripping, so they are kept out of the common SuperpositionHandler.
 */
public class PermadeathClient {

	public static void displayPermadeathScreen() {
		if (Minecraft.getInstance().level != null) {
			boolean local = Minecraft.getInstance().isLocalServer();
			Minecraft.getInstance().level.disconnect(net.minecraft.client.multiplayer.ClientLevel.DEFAULT_QUIT_MESSAGE);

			if (local) {
				Minecraft.getInstance().disconnectWithSavingScreen();
			} else {
				Minecraft.getInstance().disconnectWithProgressScreen();
			}
		}

		PermadeathScreen permadeath = new PermadeathScreen(new SelectWorldScreen(new TitleScreen()),
				Component.translatable("gui.enigmaticlegacy.permadeathTitle"),
				Component.translatable("message.enigmaticlegacy.permadeath"));
		PermadeathScreen.active = permadeath;
		Minecraft.getInstance().setScreen(permadeath);
	}

	public static File getSaveFolder(LevelSummary summary) {
		LevelStorageSource levels = Minecraft.getInstance().getLevelSource();
		return new File(levels.getBaseDir().toFile(), summary.getLevelId());
	}

	public static Component getAltInfo(LevelSummary summary) {
		Component info = summary.getInfo();
		File world = getSaveFolder(summary);
		boolean fractured = SuperpositionHandler.isWorldFractured(world);

		if (summary.getGameMode() == GameType.SURVIVAL || fractured) {
			String key = "gameMode.enigmaticlegacy.";

			if (fractured) {
				key += "fractured";
			} else if (SuperpositionHandler.isWorldCursed(world)) {
				key += "cursed";

				if (summary.isHardcore()) {
					key += "Hardcore";
				}
			} else
				return info;

			MutableComponent tcn = Component.translatable(key);
			tcn.withStyle(info.getStyle());

			if (info instanceof MutableComponent mutable)
				if (mutable.getContents() instanceof TranslatableContents) {
					info.getSiblings().forEach(tcn::append);
				} else if (mutable.getContents() instanceof PlainTextContents.LiteralContents) {
					for (int i = 1; i < info.getSiblings().size(); i++) {
						tcn.append(info.getSiblings().get(i));
					}
				}

			return tcn;
		}

		return info;
	}

}
