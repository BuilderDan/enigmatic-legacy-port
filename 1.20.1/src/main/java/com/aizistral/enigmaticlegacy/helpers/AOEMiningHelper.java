package com.aizistral.enigmaticlegacy.helpers;

import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.items.generic.ItemBase;
import com.aizistral.enigmaticlegacy.objects.Vector3;
import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Port: attemptBreakNeighbors is left out (unused upstream). Experience is dropped by
 * vanilla Block.dropResources, so the Forge getExpDrop call is gone.
 */
public class AOEMiningHelper {
	public static final Random random = new Random();

	/**
	 * To break, the given block must be contained in effectiveOn, or its material contained in effectiveMaterials, and
	 * the block cannot have the wither-immune tag. Wither-immune seems to be the only comprehensive list of "blocks you
	 * shouldn't be able to break" in the game.
	 *
	 * @param toolDamageConsumer Consumer that is called for each block destroyed. It is expected to either damage
	 * or not damage the tool used for mining the block.
	 */

	public static void attemptBreak(Level world, BlockPos pos, Player player, Set<Block> effectiveOn, Predicate<BlockState> predicate, boolean checkHarvestLevel, ItemStack tool, BiConsumer<BlockPos, BlockState> toolDamageConsumer) {
		BlockState state = world.getBlockState(pos);
		BlockEntity iCertainlyHopeYouHaveATileEntityLicense = world.getBlockEntity(pos);

		boolean validHarvest = !checkHarvestLevel || player.getMainHandItem().isCorrectToolForDrops(state);
		boolean isEffective = effectiveOn.contains(state.getBlock()) || predicate.test(state);
		boolean unbreakable = state.is(BlockTags.WITHER_IMMUNE) || state.getBlock() == Blocks.SPAWNER || state.getDestroySpeed(world, pos) < 0F;

		if (validHarvest && isEffective && !unbreakable) {
			world.destroyBlock(pos, false);
			Block.dropResources(state, world, pos, iCertainlyHopeYouHaveATileEntityLicense, player, player.getMainHandItem());

			toolDamageConsumer.accept(pos, state);
		}
	}

	public static BlockHitResult calcRayTrace(Level worldIn, Player player, ClipContext.Fluid fluidMode) {
		return ItemBase.rayTrace(worldIn, player, fluidMode);
	}

	public static Vector3 calcRayTrace(Level worldIn, Player player, ClipContext.Fluid fluidMode, double distance) {
		float f = player.getXRot();
		float f1 = player.getYRot();
		Vec3 vector3d = player.getEyePosition(1.0F);
		float f2 = Mth.cos(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
		float f3 = Mth.sin(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
		float f4 = -Mth.cos(-f * ((float) Math.PI / 180F));
		float f5 = Mth.sin(-f * ((float) Math.PI / 180F));
		float f6 = f3 * f4;
		float f7 = f2 * f4;
		double d0 = distance;
		Vec3 vector3d1 = vector3d.add(f6 * d0, f5 * d0, f7 * d0);
		HitResult result = worldIn.clip(new ClipContext(vector3d, vector3d1, ClipContext.Block.OUTLINE, fluidMode, player));

		if (result.getType() == HitResult.Type.BLOCK)
			return new Vector3(result.getLocation());
		else {
			Vector3 vec = new Vector3(player.getLookAngle()).multiply(64F).add(new Vector3(player.position()));
			return vec;
		}
	}

	public static void harvestPlane(Level world, Player player, Direction dir, BlockPos pos, Predicate<BlockState> predicate, int radius, boolean harvestLevelCheck, @Nullable BlockPos excludedBlock, ItemStack tool, BiConsumer<BlockPos, BlockState> toolDamageConsumer) {
		int supRad = (radius - 1) / 2;

		for (int a = -supRad; a <= supRad; a++) {
			for (int b = -supRad; b <= supRad; b++) {
				BlockPos target = null;

				if (dir == Direction.UP || dir == Direction.DOWN) {
					target = pos.offset(a, 0, b);
				}
				if (dir == Direction.NORTH || dir == Direction.SOUTH) {
					target = pos.offset(a, b, 0);
				}
				if (dir == Direction.EAST || dir == Direction.WEST) {
					target = pos.offset(0, a, b);
				}

				if (excludedBlock != null && target != null)
					if (target.equals(excludedBlock)) {
						continue;
					}

				AOEMiningHelper.attemptBreak(world, target, player, Sets.newHashSet(), predicate, harvestLevelCheck, tool, toolDamageConsumer);
			}
		}
	}

	public static void harvestCube(Level world, Player player, Direction dir, BlockPos centralPos, Predicate<BlockState> predicate, int planeRadius, int depth, boolean harvestLevelCheck, @Nullable BlockPos excludedBlock, ItemStack tool, BiConsumer<BlockPos, BlockState> toolDamageConsumer) {

		for (int a = 0; a < depth; a++) {
			int x = 0;
			int y = 0;
			int z = 0;

			if (dir == Direction.UP) {
				y -= a;
			}
			if (dir == Direction.DOWN) {
				y += a;
			}
			if (dir == Direction.SOUTH) {
				z -= a;
			}
			if (dir == Direction.NORTH) {
				z += a;
			}
			if (dir == Direction.EAST) {
				x -= a;
			}
			if (dir == Direction.WEST) {
				x += a;
			}

			AOEMiningHelper.harvestPlane(world, player, dir, centralPos.offset(x, y, z), predicate, planeRadius, harvestLevelCheck, excludedBlock, tool, toolDamageConsumer);
		}
	}

}
