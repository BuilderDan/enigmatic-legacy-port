package com.aizistral.etherium.items;

import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.api.items.ICreativeTabMember;
import com.aizistral.enigmaticlegacy.api.materials.EnigmaticMaterials;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.objects.Vector3;
import com.aizistral.enigmaticlegacy.registries.EnigmaticTabs;
import com.aizistral.etherium.core.EtheriumUtil;
import com.aizistral.etherium.core.IEtheriumConfig;
import com.aizistral.etherium.core.IEtheriumTool;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class EtheriumSword extends ItemBase implements IEtheriumTool {

	public EtheriumSword() {
		// Port: 1.21 weapons take their attack attributes from the item properties.
		// Port: 26.3 has no SwordItem; the sword properties add the tool component, attributes, durability and repair tag.
		super(EtheriumUtil.defaultProperties(EtheriumSword.class).fireResistant().sword(EnigmaticMaterials.ETHERIUM, 6, -2.6F));
	}

	@Override
	public CreativeModeTab getCreativeTab() {
		return EnigmaticTabs.MAIN;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flagIn) {
		if (Minecraft.getInstance().hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.etheriumSword1");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.etheriumSword2");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.etheriumSword3");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.etheriumSword4", ChatFormatting.GOLD, this.getConfig().getSwordCooldown() / 20F);
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.etheriumSword5");
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}

		if (!this.areaEffectsAllowed(stack)) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.abilityDisabled");
		}
	}

	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (hand == InteractionHand.OFF_HAND || player.getOffhandItem().getUseAnimation() == ItemUseAnimation.BLOCK)
			return InteractionResult.PASS;

		if (player.isCrouching()) {
			this.toggleAreaEffects(player, stack);

			return InteractionResult.SUCCESS;
		} else if (!player.level().isClientSide()) {
			if (!player.getCooldowns().isOnCooldown(new ItemStack(this)) && this.areaEffectsEnabled(player, stack)) {
				Vector3 look = new Vector3(player.getLookAngle());
				Vector3 dir = look.multiply(1D);

				this.getConfig().knockBack(player, 1.0F, dir.x, dir.z);
				world.playSound(null, player.blockPosition(), SoundEvents.SKELETON_SHOOT, SoundSource.PLAYERS, 1.0F, (float) (0.6F + (Math.random() * 0.1D)));

				player.getCooldowns().addCooldown(new ItemStack(this), this.getConfig().getSwordCooldown());

				player.startUsingItem(hand);
				return InteractionResult.SUCCESS;
			}
		}

		player.startUsingItem(hand);
		return InteractionResult.PASS;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (context.getPlayer().isCrouching())
			return this.use(context.getLevel(), context.getPlayer(), context.getHand());
		else
			return super.useOn(context);
	}

}
