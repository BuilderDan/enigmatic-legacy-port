package com.aizistral.enigmaticlegacy.neoforge.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Port-only. Upstream flies from the Curios back slot through Caelus' flight attribute, and
 * NeoForge 21.11 has no gliding attribute, so the back-slot elytra joins vanilla's canGlide here
 * and gets its flight tick in place of vanilla's durability loss. This mirrors Fabric's
 * EntityElytraEvents.CUSTOM. The chest slot works through the GLIDER component.
 */
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntityElytra {

	@Inject(method = "canGlide", at = @At("RETURN"), cancellable = true)
	private void enigmaticlegacy$canGlide(CallbackInfoReturnable<Boolean> info) {
		LivingEntity self = (LivingEntity) (Object) this;

		if (!info.getReturnValueZ() && !self.onGround() && !self.isPassenger() && !self.hasEffect(MobEffects.LEVITATION)) {
			ItemStack stack = SuperpositionHandler.getEnigmaticElytra(self);

			if (stack != null && EnigmaticItems.ENIGMATIC_ELYTRA.canElytraFly(stack, self)) {
				info.setReturnValue(true);
			}
		}
	}

	// Runs after the canGlide check, where vanilla picks a glider slot to damage; with only the back slot there is none.
	@Inject(method = "updateFallFlying", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;fallFlyTicks:I", ordinal = 0), cancellable = true)
	private void enigmaticlegacy$updateFallFlying(CallbackInfo info) {
		LivingEntity self = (LivingEntity) (Object) this;

		for (EquipmentSlot slot : EquipmentSlot.VALUES) {
			if (LivingEntity.canGlideUsing(self.getItemBySlot(slot), slot))
				return;
		}

		ItemStack stack = SuperpositionHandler.getEnigmaticElytra(self);

		if (stack != null) {
			EnigmaticItems.ENIGMATIC_ELYTRA.elytraFlightTick(stack, self, self.getFallFlyingTicks());
		}

		info.cancel();
	}
}
