package com.aizistral.enigmaticlegacy.items;

import net.minecraft.core.dispenser.EquipmentDispenseItemBehavior;
import net.minecraft.world.InteractionResult;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.EquipmentSlotGroup;
import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.*;

import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.items.ICursed;
import com.aizistral.enigmaticlegacy.api.items.ITaintable;
import com.aizistral.enigmaticlegacy.api.materials.EnigmaticMaterials;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseTool;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.enigmaticlegacy.registries.EnigmaticSounds;
import com.aizistral.enigmaticlegacy.registries.EnigmaticTabs;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.*;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Port: 26.3 has no TieredItem; the pan extends ItemBase and takes durability, enchantability and
 * repair items from item properties (repair items are the enigmaticlegacy:repairs/eldritch_pan tag).
 */
public class EldritchPan extends ItemBase implements ICursed {
	public static final Map<Player, Integer> HOLDING_DURATIONS = new WeakHashMap<>();

	public static Omniconfig.DoubleParameter attackDamage = null;
	public static Omniconfig.DoubleParameter attackSpeed = null;
	public static Omniconfig.DoubleParameter armorValue = null;
	public static Omniconfig.DoubleParameter lifeSteal = null;
	public static Omniconfig.DoubleParameter hungerSteal = null;
	public static Omniconfig.DoubleParameter uniqueDamageGain = null;
	public static Omniconfig.DoubleParameter uniqueArmorGain = null;
    public static Omniconfig.IntParameter uniqueGainLimit = null;

	@SubscribeConfig
	public static void onConfig(OmniconfigWrapper builder) {
		builder.pushPrefix("EldritchPan");

		attackDamage = builder
				.comment("The base attack damage of The Voracious Pan.")
				.max(32768).getDouble("AttackDamage", 31);

		attackSpeed = builder
				.comment("The base attack speed of The Voracious Pan.")
				.minMax(32768).getDouble("AttackSpeed", -3.2);

		armorValue = builder
				.comment("The base armor value of The Voracious Pan.")
				.max(32768).getDouble("ArmorValue", 4);

		lifeSteal = builder
				.comment("Base Lifesteal fraction of The Voracious Pan.")
				.max(32768).getDouble("LifeSteal", 0.15);

		hungerSteal = builder
				.comment("Base Hungersteal value of The Voracious Pan.")
				.max(32768).getDouble("HungerSteal", 2);

		uniqueDamageGain = builder
				.comment("Base damage gain from unique mob kills for The Voracious Pan.")
				.max(32768).getDouble("UniqueDamageGain", 0.5);

		uniqueArmorGain = builder
				.comment("Base armor gain from unique mob kills for The Voracious Pan.")
				.max(32768).getDouble("UniqueArmorGain", 0.5);

		uniqueGainLimit = builder
				.comment("How many unique mob kills will count towards increasing the stats of The Voracious Pan.")
				.max(32768).getInt("UniqueGainLimit", 100);

		builder.popPrefix();
	}

