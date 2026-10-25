package com.aizistral.enigmaticlegacy.objects;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

/**
 * Port: 26.3 turned Explosion into an interface and made ServerExplosion's block step private.
 * Entity damage and knockback still come from vanilla (super.explode() runs with KEEP), then the
 * blocks are ray-cast here and removed without drops, as upstream's finalizeExplosion did.
 */
public class WitherExplosion extends ServerExplosion {
	private final ServerLevel world;
	private final Vec3 center;
	private final float size;
	private final boolean causesFire;
	private final Explosion.BlockInteraction mode;
	private final ExplosionDamageCalculator calculator;
	private int blockCount;

	public WitherExplosion(ServerLevel worldIn, @Nullable Entity exploderIn, Vec3 center, float sizeIn, boolean causesFireIn, BlockInteraction modeIn) {
		super(worldIn, exploderIn, null, null, center, sizeIn, false, Explosion.BlockInteraction.KEEP);
		this.world = worldIn;
		this.center = center;
		this.size = sizeIn;
		this.causesFire = causesFireIn;
		this.mode = modeIn;
		this.calculator = exploderIn == null ? new ExplosionDamageCalculator() : new EntityBasedExplosionDamageCalculator(exploderIn);
	}

	@Override
	public int explode() {
		super.explode();
		List<BlockPos> toBlow = this.calculateExplodedPositions();

		if (this.mode != Explosion.BlockInteraction.KEEP) {
			Util.shuffle(toBlow, this.world.getRandom());
			ProfilerFiller profiler = Profiler.get();

			for (BlockPos blockpos : toBlow) {
				BlockState blockstate = this.world.getBlockState(blockpos);
				if (!blockstate.isAir()) {
					profiler.push("explosion_blocks");
					// Port: Forge's IBlockStateExtension.onBlockExploded, inlined.
					this.world.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 3);
					blockstate.getBlock().wasExploded(this.world, blockpos, this);
					profiler.pop();
				}
			}
		}

		if (this.causesFire) {
			for (BlockPos blockpos2 : toBlow) {
				if (this.world.getRandom().nextInt(3) == 0 && this.world.getBlockState(blockpos2).isAir() && this.world.getBlockState(blockpos2.below()).isSolidRender()) {
					this.world.setBlockAndUpdate(blockpos2, BaseFireBlock.getState(this.world, blockpos2));
				}
			}
		}

		this.blockCount = toBlow.size();
		return this.blockCount;
	}

	public int getBlockCount() {
		return this.blockCount;
	}

	@Override
	public BlockInteraction getBlockInteraction() {
		return this.mode;
	}

	@Override
	public boolean shouldAffectBlocklikeEntities() {
		return this.world.getGameRules().get(GameRules.MOB_GRIEFING) || this.mode.shouldAffectBlocklikeEntities();
	}

	// Port: copy of vanilla ServerExplosion.calculateExplodedPositions, which is private in 26.3.
	private List<BlockPos> calculateExplodedPositions() {
		Set<BlockPos> toBlowSet = new HashSet<>();

		for (int xx = 0; xx < 16; xx++) {
			for (int yy = 0; yy < 16; yy++) {
				for (int zz = 0; zz < 16; zz++) {
					if (xx == 0 || xx == 15 || yy == 0 || yy == 15 || zz == 0 || zz == 15) {
						double xd = xx / 15.0F * 2.0F - 1.0F;
						double yd = yy / 15.0F * 2.0F - 1.0F;
						double zd = zz / 15.0F * 2.0F - 1.0F;
						double d = Math.sqrt(xd * xd + yd * yd + zd * zd);
						xd /= d;
						yd /= d;
						zd /= d;
						float remainingPower = this.size * (0.7F + this.world.getRandom().nextFloat() * 0.6F);
						double xp = this.center.x;
						double yp = this.center.y;
						double zp = this.center.z;

						while (remainingPower > 0.0F) {
							BlockPos pos = BlockPos.containing(xp, yp, zp);
							BlockState block = this.world.getBlockState(pos);
							FluidState fluid = this.world.getFluidState(pos);
							if (!this.world.isInWorldBounds(pos)) {
								break;
							}

							Optional<Float> resistance = this.calculator.getBlockExplosionResistance(this, this.world, pos, block, fluid);
							if (resistance.isPresent()) {
								remainingPower -= (resistance.get() + 0.3F) * 0.3F;
							}

							if (remainingPower > 0.0F && this.calculator.shouldBlockExplode(this, this.world, pos, block, remainingPower)) {
								toBlowSet.add(pos);
							}

							xp += xd * 0.3F;
							yp += yd * 0.3F;
							zp += zd * 0.3F;
							remainingPower -= 0.22500001F;
						}
					}
				}
			}
		}

		return new ObjectArrayList<>(toBlowSet);
	}

}
