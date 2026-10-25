package com.aizistral.enigmaticlegacy.items;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.core.Holder;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import vazkii.patchouli.api.PatchouliAPI;
import com.aizistral.enigmaticlegacy.helpers.PatchouliState;

public class TheAcknowledgment extends ItemBase {
	private static final Identifier BOOK_ID = Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, "the_acknowledgment");
	private final ItemAttributeModifiers attributes;
	// Port: kept for parity; upstream only reads it in Forge's isBookEnchantable and
	// canApplyAtEnchantingTable, which have no vanilla equivalent. Fabric's ALLOW_ENCHANTING
	// event only handles the Bane of Arthropods case, and no subclass uses this.
	protected boolean allowAllEnchantments = false;

	protected TheAcknowledgment(Properties props, String name, double attackDamage, double attackSpeed) {
		super(props.enchantable(24).attributes(buildAttributes(attackDamage, attackSpeed)));

		this.attributes = buildAttributes(attackDamage, attackSpeed);
	}

	// Port: 26.3 items take attributes from Properties.attributes.
	private static ItemAttributeModifiers buildAttributes(double attackDamage, double attackSpeed) {
		return ItemAttributeModifiers.builder()
				.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
				.add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
				.build();
	}

	public TheAcknowledgment() {
		this(getDefaultProperties().rarity(Rarity.EPIC).stacksTo(1), BOOK_ID.getPath(), 3.5, -2.1);
	}

	public static boolean isOpen() {
		return PatchouliState.LOADED && BOOK_ID.equals(PatchouliAPI.get().getOpenBookGui());
	}

	public static Component getEdition() {
		return PatchouliState.LOADED ? PatchouliAPI.get().getSubtitle(BOOK_ID) : Component.empty();
	}

	public static Component getTitle(ItemStack stack) {
		Component title = stack.getHoverName();

		String akashicTomeNBT = "akashictome:displayName";
		if (ItemNBTHelper.verifyExistance(stack, akashicTomeNBT)) {
			title = Component.literal(ItemNBTHelper.getString(stack, akashicTomeNBT, ""));
		}

		return title;
	}

	protected void setAllowAllEnchantments(boolean allowAllEnchantments) {
		this.allowAllEnchantments = allowAllEnchantments;
	}

	@Override
	public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		target.igniteForSeconds(4);

		super.hurtEnemy(stack, target, attacker);
	}

	// Port: 1.21 item-level modifiers (slot groups are in the component) instead of Forge's stack-aware overload.
	public ItemAttributeModifiers getDefaultAttributeModifiers() {
		return this.attributes;
	}

	@NotNull
	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (PatchouliState.LOADED && player instanceof ServerPlayer playerMP) {
			PatchouliAPI.get().openBookGUI(playerMP, BOOK_ID);
		}

		return InteractionResult.SUCCESS;
	}

	// Port: Forge's isBookEnchantable and canApplyAtEnchantingTable are replaced by
	// Fabric's ALLOW_ENCHANTING event (EnigmaticLegacyFabric) for Bane of Arthropods.

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		if (Minecraft.getInstance().hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.theAcknowledgment1");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.theAcknowledgment2");
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}
	}

}
