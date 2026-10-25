package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.enigmaticlegacy.registries.EnigmaticBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

/**
 * Upstream overrides the whole PlayerList.respawn to support End Anchors. Port: 26.3 resolves the
 * respawn point through ServerPlayer.findRespawnPositionAndUseSpawnBlock, which MixinServerPlayer
 * hooks, so all that is left here is the deplete sound vanilla plays for a Respawn Anchor.
 */
@Mixin(PlayerList.class)
public class MixinPlayerList {
	@Shadow @Final
	private MinecraftServer server;

	@Inject(method = "respawn", at = @At("RETURN"))
	private void enigmaticlegacy$onRespawn(ServerPlayer oldPlayer, boolean keepAllPlayerData,
			Entity.RemovalReason reason, CallbackInfoReturnable<ServerPlayer> info) {
		ServerPlayer player = info.getReturnValue();
		ServerPlayer.RespawnConfig config = player.getRespawnConfig();

		if (keepAllPlayerData || config == null)
			return;

		ServerLevel level = this.server.getLevel(config.respawnData().dimension());
		BlockPos pos = config.respawnData().pos();

		if (level != null && level.getBlockState(pos).is(EnigmaticBlocks.END_ANCHOR)) {
			player.connection.send(new ClientboundSoundPacket(SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS,
					pos.getX(), pos.getY(), pos.getZ(), 1.0F, 1.0F, level.getRandom().nextLong()));
		}
	}
}
