package com.aizistral.enigmaticlegacy.api.capabilities;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;

// Port: Forge capability Provider removed; each loader attaches the counter its own way.
public class PlayerPlaytimeCounter implements IPlaytimeCounter {
	public static final ResourceLocation TIME_WITH_CURSES_STAT = ResourceLocation.fromNamespaceAndPath(EnigmaticLegacy.MODID, "play_time_with_seven_curses");
	public static final ResourceLocation TIME_WITHOUT_CURSES_STAT = ResourceLocation.fromNamespaceAndPath(EnigmaticLegacy.MODID, "play_time_without_seven_curses");

	private final Player player;
	private long timeWithCurses, timeWithoutCurses;

	public PlayerPlaytimeCounter(Player player) {
		this.player = player;
	}

	// Port: upstream never registers these stats in code; Stats.CUSTOM.get() needs them registered.
	public static void registerStats() {
		for (ResourceLocation stat : new ResourceLocation[] { TIME_WITH_CURSES_STAT, TIME_WITHOUT_CURSES_STAT }) {
			Registry.register(BuiltInRegistries.CUSTOM_STAT, stat, stat);
			Stats.CUSTOM.get(stat, StatFormatter.TIME);
		}
	}

	@Override
	public long getTimeWithoutCurses() {
		return this.timeWithoutCurses;
	}

	@Override
	public long getTimeWithCurses() {
		return this.timeWithCurses;
	}

	@Override
	public void setTimeWithoutCurses(long time) {
		if (time != this.timeWithoutCurses) {
			this.updateStat(TIME_WITHOUT_CURSES_STAT, (int) time);
		}

		this.timeWithoutCurses = time;
	}

	@Override
	public void setTimeWithCurses(long time) {
		if (time != this.timeWithCurses) {
			this.updateStat(TIME_WITH_CURSES_STAT, (int) time);
		}

		this.timeWithCurses = time;
	}

	@Override
	public void incrementTimeWithoutCurses() {
		this.updateStat(TIME_WITHOUT_CURSES_STAT, (int) ++this.timeWithoutCurses);
	}

	@Override
	public void incrementTimeWithCurses() {
		this.updateStat(TIME_WITH_CURSES_STAT, (int) ++this.timeWithCurses);
	}

	@Override
	public void matchStats() {
		this.updateStat(TIME_WITH_CURSES_STAT, (int) this.timeWithCurses);
		this.updateStat(TIME_WITHOUT_CURSES_STAT, (int) this.timeWithoutCurses);
	}

	private void updateStat(ResourceLocation stat, int value) {
		if (this.player instanceof ServerPlayer player) {
			player.getStats().setValue(player, Stats.CUSTOM.get(stat), value);
		}
	}

	@Override
	public CompoundTag serializeNBT() {
		var tag = new CompoundTag();
		tag.putLong("timeWithCurses", this.timeWithCurses);
		tag.putLong("timeWithoutCurses", this.timeWithoutCurses);
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag tag) {
		this.timeWithCurses = tag.getLong("timeWithCurses");
		this.timeWithoutCurses = tag.getLong("timeWithoutCurses");
	}

}
