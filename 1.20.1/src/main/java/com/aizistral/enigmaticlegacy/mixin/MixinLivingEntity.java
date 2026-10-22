package com.aizistral.enigmaticlegacy.mixin;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;
import com.aizistral.enigmaticlegacy.items.MonsterCharm;
import com.aizistral.enigmaticlegacy.objects.DropCapture;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.player.Player;

/**
 * Port-only. Stands in for the Forge events LivingHurtEvent, LivingExperienceDropEvent and
 * LivingDropsEvent (see docs/event-hooks-1.20.1.md). Each handler keeps upstream's logic in the item class.
 */
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
	@Shadow @Nullable protected Player lastHurtByPlayer;
	@Shadow protected int lastHurtByPlayerTime;

	/**
	 * Forge fires LivingHurtEvent at the head of actuallyHurt, before armor and absorption.
	 * Player overrides actuallyHurt without calling super, so this does not cover players
	 * as targets; add a Player mixin when an item needs that.
	 */
	@ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true)
	private float enigmaticlegacy$onHurt(float amount, DamageSource source) {
		LivingEntity self = (LivingEntity) (Object) this;
		return MonsterCharm.modifyHurtAmount(self, source, amount);
	}

	/** Forge fires MobEffectEvent.Applicable at the head of canBeAffected; DENY returns false. */
	@Inject(method = "canBeAffected", at = @At("HEAD"), cancellable = true)
	private void enigmaticlegacy$onPotionApplicable(MobEffectInstance effect, CallbackInfoReturnable<Boolean> info) {
		if (!EnigmaticEventHandler.onPotionApplicable((LivingEntity) (Object) this, effect)) {
			info.setReturnValue(false);
		}
	}

	/** Forge's LivingExperienceDropEvent wraps the amount passed to ExperienceOrb.award. */
	@ModifyArg(method = "dropExperience", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V"),
			index = 2)
	private int enigmaticlegacy$onExperienceDrop(int experience) {
		LivingEntity self = (LivingEntity) (Object) this;
		return MonsterCharm.modifyExperience(self, this.lastHurtByPlayer, experience);
	}

	/**
	 * Forge captures everything dropAllDeathLoot spawns, fires LivingDropsEvent with it, then
	 * spawns what is left in the list (see MixinEntity and MixinServerPlayer).
	 */
	@Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
	private void enigmaticlegacy$startDropCapture(DamageSource source, CallbackInfo info) {
		((DropCapture) this).enigmaticlegacy$setCapturedDrops(new ArrayList<>());
	}

	@Inject(method = "dropAllDeathLoot", at = @At("RETURN"))
	private void enigmaticlegacy$onDrops(DamageSource source, CallbackInfo info) {
		LivingEntity self = (LivingEntity) (Object) this;
		List<ItemEntity> drops = ((DropCapture) this).enigmaticlegacy$getCapturedDrops();
		((DropCapture) this).enigmaticlegacy$setCapturedDrops(null);

		if (drops == null)
			return;

		// Forge's looting level for the event: the killer's mob looting (accessory bonus included
		// through MixinEnchantmentHelper).
		int looting = source.getEntity() instanceof LivingEntity killer ? EnchantmentHelper.getMobLooting(killer) : 0;
		EnigmaticEventHandler.onLivingDrops(self, source, drops, looting, this.lastHurtByPlayerTime > 0);

		for (ItemEntity drop : drops) {
			self.level().addFreshEntity(drop);
		}
	}
}
