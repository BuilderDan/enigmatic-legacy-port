package com.aizistral.etherium.items;

import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.api.items.ICreativeTabMember;
import com.aizistral.enigmaticlegacy.api.materials.EnigmaticMaterials;
import com.aizistral.enigmaticlegacy.helpers.AOEMiningHelper;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.registries.EnigmaticTabs;
import com.aizistral.etherium.core.EtheriumUtil;
import com.aizistral.etherium.core.IEtheriumConfig;
import com.aizistral.etherium.core.IEtheriumTool;
import com.google.common.collect.Sets;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class EtheriumAxe extends ItemBase implements IEtheriumTool {
	public Set<TagKey<Block>> effectiveTags;

	public EtheriumAxe() {
		// Port: 1.21 weapons take their attack attributes from the item properties.
		// Port: 26.3 has no AxeItem; the axe properties add the tool component, attributes, durability and repair tag.
		// Vanilla's axe properties also add the log-stripping block transformer that super.useOn applies.
		super(EtheriumUtil.defaultProperties(EtheriumAxe.class).fireResistant().axe(EnigmaticMaterials.ETHERIUM, 10, -3.2F));
		this.effectiveTags = Sets.newHashSet();
		this.effectiveTags.add(BlockTags.MINEABLE_WITH_AXE);
	}

	@Override
	public CreativeModeTab getCreativeTab() {
		return EnigmaticTabs.MAIN;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flagIn) {
		if (this.getConfig().getAxeMiningVolume() == -1)
			return;

		if (Minecraft.getInstance().hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.etheriumAxe1", ChatFormatting.GOLD, this.getConfig().getAxeMiningVolume() + this.getConfig().getAOEBoost(Minecraft.getInstance().player));
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");

			if (!this.getConfig().disableAOEShiftInhibition()) {
				ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.etheriumAxe2");
			}
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.etheriumAxe3");
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}

		if (!this.areaEffectsAllowed(stack)) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.aoeDisabled");
		}
	}

	@Override
	public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity entityLiving) {
		if (entityLiving instanceof Player && this.areaEffectsEnabled((Player) entityLiving, stack) && this.isCorrectToolForDrops(stack, state) && !world.isClientSide() && this.getConfig().getAxeMiningVolume() != -1) {
			Direction face = Direction.UP;
			int volume = this.getConfig().getAxeMiningVolume() + (this.getConfig().getAOEBoost((Player) entityLiving));

			AOEMiningHelper.harvestCube(world, (Player) entityLiving, face, pos.offset(0, (volume - 1) / 2, 0), (s) -> this.isCorrectToolForDrops(stack, s), volume, volume, true, pos, stack, (objPos, objState) -> {
				stack.hurtAndBreak(1, entityLiving, EquipmentSlot.MAINHAND);
			});
		}

		return super.mineBlock(stack, world, state, pos, entityLiving);
	}

	@Override
	public boolean isCorrectToolForDrops(ItemStack stack, BlockState blockIn) {
		return super.isCorrectToolForDrops(stack, blockIn) || this.hasAnyTag(blockIn, this.effectiveTags);
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		return !this.hasAnyTag(state, this.effectiveTags) ? super.getDestroySpeed(stack, state) : EnigmaticMaterials.ETHERIUM.speed();
	}

	protected boolean hasAnyTag(BlockState state, Set<TagKey<Block>> tags) {
		return tags.stream().anyMatch(tag -> this.hasTag(state, tag));
	}

	protected boolean hasTag(BlockState state, TagKey<Block> tag) {
		return state.is(tag);
	}

	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		player.startUsingItem(hand);

		if (player.isCrouching()) {
			this.toggleAreaEffects(player, stack);

			return InteractionResult.SUCCESS;
		} else
			return super.use(world, player, hand);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (context.getPlayer().isCrouching())
			return this.use(context.getLevel(), context.getPlayer(), context.getHand());
		else
			return super.useOn(context);
	}

}
