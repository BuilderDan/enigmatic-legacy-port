package com.aizistral.enigmaticlegacy.platform.network;

import java.util.concurrent.Executor;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.level.ServerPlayer;

/**
 * Port-only stand-in for Forge's NetworkEvent.Context, keeping the three calls upstream
 * packet handlers make.
 */
public class PacketContext {
	private final Executor executor;
	@Nullable
	private final ServerPlayer sender;

	public PacketContext(Executor executor, @Nullable ServerPlayer sender) {
		this.executor = executor;
		this.sender = sender;
	}

	/** Runs the task on the main thread of the receiving side. */
	public void enqueueWork(Runnable task) {
		this.executor.execute(task);
	}

	/** The player who sent the packet; null for packets received by the client. */
	@Nullable
	public ServerPlayer getSender() {
		return this.sender;
	}

	/** Kept for upstream compatibility; does nothing. */
	public void setPacketHandled(boolean handled) {
		// NO-OP
	}
}
