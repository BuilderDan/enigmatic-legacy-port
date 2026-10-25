package com.aizistral.enigmaticlegacy.helpers;

import net.minecraft.world.entity.player.Player;

/**
 * A couple of methods for messing around with player's experience.
 * Originated from Botania's code.
 * @author Integral
 */

public class ExperienceHelper {

	public static int getPlayerXP(Player player) {
		return (int) (getExperienceForLevel(player.experienceLevel) + player.experienceProgress * player.getXpNeededForNextLevel());
	}

	public static int getPlayerXPLevel(Player player) {
		return player.experienceLevel;
	}

	public static void drainPlayerXP(Player player, int amount) {
		addPlayerXP(player, -amount);
	}

	public static void addPlayerXP(Player player, int amount) {
		// Port: upstream builds Forge PlayerXpEvent.XpChange/LevelChange here but never posts
		// them, so they changed nothing and are dropped. The unused level-change branch went with them.
		int experience = getPlayerXP(player) + amount;

		player.totalExperience = experience;
		player.experienceLevel = getLevelForExperience(experience);
		int expForLevel = getExperienceForLevel(player.experienceLevel);
		player.experienceProgress = (float) (experience - expForLevel) / (float) player.getXpNeededForNextLevel();
	}

	public static int getExperienceForLevel(int level) {
		if (level <= 0)
			return 0;

		if (level > 0 && level < 17)
			return (level * level + 6 * level);
		else if (level > 16 && level < 32)
			return (int) (2.5 * level * level - 40.5 * level + 360);
		else
			return (int) (4.5 * level * level - 162.5 * level + 2220);
	}

	public static int getLevelForExperience(int experience) {
		if (experience <= 0)
			return 0;

		int i = 0;
		while (getExperienceForLevel(i) <= experience) {
			i++;
		}

		return i - 1;
	}

}
