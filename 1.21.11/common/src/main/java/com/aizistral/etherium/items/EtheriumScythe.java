package com.aizistral.etherium.items;

import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import java.util.List;
import java.util.Map;
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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class EtheriumScythe extends ItemBase implements IEtheriumTool {
	protected static final Map<Block, BlockState> HOE_LOOKUP = Maps.newHashMap(ImmutableMap.of(Blocks.GRASS_BLOCK,
			Blocks.FARMLAND.defaultBlockState(), Blocks.DIRT_PATH, Blocks.FARMLAND.defaultBlockState(), Blocks.DIRT,
			Blocks.FARMLAND.defaultBlockState(), Blocks.COARSE_DIRT, Blocks.DIRT.defaultBlockState()));
	public Set<TagKey<Block>> effectiveTags;
	public Set<Block> effectiveBlocks;

	public EtheriumScythe() {
		// Port: 1.21 weapons take their attack attributes from the item properties.
		// Port: 26.3 has no SwordItem; the sword properties add the tool component, attributes, durability and repair tag.
		super(EtheriumUtil.defaultProperties(EtheriumScythe.class).fireResistant().sword(EnigmaticMaterials.ETHERIUM, 3, -2.0F));
		this.effectiveTags = Sets.newHashSet();
		this.effectiveTags.add(BlockTags.MINEABLE_WITH_HOE);
		this.effectiveTags.add(BlockTags.FLOWERS);
		this.effectiveTags.add(BlockTags.SMALL_FLOWERS);
		this.effectiveTags.add(BlockTags.FLOWERS);
		this.effectiveTags.add(BlockTags.LEAVES);
		this.effectiveTags.add(BlockTags.CAVE_VINES);
		this.effectiveTags.add(BlockTags.CROPS);
		this.effectiveTags.add(BlockTags.REPLACEABLE);

		this.effectiveBlocks = Sets.newHashSet();
		this.effectiveBlocks.add(Blocks.BAMBOO);
		this.effectiveBlocks.add(Blocks.BAMBOO_SAPLING);
		this.effectiveBlocks.add(Blocks.SHORT_GRASS);
		this.effectiveBlocks.add(Blocks.TALL_GRASS);
		this.effectiveBlocks.add(Blocks.SEAGRASS);
		this.effectiveBlocks.add(Blocks.TALL_SEAGRASS);
		this.effectiveBlocks.add(Blocks.NETHER_WART);
		this.effectiveBlocks.add(Blocks.CACTUS);
		this.effectiveBlocks.add(Blocks.SUGAR_CANE);
	}

	@Override
	public CreativeModeTab getCreativeTab() {
		return EnigmaticTabs.MAIN;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flagIn) {
		if (this.getConfig().getScytheMiningVolume() == -1)
			return;

		if (Minecraft.getInstance().hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.etheriumScythe1", ChatFormatting.GOLD, this.getConfig().getScytheMiningVolume() + this.getConfig().getAOEBoost(Minecraft.getInstance().player));
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.etheriumScythe2", ChatFormatting.GOLD, this.getConfig().getScytheMiningVolume());
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			if (!this.getConfig().disableAOEShiftInhibition()) {
				ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.etheriumScythe3");
			}
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.etheriumScythe4");
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}

		if (!this.areaEffectsAllowed(stack)) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.aoeDisabled");
		}
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

		InteractionResult type = Items.DIAMOND_HOE.useOn(context);

		if (!this.areaEffectsEnabled(context.getPlayer(), context.getItemInHand()))
			return type;

		int supRad = (this.getConfig().getScytheMiningVolume() - 1) / 2;

		if (type == InteractionResult.CONSUME) {
			for (int x = -supRad; x <= supRad; x++) {
				for (int z = -supRad; z <= supRad; z++) {
					if (x == 0 & z == 0) {
						continue;
					}

					Items.DIAMOND_HOE.useOn(new UseOnContext(context.getPlayer(), context.getHand(), new BlockHitResult(context.getClickLocation().add(x, 0, z), Direction.UP, context.getClickedPos().offset(x, 0, z), context.isInside())));
				}
			}
		}

		return type;
	}

	@Override
	public boolean isCorrectToolForDrops(ItemStack stack, BlockState blockIn) {
		return super.isCorrectToolForDrops(stack, blockIn) || this.effectiveBlocks.contains(blockIn.getBlock())
				|| this.hasAnyTag(blockIn, this.effectiveTags);
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		return (!this.effectiveBlocks.contains(state.getBlock()) && !this.hasAnyTag(state, this.effectiveTags))
				? super.getDestroySpeed(stack, state) : EnigmaticMaterials.ETHERIUM.speed();
	}

	protected boolean hasAnyTag(BlockState state, Set<TagKey<Block>> tags) {
		return tags.stream().anyMatch(tag -> this.hasTag(state, tag));
	}

	protected boolean hasTag(BlockState state, TagKey<Block> tag) {
		return state.is(tag);
	}


	@Override
	public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity entityLiving) {
		if (entityLiving instanceof Player && this.areaEffectsEnabled((Player) entityLiving, stack) && this.isCorrectToolForDrops(stack, state) && !world.isClientSide() && this.getConfig().getScytheMiningVolume() != -1) {
			Direction face = Direction.UP;
			int volume = this.getConfig().getScytheMiningVolume() + (this.getConfig().getAOEBoost((Player) entityLiving));

			AOEMiningHelper.harvestCube(world, (Player) entityLiving, face, pos.offset(0, (volume - 1) / 2, 0), (s) -> this.isCorrectToolForDrops(stack, s), volume, volume, false, pos, stack, (objPos, objState) -> {
				stack.hurtAndBreak(1, entityLiving, EquipmentSlot.MAINHAND);
			});
		}

		return super.mineBlock(stack, world, state, pos, entityLiving);
	}

}
