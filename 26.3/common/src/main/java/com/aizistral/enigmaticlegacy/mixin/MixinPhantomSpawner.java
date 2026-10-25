package com.aizistral.enigmaticlegacy.mixin;

import net.minecraft.world.entity.EntityTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.items.CursedRing;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.material.FluidState;

/**
 * Cursed Ring insomnia: an extra phantom spawn pass for ring bearers, on top of vanilla's.
 */
@Mixin(PhantomSpawner.class)
public class MixinPhantomSpawner {
	@Unique
	private int enigmaticlegacy$ticksUntilSpawn = 0;

	/** Port: 26.3 tick takes (ServerLevel, boolean) and returns void, so the spawn count is no longer returned. */
	@Inject(at = @At("RETURN"), method = "tick")
	private void onHandlePhantomSpawns(ServerLevel world, boolean spawnEnemies, CallbackInfo info) {
		if (!spawnEnemies) {
			// NO-OP
		} else if (!world.getGameRules().get(GameRules.SPAWN_PHANTOMS)) {
			// NO-OP
		} else {
			var random = world.getRandom();
			--this.enigmaticlegacy$ticksUntilSpawn;
			if (this.enigmaticlegacy$ticksUntilSpawn > 0) {
				// NO-OP
			} else {
				this.enigmaticlegacy$ticksUntilSpawn += (60 + random.nextInt(60)) * 20;
				if (world.getSkyDarken() < 5 && world.dimensionType().hasSkyLight()) {
					// NO-OP
				} else {
					for (ServerPlayer player : world.players()) {
						if (!player.isSpectator() && !player.isCreative()) {
							BlockPos blockpos = player.blockPosition();
							if (!world.dimensionType().hasSkyLight() || blockpos.getY() >= world.getSeaLevel() && world.canSeeSky(blockpos)) {
								DifficultyInstance difficulty = world.getCurrentDifficultyAt(blockpos);
								if (difficulty.isHarderThan(random.nextFloat() * 3.0F)) {
									ServerStatsCounter stats = player.getStats();
									int ticksSinceRest = Mth.clamp(stats.getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)), 1, Integer.MAX_VALUE);

									if (SuperpositionHandler.hasCurio(player, EnigmaticItems.CURSED_RING) && !CursedRing.disableInsomnia.getValue())
										if (random.nextInt(ticksSinceRest) <= 72000) {
											BlockPos blockpos1 = blockpos.above(20 + random.nextInt(15)).east(-10 + random.nextInt(21)).south(-10 + random.nextInt(21));
											BlockState blockstate = world.getBlockState(blockpos1);
											FluidState fluidstate = world.getFluidState(blockpos1);
											if (NaturalSpawner.isValidEmptySpawnBlock(world, blockpos1, blockstate, fluidstate, EntityTypes.PHANTOM)) {
												SpawnGroupData spawnData = null;
												int l = 1 + random.nextInt(difficulty.getDifficulty().getId() + 1);

												for (int i1 = 0; i1 < l; ++i1) {
													Phantom phantom = EntityTypes.PHANTOM.create(world, EntitySpawnReason.NATURAL);
													phantom.snapTo(blockpos1, 0.0F, 0.0F);
													spawnData = phantom.finalizeSpawn(world, difficulty, EntitySpawnReason.NATURAL, spawnData);
													world.addFreshEntityWithPassengers(phantom);
												}
											}
										}
								}
							}
						}
					}
				}
			}
		}
	}

}
