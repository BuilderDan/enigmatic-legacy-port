package com.aizistral.enigmaticlegacy.fabric.platform;

import com.aizistral.enigmaticlegacy.platform.accessory.AccessoryTooltips;

import net.minecraft.resources.Identifier;
import net.minecraft.core.Holder;

import java.util.function.BiConsumer;

import com.aizistral.enigmaticlegacy.platform.accessory.DropRule;
import com.aizistral.enigmaticlegacy.platform.accessory.IAccessoryItem;
import com.aizistral.enigmaticlegacy.platform.accessory.SlotContext;
import com.aizistral.enigmaticlegacy.triggers.EquipAccessoryTrigger;

import eu.pb4.trinkets.api.TrinketDropRule;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.callback.TrinketCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;

/**
 * Port-only. Forwards Trinkets callbacks to an {@link IAccessoryItem}.
 */
public record TrinketAdapter(IAccessoryItem item) implements TrinketCallback {

	/** Depth of nested ItemStack.getTooltipLines calls on this thread (bundle/shulker previews nest). */

	static SlotContext context(TrinketSlotAccess access, LivingEntity entity) {
		return new SlotContext(access.slotType().name(), entity, access.index(), false, true);
	}

	static TrinketDropRule toTrinkets(DropRule rule) {
		return switch (rule) {
			case ALWAYS_DROP -> TrinketDropRule.DROP;
			case ALWAYS_KEEP -> TrinketDropRule.KEEP;
			case DESTROY -> TrinketDropRule.DESTROY;
			case DEFAULT -> TrinketDropRule.DEFAULT;
		};
	}

	@Override
	public void tick(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
		this.item.curioTick(context(slot, entity), stack);
	}

	@Override
	public void onEquip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
		this.item.onEquip(context(slot, entity), ItemStack.EMPTY, stack);

		// Curios fires curios:equip_curio itself; Trinkets has no such trigger.
		if (entity instanceof ServerPlayer player)
			EquipAccessoryTrigger.INSTANCE.trigger(player, stack);
	}

	@Override
	public void onUnequip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
		this.item.onUnequip(context(slot, entity), ItemStack.EMPTY, stack);
	}

	@Override
	public boolean canEquip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
		return this.item.canEquip(context(slot, entity), stack);
	}

	@Override
	public boolean canUnequip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
		// Port: Curios' slot refuses bound items itself; Trinkets leaves that to the callback.
		if (EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) && !(entity instanceof Player player && player.isCreative()))
			return false;

		return this.item.canUnequip(context(slot, entity), stack);
	}

	@Override
	public boolean canEquipFromUse(ItemStack stack, LivingEntity entity) {
		return this.item.canEquipFromUse(new SlotContext("", entity, -1, false, true), stack);
	}

	@Override
	public void forEachTrinketModifier(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity, Identifier id,
			BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {
		if (AccessoryTooltips.TOOLTIP_PASS.get()[0] > 0 && this.item.hidesAttributeTooltip(stack))
			return;

		this.item.getAttributeModifiers(context(slot, entity), id, stack).forEach(consumer);
	}

	// Trinkets does not report looting or whether the entity was recently hit.
	@Override
	public TrinketDropRule getDropRule(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
		return toTrinkets(this.item.getDropRule(context(slot, entity), entity.getLastDamageSource(), 0, false, stack));
	}
}
