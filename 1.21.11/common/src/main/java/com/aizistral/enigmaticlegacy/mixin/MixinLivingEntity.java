package com.aizistral.enigmaticlegacy.mixin;

import net.minecraft.server.level.ServerLevel;
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
import com.aizistral.enigmaticlegacy.platform.Services;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.items.CursedScroll;
import com.aizistral.enigmaticlegacy.items.ForbiddenFruit;
import com.aizistral.enigmaticlegacy.items.MonsterCharm;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.enigmaticlegacy.objects.DropCapture;
import com.aizistral.enigmaticlegacy.registries.EnigmaticAttributes;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;

/**
 * Port-only. Stands in for the Forge events LivingHurtEvent, LivingDamageEvent,
 * LivingExperienceDropEvent and LivingDropsEvent, and for Forge's ENTITY_GRAVITY and SWIM_SPEED
 * attributes (see docs/event-hooks-1.20.1.md). Each handler keeps upstream's logic in the item class.
 */
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
	@Shadow protected int lastHurtByPlayerMemoryTime;

	/**
	 * Forge fires LivingHurtEvent at the head of actuallyHurt, before armor and absorption.
	 * Player overrides actuallyHurt without calling super, so this does not cover players
	 * as targets; add a Player mixin when an item needs that.
	 */
	@ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true)
	private float enigmaticlegacy$onHurt(float amount, @Local(argsOnly = true) DamageSource source) {
		LivingEntity self = (LivingEntity) (Object) this;
		return EnigmaticEventHandler.onLivingHurt(self, source, MonsterCharm.modifyHurtAmount(self, source, amount));
	}

	/**
	 * Upstream's MixinLivingEntity: lets the Bulwark of Blazing Pride decide what it blocks.
	 * Port: 26.3 has no isDamageSourceBlocked; applyItemBlocking returns the blocked amount, so a
	 * forced block raises it to the full damage.
	 */
	@WrapMethod(method = "applyItemBlocking")
	private float enigmaticlegacy$onDamageSourceBlocking(ServerLevel level, DamageSource source, float damage, Operation<Float> original) {
		LivingEntity self = (LivingEntity) (Object) this;
		boolean forced = damage > 0.0F && SuperpositionHandler.onDamageSourceBlocking(self, self.getUseItem(), source);
		float blocked = original.call(level, source, damage);
		return forced ? Math.max(blocked, damage) : blocked;
	}

	/**
	 * Forge fires LivingDamageEvent in actuallyHurt after armor and absorption, right before the
	 * combat tracker records the final damage. Player has its own copy in MixinPlayer.
	 */
	@WrapOperation(method = "actuallyHurt", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/damagesource/CombatTracker;recordDamage(Lnet/minecraft/world/damagesource/DamageSource;F)V"))
	private void enigmaticlegacy$onDamage(CombatTracker tracker, DamageSource source, float amount, Operation<Void> original) {
		EnigmaticEventHandler.onLivingDamage((LivingEntity) (Object) this, source, amount);
		original.call(tracker, source, amount);
	}

	/**
	 * Forge reads gravity from its ENTITY_GRAVITY attribute (base 0.08, slow falling as a -0.07
	 * modifier). Here vanilla's effective gravity (0.08, or at most 0.01 with slow falling) is
	 * scaled by our GRAVITY multiplier, which gives the same result. Port: 26.3 reads gravity
	 * through getEffectiveGravity instead of constants in travel.
	 */
	@ModifyReturnValue(method = "getEffectiveGravity", at = @At("RETURN"))
	private double enigmaticlegacy$scaleGravity(double gravity) {
		return gravity * EnigmaticAttributes.getMultiplier((LivingEntity) (Object) this, EnigmaticAttributes.GRAVITY);
	}

	/** Forge scales the water moveRelative speed by its SWIM_SPEED attribute. */
	@ModifyArg(method = "travelInWater", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V", ordinal = 0),
			index = 0)
	private float enigmaticlegacy$scaleSwimSpeed(float speed) {
		return speed * (float) EnigmaticAttributes.getMultiplier((LivingEntity) (Object) this, EnigmaticAttributes.SWIM_SPEED);
	}

	/** Forge fires LivingTickEvent at the head of tick. */
	@Inject(method = "tick", at = @At("HEAD"))
	private void enigmaticlegacy$onLivingTick(CallbackInfo info) {
		EnigmaticEventHandler.onLivingTick((LivingEntity) (Object) this);
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
		return EnigmaticEventHandler.onExperienceDrop(self, self.getLastHurtByPlayer(), experience);
	}

	/** Forge's LivingKnockBackEvent fires at the head of knockback and may change the strength. */
	@ModifyVariable(method = "knockback", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private double enigmaticlegacy$cursedKnockback(double strength) {
		return EnigmaticEventHandler.onLivingKnockback((LivingEntity) (Object) this, strength);
	}

	/**
	 * Forge captures everything dropAllDeathLoot spawns, fires LivingDropsEvent with it, then
	 * spawns what is left in the list (see MixinEntity and the drop wrap below).
	 */
	@Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
	private void enigmaticlegacy$startDropCapture(ServerLevel level, DamageSource source, CallbackInfo info) {
		// NeoForge captures the drops itself; its LivingDropsEvent listener calls onLivingDrops.
		if (Services.PLATFORM.firesLivingDrops())
			return;

		((DropCapture) this).enigmaticlegacy$setCapturedDrops(new ArrayList<>());
	}

	@Inject(method = "dropAllDeathLoot", at = @At("RETURN"))
	private void enigmaticlegacy$onDrops(ServerLevel level, DamageSource source, CallbackInfo info) {
		LivingEntity self = (LivingEntity) (Object) this;
		List<ItemEntity> drops = ((DropCapture) this).enigmaticlegacy$getCapturedDrops();
		((DropCapture) this).enigmaticlegacy$setCapturedDrops(null);

		if (drops == null)
			return;

		// Forge's looting level for the event: the killer's mob looting (accessory bonus included
		// through MixinEnchantmentHelper).
		int looting = source.getEntity() instanceof LivingEntity killer ? EnchantmentHelper.getEnchantmentLevel(self.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING), killer) : 0;
		EnigmaticEventHandler.onLivingDrops(self, source, drops, looting, this.lastHurtByPlayerMemoryTime > 0);

		for (ItemEntity drop : drops) {
			self.level().addFreshEntity(drop);
		}
	}

	/**
	 * Forge fires LivingHealEvent at the head of heal. Upstream reduces small heals (natural
	 * regeneration) for players who ate the Forbidden Fruit.
	 */
	@ModifyVariable(method = "heal", at = @At("HEAD"), argsOnly = true)
	private float enigmaticlegacy$onHeal(float amount) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self instanceof Player player && !player.level().isClientSide()) {
			if (amount <= 1.0F && EnigmaticItems.FORBIDDEN_FRUIT.haveConsumedFruit(player)) {
				amount *= ForbiddenFruit.regenerationSubtraction.getValue().asModifierInverted();
			}

			if (SuperpositionHandler.hasCurio(player, EnigmaticItems.CURSED_SCROLL)) {
				amount += amount * (CursedScroll.regenBoost.getValue().asModifier() * SuperpositionHandler.getCurseAmount(player));
			}
		}

		return amount;
	}

	/**
	 * Port: 1.21.1 wrapped Player.drop(ItemStack, boolean, boolean); 1.21.11 has it on LivingEntity.
	 * Inventory, armor and offhand death drops all go through it, so they are captured here.
	 */
	@WrapOperation(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
	private boolean enigmaticlegacy$captureDrop(Level level, Entity entity, Operation<Boolean> original) {
		List<ItemEntity> drops = ((DropCapture) this).enigmaticlegacy$getCapturedDrops();

		if (drops != null && entity instanceof ItemEntity drop) {
			drops.add(drop);
			return true;
		}

		return original.call(level, entity);
	}
}
