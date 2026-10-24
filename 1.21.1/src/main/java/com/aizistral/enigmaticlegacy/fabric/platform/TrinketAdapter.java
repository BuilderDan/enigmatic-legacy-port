package com.aizistral.enigmaticlegacy.fabric.platform;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Holder;
import java.util.UUID;

import com.aizistral.enigmaticlegacy.platform.accessory.DropRule;
import com.aizistral.enigmaticlegacy.platform.accessory.IAccessoryItem;
import com.aizistral.enigmaticlegacy.platform.accessory.SlotContext;
import com.aizistral.enigmaticlegacy.triggers.EquipAccessoryTrigger;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketEnums;
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
public record TrinketAdapter(IAccessoryItem item) implements Trinket {

	/**
	 * Set by MixinItemStack while a tooltip is being built, so items that hide their
	 * attribute lines report no modifiers to Trinkets' tooltip code.
	 */
	/** Depth of nested ItemStack.getTooltipLines calls on this thread (bundle/shulker previews nest). */
	public static final ThreadLocal<int[]> TOOLTIP_PASS = ThreadLocal.withInitial(() -> new int[1]);

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

		// Curios fires curios:equip_curio itself; Trinkets has no such trigger.
		if (entity instanceof ServerPlayer player)
			EquipAccessoryTrigger.INSTANCE.trigger(player, stack);
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
		// Port: Curios' slot refuses bound items itself; Trinkets leaves that to Trinket.canUnequip.
		if (EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) && !(entity instanceof Player player && player.isCreative()))
			return false;

		return this.item.canUnequip(context(slot, entity), stack);
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, ResourceLocation id) {
		if (TOOLTIP_PASS.get()[0] > 0 && this.item.hidesAttributeTooltip(stack))
			return ImmutableMultimap.of();

		return this.item.getAttributeModifiers(context(slot, entity), id, stack);
	}

	// Trinkets does not report looting or whether the entity was recently hit.
	@Override
	public TrinketEnums.DropRule getDropRule(ItemStack stack, SlotReference slot, LivingEntity entity) {
		return toTrinkets(this.item.getDropRule(context(slot, entity), entity.getLastDamageSource(), 0, false, stack));
	}
}
