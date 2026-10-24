package com.aizistral.enigmaticlegacy.items.generic;

import com.aizistral.enigmaticlegacy.api.items.IBindable;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.platform.accessory.DropRule;
import com.aizistral.enigmaticlegacy.platform.accessory.IAccessoryItem;
import com.aizistral.enigmaticlegacy.platform.accessory.SlotContext;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

/**
 * Port: implements {@link IAccessoryItem} (the port's stand-in for Curios' ICurioItem)
 * instead of ICurioItem. Upstream's isBookEnchantable override is a Forge-only hook that
 * returned true in both branches (Forge's default), so it is dropped. Curse of Binding
 * still applies through MixinBindingCurseEnchantment and {@link IBindable}.
 */
public abstract class ItemBaseCurio extends ItemBase implements IAccessoryItem, IBindable {

	public ItemBaseCurio() {
		this(getDefaultProperties());
	}

	public ItemBaseCurio(Properties props) {
		super(props);
	}

	@Override
	public void onEquip(SlotContext context, ItemStack prevStack, ItemStack stack) {
		// Insert existential void here
	}

	@Override
	public void onUnequip(SlotContext context, ItemStack newStack, ItemStack stack) {
		// Insert existential void here
	}

	@Override
	public void curioTick(SlotContext context, ItemStack stack) {
		// Insert existential void here
	}

	@Override
	public void onCraftedBy(ItemStack stack, Level worldIn, Player playerIn) {
		// Insert existential void here
	}

	@Override
	public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
		return true;
	}

	@Override
	public boolean canEquip(SlotContext context, ItemStack stack) {
		return !SuperpositionHandler.hasCurio(context.entity(), this);
	}

	@Override
	public boolean canUnequip(SlotContext context, ItemStack stack) {
		return true;
	}

	@Override
	public DropRule getDropRule(SlotContext context, DamageSource source, int lootingLevel, boolean recentlyHit, ItemStack stack) {
		return DropRule.DEFAULT;
	}

	public static Properties getDefaultProperties() {
		Properties props = new Item.Properties();

		props.stacksTo(1);
		props.rarity(Rarity.COMMON);

		return props;
	}

}
