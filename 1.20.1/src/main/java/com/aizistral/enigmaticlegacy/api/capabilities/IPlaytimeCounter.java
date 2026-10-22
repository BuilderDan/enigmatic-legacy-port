package com.aizistral.enigmaticlegacy.api.capabilities;

import com.aizistral.enigmaticlegacy.platform.Services;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

// Port: no longer a Forge capability; get() goes through the platform's player data.
public interface IPlaytimeCounter {
	public static final IPlaytimeCounter DEFAULT = new IPlaytimeCounter() {
		@Override
		public void setTimeWithoutCurses(long time) {
			// NO-OP
		}

		@Override
		public void setTimeWithCurses(long time) {
			// NO-OP
		}

		@Override
		public void incrementTimeWithoutCurses() {
			// NO-OP
		}

		@Override
		public void incrementTimeWithCurses() {
			// NO-OP
		}

		@Override
		public long getTimeWithoutCurses() {
			return 0L;
		}

		@Override
		public long getTimeWithCurses() {
			return 1L;
		}

		@Override
		public void matchStats() {
			// NO-OP
		}

		@Override
		public CompoundTag serializeNBT() {
			return new CompoundTag();
		}

		@Override
		public void deserializeNBT(CompoundTag nbt) {
			// NO-OP
		}
	};

	public static IPlaytimeCounter get(Player player) {
		IPlaytimeCounter counter = Services.PLAYER_DATA.getPlaytimeCounter(player);
		return counter != null ? counter : DEFAULT;
	}

	public long getTimeWithoutCurses();

	public long getTimeWithCurses();

	public void setTimeWithoutCurses(long time);

	public void setTimeWithCurses(long time);

	public void incrementTimeWithoutCurses();

	public void incrementTimeWithCurses();

	public void matchStats();

	public CompoundTag serializeNBT();

	public void deserializeNBT(CompoundTag nbt);

}
