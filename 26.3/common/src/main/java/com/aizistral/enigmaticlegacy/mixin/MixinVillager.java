package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;

/**
 * Port: upstream's MixinVillager (Desolation Ring) and MixinVillagerEntity (Scroll of Avarice),
 * merged into one mixin.
 */
@Mixin(Villager.class)
public abstract class MixinVillager {

	// Port: upstream injects at RETURN, where canceling does nothing, so golems still spawned.
	// HEAD makes the Desolation Ring block them as intended.
	@Inject(method = "spawnGolemIfNeeded", at = @At("HEAD"), cancellable = true)
	private void onGolemCreation(ServerLevel level, long gameTime, int villagersRequired, CallbackInfo info) {
		Villager villager = (Villager) (Object) this;

		if (EnigmaticEventHandler.DESOLATION_BOXES.values().stream().anyMatch(villager.getBoundingBox()::intersects)) {
			info.cancel();
		}
	}

	@Inject(at = @At("RETURN"), method = "updateSpecialPrices")
	private void onSpecialPrices(Player player, CallbackInfo info) {
		Villager villager = (Villager) (Object) this;

		if (SuperpositionHandler.hasCurio(player, EnigmaticItems.AVARICE_SCROLL)) {
			for (MerchantOffer trade : villager.getOffers()) {
				double discountValue = 0.35;
				int discount = (int) Math.floor(discountValue * trade.getBaseCostA().getCount());
				trade.addToSpecialPriceDiff(-Math.max(discount, 1));
			}
		}
	}

}