	private static final Identifier ARMOR_MODIFIER_ID = Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, "eldritch_pan_armor");

	public EldritchPan() {
		super(ItemBaseTool.getDefaultProperties()
				.durability(EnigmaticMaterials.ELDRITCH_PAN.durability())
				.enchantable(EnigmaticMaterials.ELDRITCH_PAN.enchantmentValue())
				.repairable(EnigmaticMaterials.REPAIRS_ELDRITCH_PAN)
				.rarity(Rarity.EPIC)
				.fireResistant()
				.delayedComponent(DataComponents.BLOCKS_ATTACKS, context -> InfernalShield.shieldBlocking(context, 1.0F)));


		DispenserBlock.registerBehavior(this, EquipmentDispenseItemBehavior.INSTANCE);
	}

	/**
	 * Port: replaces upstream's cached mainhand/offhand multimaps and its getDefaultAttributeModifiers
	 * override. Built per call so config values read at use time, as upstream's stack-aware
	 * overload does.
	 */
	public ItemAttributeModifiers getDefaultAttributeModifiers() {
		return buildModifiers(attackDamage.getValue(), armorValue.getValue());
	}

	/**
	 * Port: Forge's stack-aware getAttributeModifiers(slot, stack). Vanilla 1.21 has no per-stack
	 * hook, so MixinItemStack returns this in place of the stack's attribute modifiers component.
	 */
	public ItemAttributeModifiers getAttributeModifiers(ItemStack stack) {
		int kills = getKillCount(stack);

		if (kills <= 0)
			return this.getDefaultAttributeModifiers();

		double armor = armorValue.getValue() + (uniqueArmorGain.getValue() * kills);
		double damage = attackDamage.getValue() + (uniqueDamageGain.getValue() * kills);
		return buildModifiers(damage, armor);
	}

	private static ItemAttributeModifiers buildModifiers(double damage, double armor) {
		AttributeModifier armorModifier = new AttributeModifier(ARMOR_MODIFIER_ID, armor, AttributeModifier.Operation.ADD_VALUE);

		return ItemAttributeModifiers.builder()
				.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, damage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
				.add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, attackSpeed.getValue(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
				.add(Attributes.ARMOR, armorModifier, EquipmentSlotGroup.MAINHAND)
				.add(Attributes.ARMOR, armorModifier, EquipmentSlotGroup.OFFHAND)
				.build();
	}

	@Override
	public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (attacker.getRandom().nextDouble() < 0.0001) {
			attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
					EnigmaticSounds.PAN_CLANG_FR, SoundSource.PLAYERS, 1F,
					attacker.level().getRandom().nextFloat() * 0.2F + 0.8F);
		} else {
			attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
					EnigmaticSounds.PAN_CLANG, SoundSource.PLAYERS, 0.5F,
					attacker.level().getRandom().nextFloat() * 0.1F + 0.9F);
		}

		stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
	}

	public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
		if (state.getDestroySpeed(level, pos) != 0.0F) {
			stack.hurtAndBreak(2, entity, EquipmentSlot.MAINHAND);
		}

		return true;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		// NO-OP
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		if (Minecraft.getInstance().hasShiftDown()) {
			String life = "+" + this.toString(100 * lifeSteal.getValue()) + "%";
			String hunger = "+" + this.toString(hungerSteal.getValue());
			String damageGain = "+" + this.toString(uniqueDamageGain.getValue());
			String armorGain = "+" + this.toString(uniqueArmorGain.getValue());

			Player clientPlayer = Minecraft.getInstance().player;
			boolean noHunger = clientPlayer != null && SuperpositionHandler.cannotHunger(clientPlayer);

			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.eldritchPan1");
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.eldritchPan2");
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.eldritchPan3",
					ChatFormatting.GOLD, life);

			if (!noHunger) {
				ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.eldritchPan4",
					ChatFormatting.GOLD, hunger);
			} else {
				ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.eldritchPan4_alt",
						ChatFormatting.GOLD, hunger);
			}

			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.eldritchPan5");
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.eldritchPan6");
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.void");

			if (!noHunger) {
				ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.eldritchPan7");
				ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.eldritchPan8");
			} else {
				ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.eldritchPan7_alt");
				ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.eldritchPan8_alt");
				ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.eldritchPan8p_alt");
			}
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.eldritchPan9",
					ChatFormatting.GOLD, damageGain);
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.eldritchPan10",
					ChatFormatting.GOLD, armorGain);
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.indicateWorthyOnesOnly(tooltip);
		} else {
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.eldritchPanLore1");
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.holdShift");
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.void");
			this.writeKillCount(tooltip, stack);
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.indicateCursedOnesOnly(tooltip);
		}
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.BLOCK;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 72000;
	}

	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (hand == InteractionHand.MAIN_HAND) {
			ItemStack offhandStack = player.getOffhandItem();

			if (offhandStack.getItem().getUseAnimation(offhandStack) == ItemUseAnimation.BLOCK)
				return InteractionResult.PASS;
		}

		if (SuperpositionHandler.isTheCursedOne(player)) {
			player.startUsingItem(hand);
			return InteractionResult.CONSUME;
		} else
			return InteractionResult.PASS;
	}

	// Port: Forge's canPerformAction (shield actions) removed. Blocking, shield damage and the axe
	// cooldown come from the BLOCKS_ATTACKS component (see CHANGES.md).

	private void writeKillCount(List<Component> tooltip, ItemStack pan) {
		int kills = getKillCount(pan);

		ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.eldritchPanKills1",
				ChatFormatting.GOLD, kills);

		if (kills >= uniqueGainLimit.getValue()) {
			ItemLoreHelper.addLocalizedString(tooltip, "tooltip.enigmaticlegacy.eldritchPanKillsMax");
		}
	}

	@Override
	public @Nullable CreativeModeTab getCreativeTab() {
		return EnigmaticTabs.MAIN;
	}

	private String toString(double value) {
		if (Math.floor(value) == value) {
			return Integer.toString((int) value);
		} else {
			return Double.toString(value);
		}
	}

	public static int getKillCount(ItemStack pan) {
		CompoundTag tag = ItemNBTHelper.getNBT(pan);

		if (tag == null || !tag.contains("PanUniqueKills"))
			return 0;

		ListTag list = tag.getListOrEmpty("PanUniqueKills");

		return list.size();
	}

	public static List<Identifier> getUniqueKills(ItemStack pan) {
		CompoundTag tag = ItemNBTHelper.getNBT(pan);

		if (tag == null || !tag.contains("PanUniqueKills"))
			return Collections.emptyList();

		ListTag list = tag.getListOrEmpty("PanUniqueKills");

		return list.stream().map(e -> Identifier.parse(((StringTag) e).value())).toList();
	}

	public static void setUniqueKills(ItemStack pan, List<Identifier> mobs) {
		CompoundTag tag = ItemNBTHelper.getNBT(pan);

		ListTag list = new ListTag();
		mobs.forEach(entity -> list.add(StringTag.valueOf(entity.toString())));
		tag.put("PanUniqueKills", list);

		ItemNBTHelper.injectNBT(pan, tag);
	}

	public static void addUniqueKill(ItemStack pan, Identifier mob) {
		CompoundTag tag = ItemNBTHelper.getNBT(pan);
		ListTag list;

		if (!tag.contains("PanUniqueKills")) {
			list = new ListTag();
		} else {
			list = tag.getListOrEmpty("PanUniqueKills");
		}

		list.add(StringTag.valueOf(mob.toString()));
		
		tag.put("PanUniqueKills", list);
		ItemNBTHelper.injectNBT(pan, tag);
	}

	public static boolean addKillIfNotPresent(ItemStack pan, Identifier mob) {
		List<Identifier> kills = getUniqueKills(pan);

		if (kills.size() < 100 && !kills.contains(mob)) {
			addUniqueKill(pan, mob);
			return true;
		}

		return false;
	}

	public static Ingredient getRepairMaterial() {
		return Ingredient.of(
				Items.BEEF,
				Items.PORKCHOP,
				Items.MUTTON,
				Items.ROTTEN_FLESH,
				Items.APPLE,
				Items.GOLDEN_APPLE,
				Items.ENCHANTED_GOLDEN_APPLE,
				Items.POISONOUS_POTATO
		);
	}

	// Port: isEnchantable and Forge's stack-aware getEnchantmentValue removed; enchantable(24) in the
	// item properties gives the same.
	// isBookEnchantable and canApplyAtEnchantingTable are replaced by MixinEnchantment and
	// MixinEnchantmentHelper, which treat the pan as a diamond sword (see CHANGES.md).

}
