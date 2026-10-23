package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler;
import com.aizistral.enigmaticlegacy.items.EldritchPan;
import com.aizistral.enigmaticlegacy.items.InfernalShield;
import com.aizistral.enigmaticlegacy.objects.PersistentData;
import com.aizistral.enigmaticlegacy.registries.EnigmaticAttributes;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Port-only. Gives players the port's block reach attribute, and stands in for Forge's
 * {@code PlayerEvent.BreakSpeed}, which Forge fires at the end of getDestroySpeed, and for the
 * player part of {@code LivingHurtEvent}, which Forge fires at the head of actuallyHurt (Player
 * overrides it without calling super, so MixinLivingEntity's hook does not reach players), and
 * for {@code LivingDamageEvent} in the same method.
 * Also holds the player's persistent data (Forge's {@code PlayerPersisted} compound), and makes
 * the Bulwark of Blazing Pride and the Voracious Pan count as shields where Forge checks
 * canPerformAction(SHIELD_BLOCK) or uses the blocking item.
 */
@Mixin(Player.class)
public abstract class MixinPlayer implements PersistentData {
	@Unique
	private static final String enigmaticlegacy$PERSISTED_KEY = "EnigmaticLegacyPersisted";

	@Unique
	private CompoundTag enigmaticlegacy$persisted = new CompoundTag();

	@Inject(method = "createAttributes", at = @At("RETURN"))
	private static void enigmaticlegacy$addAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> info) {
		info.getReturnValue().add(EnigmaticAttributes.BLOCK_REACH);
		info.getReturnValue().add(EnigmaticAttributes.GRAVITY);
		info.getReturnValue().add(EnigmaticAttributes.SWIM_SPEED);
	}

	@Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
	private void enigmaticlegacy$onBreakSpeed(BlockState state, CallbackInfoReturnable<Float> info) {
		info.setReturnValue(EnigmaticEventHandler.onBreakSpeed((Player) (Object) this, info.getReturnValueF()));
	}

	@ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true)
	private float enigmaticlegacy$onHurt(float amount, DamageSource source) {
		return EnigmaticEventHandler.onLivingHurt((Player) (Object) this, source, amount);
	}

	/** Player's copy of MixinLivingEntity's LivingDamageEvent hook. */
	@WrapOperation(method = "actuallyHurt", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/damagesource/CombatTracker;recordDamage(Lnet/minecraft/world/damagesource/DamageSource;F)V"))
	private void enigmaticlegacy$onDamage(CombatTracker tracker, DamageSource source, float amount, Operation<Void> original) {
		EnigmaticEventHandler.onLivingDamage((Player) (Object) this, source, amount);
		original.call(tracker, source, amount);
	}

	/** Upstream's MixinPlayer: axes never disable the Bulwark of Blazing Pride. */
	@Inject(method = "disableShield", at = @At("HEAD"), cancellable = true)
	private void enigmaticlegacy$onDisableShield(boolean sprinting, CallbackInfo info) {
		if (((Player) (Object) this).getUseItem().getItem() instanceof InfernalShield) {
			info.cancel();
		}
	}

	/** Vanilla only damages Items.SHIELD; Forge damages any item that can block. */
	@ModifyExpressionValue(method = "hurtCurrentlyUsedShield", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
	private boolean enigmaticlegacy$damageInfernalShield(boolean isShield) {
		ItemStack useItem = ((Player) (Object) this).getUseItem();
		return isShield || useItem.getItem() instanceof InfernalShield || useItem.getItem() instanceof EldritchPan;
	}

	/** Vanilla puts Items.SHIELD on cooldown; Forge uses the blocking item. Only the pan needs it here. */
	@ModifyArg(method = "disableShield", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemCooldowns;addCooldown(Lnet/minecraft/world/item/Item;I)V"))
	private Item enigmaticlegacy$panCooldown(Item item) {
		ItemStack useItem = ((Player) (Object) this).getUseItem();
		return useItem.getItem() instanceof EldritchPan ? useItem.getItem() : item;
	}

	@Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
	private void enigmaticlegacy$savePersisted(CompoundTag tag, CallbackInfo info) {
		tag.put(enigmaticlegacy$PERSISTED_KEY, this.enigmaticlegacy$persisted);
	}

	@Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
	private void enigmaticlegacy$loadPersisted(CompoundTag tag, CallbackInfo info) {
		this.enigmaticlegacy$persisted = tag.getCompound(enigmaticlegacy$PERSISTED_KEY);
	}

	@Override
	public CompoundTag enigmaticlegacy$getPersistedTag() {
		return this.enigmaticlegacy$persisted;
	}

	@Override
	public void enigmaticlegacy$setPersistedTag(CompoundTag tag) {
		this.enigmaticlegacy$persisted = tag;
	}
}
