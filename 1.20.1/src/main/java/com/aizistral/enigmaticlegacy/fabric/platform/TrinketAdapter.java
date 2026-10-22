package com.aizistral.enigmaticlegacy.fabric.platform;

import java.util.UUID;

import com.aizistral.enigmaticlegacy.platform.accessory.DropRule;
import com.aizistral.enigmaticlegacy.platform.accessory.IAccessoryItem;
import com.aizistral.enigmaticlegacy.platform.accessory.SlotContext;
import com.google.common.collect.Multimap;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketEnums;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

/**
 * Port-only. Forwards Trinkets callbacks to an {@link IAccessoryItem}.
 */
public record TrinketAdapter(IAccessoryItem item) implements Trinket {

	static SlotContext context(SlotReference ref, LivingEntity entity) {
		return new SlotContext(ref.inventory().getSlotType().getName(), entity, ref.index(), false, true);
	}

	static TrinketEnums.DropRule toTrinkets(DropRule rule) {
		return switch (rule) {
			case ALWAYS_DROP -> TrinketEnums.DropRule.DROP;
			case ALWAYS_KEEP -> TrinketEnums.DropRule.KEEP;
			case DESTROY -> TrinketEnums.DropRule.DESTROY;
			case DEFAULT -> TrinketEnums.DropRule.DEFAULT;
		};
	}

	@Override
	public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
		this.item.curioTick(context(slot, entity), stack);
	}

	@Override
	public void onEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
		this.item.onEquip(context(slot, entity), ItemStack.EMPTY, stack);
	}

	@Override
	public void onUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
		this.item.onUnequip(context(slot, entity), ItemStack.EMPTY, stack);
	}

	@Override
	public boolean canEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
		return this.item.canEquip(context(slot, entity), stack);
	}

	@Override
	public boolean canUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
		return this.item.canUnequip(context(slot, entity), stack);
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
		return this.item.getAttributeModifiers(context(slot, entity), uuid, stack);
	}

	// Trinkets does not report looting or whether the entity was recently hit.
	@Override
	public TrinketEnums.DropRule getDropRule(ItemStack stack, SlotReference slot, LivingEntity entity) {
		return toTrinkets(this.item.getDropRule(context(slot, entity), entity.getLastDamageSource(), 0, false, stack));
	}
}
