package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.aizistral.enigmaticlegacy.client.PermadeathClient;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;

import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelSummary;

/**
 * Port: SuperpositionHandler.getAltInfo / getSaveFolder and PROXY.displayPermadeathScreen
 * moved to the client-only PermadeathClient.
 */
@Mixin(WorldSelectionList.WorldListEntry.class)
public class MixinWorldListEntry {
	@Shadow @Final
	private LevelSummary summary;

	@Unique
	private Component cachedInfo;

	@Redirect(method = "render", at = @At(value = "INVOKE", target =
			"Lnet/minecraft/world/level/storage/LevelSummary;getInfo()"
					+ "Lnet/minecraft/network/chat/Component;"))
	private Component getAltInfo(LevelSummary summary) {
		if (this.cachedInfo == null) {
			this.cachedInfo = PermadeathClient.getAltInfo(summary);
		}

		return this.cachedInfo;
	}

	@Inject(method = "joinWorld", at = @At("HEAD"), cancellable = true)
	private void onJoinWorld(CallbackInfo info) {
		if (SuperpositionHandler.isWorldFractured(PermadeathClient.getSaveFolder(this.summary))) {
			PermadeathClient.displayPermadeathScreen();
			info.cancel();
		}
	}

}
