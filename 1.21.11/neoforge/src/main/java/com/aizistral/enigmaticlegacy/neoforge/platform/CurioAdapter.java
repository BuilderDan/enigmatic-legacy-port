package com.aizistral.enigmaticlegacy.neoforge.platform;

import java.util.ArrayList;
import java.util.List;

import com.aizistral.enigmaticlegacy.platform.accessory.IAccessoryItem;
import com.aizistral.enigmaticlegacy.platform.accessory.SoundInfo;
import com.aizistral.enigmaticlegacy.triggers.EquipAccessoryTrigger;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.common.DropRule;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * Port-only. Forwards Curios callbacks to an {@link IAccessoryItem}. Upstream's items implement
 * ICurioItem directly; common code uses loader-neutral types instead.
 */
public record CurioAdapter(IAccessoryItem item) implements ICurioItem {

	static com.aizistral.enigmaticlegacy.platform.accessory.SlotContext toCommon(SlotContext ctx) {
		return new com.aizistral.enigmaticlegacy.platform.accessory.SlotContext(ctx.identifier(), ctx.entity(),
				ctx.index(), ctx.cosmetic(), ctx.visible());
	}

	static DropRule toCurios(com.aizistral.enigmaticlegacy.platform.accessory.DropRule rule) {
		return DropRule.valueOf(rule.name());
	}

	@Override
	public void curioTick(SlotContext ctx, ItemStack stack) {
		this.item.curioTick(toCommon(ctx), stack);

		// Port: the adapter's modifiers are transient and the elytra's flight depends on durability,
		// so they are checked again once a second.
		if (!ctx.entity().level().isClientSide() && ctx.entity().tickCount % 20 == 0) {
			this.updateModifiers(ctx, stack, true);
		}
	}

	@Override
	public void onEquip(SlotContext ctx, ItemStack prevStack, ItemStack stack) {
		this.updateModifiers(ctx, stack, true);
		this.item.onEquip(toCommon(ctx), prevStack, stack);

		// The port's advancements use its own equip trigger on both loaders, not curios:equip_curio.
		if (ctx.entity() instanceof ServerPlayer player)
			EquipAccessoryTrigger.INSTANCE.trigger(player, stack);
	}

	@Override
	public void onUnequip(SlotContext ctx, ItemStack newStack, ItemStack stack) {
		this.updateModifiers(ctx, stack, false);
		this.item.onUnequip(toCommon(ctx), newStack, stack);
	}

	@Override
	public boolean canEquip(SlotContext ctx, ItemStack stack) {
		return this.item.canEquip(toCommon(ctx), stack);
	}

	@Override
	public boolean canUnequip(SlotContext ctx, ItemStack stack) {
		return this.item.canUnequip(toCommon(ctx), stack);
	}

	@Override
	public boolean canEquipFromUse(SlotContext ctx, ItemStack stack) {
		return this.item.canEquipFromUse(toCommon(ctx), stack);
	}

	@Override
	public ICurio.SoundInfo getEquipSound(SlotContext ctx, ItemStack stack) {
		SoundInfo info = this.item.getEquipSound(toCommon(ctx), stack);
		return new ICurio.SoundInfo(info.soundEvent(), info.volume(), info.pitch());
	}

	/**
	 * Port: Curios 17 no longer calls the per-slot {@code getAttributeModifiers}; it only applies static
	 * modifiers stored on the stack. Several items depend on the wearer, so the adapter applies them itself
	 * when Curios equips or unequips the stack, which is when upstream's Curios recalculated them. Ids get
	 * the slot and index appended, as Curios does, so two copies in one slot type do not collide.
	 */
	private void updateModifiers(SlotContext ctx, ItemStack stack, boolean add) {
		Identifier slotId = Identifier.fromNamespaceAndPath("curios", ctx.identifier() + "/" + ctx.index());
		String suffix = "/" + ctx.identifier() + "/" + ctx.index();

		this.getModifiers(ctx, slotId, stack).forEach((attribute, modifier) -> {
			AttributeInstance instance = ctx.entity().getAttributes().getInstance(attribute);
			if (instance == null)
				return;

			Identifier id = modifier.id().withSuffix(suffix);
			AttributeModifier current = instance.getModifier(id);

			// Unchanged modifiers stay, so the once-a-second check does not touch max health.
			if (add && current != null && current.amount() == modifier.amount() && current.operation() == modifier.operation())
				return;

			instance.removeModifier(id);

			if (add) {
				instance.addTransientModifier(new AttributeModifier(id, modifier.amount(), modifier.operation()));
			}
		});
	}

	private Multimap<Holder<Attribute>, AttributeModifier> getModifiers(SlotContext ctx, Identifier id, ItemStack stack) {
		Multimap<Holder<Attribute>, AttributeModifier> map = HashMultimap.create();
		map.putAll(this.item.getAttributeModifiers(toCommon(ctx), id, stack));
		return map;
	}

	@Override
	public List<Component> getAttributesTooltip(List<Component> tooltips, Item.TooltipContext context, ItemStack stack) {
		if (this.item.hidesAttributeTooltip(stack))
			return new ArrayList<>();

		return ICurioItem.super.getAttributesTooltip(tooltips, context, stack);
	}

	// Curios does not report the looting level here; AccessoryDrops overrides go through DropRulesEvent.
	@Override
	public DropRule getDropRule(SlotContext ctx, DamageSource source, boolean recentlyHit, ItemStack stack) {
		return toCurios(this.item.getDropRule(toCommon(ctx), source, 0, recentlyHit, stack));
	}
}
