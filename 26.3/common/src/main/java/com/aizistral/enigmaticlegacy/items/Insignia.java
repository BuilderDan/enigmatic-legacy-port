package com.aizistral.enigmaticlegacy.items;

import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Holder;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.enigmaticlegacy.platform.Services;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.enigmaticlegacy.registries.EnigmaticSounds;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
// Port: Curios SlotContext -> platform SlotContext (Trinkets on Fabric).
import com.aizistral.enigmaticlegacy.platform.accessory.SlotContext;

public class Insignia extends ItemBaseCurio {

	public Insignia() {
		super(getDefaultProperties().rarity(Rarity.UNCOMMON).stacksTo(1));
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flagIn) {
		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");

		if (Minecraft.getInstance().hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.insignia1");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.insignia2");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.insignia3");
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}

		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");

		this.getCustomName(stack).ifPresent(component -> {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.insigniaName", null, component);
		});

		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.insigniaTagDisplay" +
				(ItemNBTHelper.getBoolean(stack, "tagDisplayEnabled", true) ? "Enabled" : "Disabled"));
	}

	public Optional<Component> getCustomName(ItemStack stack) {
		// Port: the display name is the CUSTOM_NAME component in 1.21.
		return Optional.ofNullable(stack.get(DataComponents.CUSTOM_NAME));
	}

	public boolean canSeeTrueName(Player player) {
		return player.isCreative() || SuperpositionHandler.hasCurio(player, EnigmaticItems.ENIGMATIC_EYE);
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, Identifier id, ItemStack stack) {
		Multimap<Holder<Attribute>, AttributeModifier> attributes = HashMultimap.create();
		Services.ACCESSORIES.addSlotModifier(attributes, "charm",
				Identifier.fromNamespaceAndPath(EnigmaticLegacy.MODID, "insignia_charm_slot"), 1, Operation.ADD_VALUE);
		return attributes;
	}

	@Override
	public boolean canEquipFromUse(SlotContext context, ItemStack stack) {
		return false;
	}

	@Override
	public InteractionResult use(Level world, Player player, InteractionHand handIn) {
		if (player.isCrouching()) {
			ItemStack stack = player.getItemInHand(handIn);

			if (ItemNBTHelper.getBoolean(stack, "tagDisplayEnabled", true)) {
				ItemNBTHelper.setBoolean(stack, "tagDisplayEnabled", false);
				world.playSound(null, player.blockPosition(), EnigmaticSounds.CHARGED_OFF, SoundSource.PLAYERS, (float) (0.8F + (Math.random() * 0.2F)), (float) (0.8F + (Math.random() * 0.2F)));
			} else {
				ItemNBTHelper.setBoolean(stack, "tagDisplayEnabled", true);
				world.playSound(null, player.blockPosition(), EnigmaticSounds.CHARGED_ON, SoundSource.PLAYERS, (float) (0.8F + (Math.random() * 0.2F)), (float) (0.8F + (Math.random() * 0.2F)));
			}

			player.swing(handIn, SwingAnimation.DEFAULT, false);

			return InteractionResult.SUCCESS;
		} else
			return super.use(world, player, handIn);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		return this.use(context.getLevel(), context.getPlayer(), context.getHand());
	}

}

