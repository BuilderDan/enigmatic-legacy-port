package com.aizistral.enigmaticlegacy.items;

import net.minecraft.client.Minecraft;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import com.aizistral.enigmaticlegacy.client.Quote;
import net.minecraft.server.level.ServerPlayer;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.enigmaticlegacy.registries.EnigmaticItems;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import com.aizistral.enigmaticlegacy.platform.accessory.SlotContext;

public class MonsterCharm extends ItemBaseCurio {
	public static Omniconfig.PerhapsParameter undeadDamageBonus;
	public static Omniconfig.PerhapsParameter hostileDamageBonus;
	public static Omniconfig.BooleanParameter bonusLootingEnabled;
	public static Omniconfig.BooleanParameter doubleXPEnabled;

	@SubscribeConfig
	public static void onConfig(OmniconfigWrapper builder) {
		builder.pushPrefix("MonsterCharm");

		undeadDamageBonus = builder
				.comment("Damage bonus against undead enemies for Emblem of Monster Slayer. Defined as percentage.")
				.max(1000)
				.getPerhaps("UndeadDamage", 25);

		hostileDamageBonus = builder
				.comment("Damage bonus against agressive creatures for Emblem of Monster Slayer. Defined as percentage.")
				.max(1000)
				.getPerhaps("HostileDamage", 10);

		bonusLootingEnabled = builder
				.comment("Whether or not Emblem of Monster Slayer should provide +1 Looting Level.")
				.getBoolean("BonusLooting", true);

		doubleXPEnabled = builder
				.comment("Whether or not Emblem of Monster Slayer should provide double experience drop from monsters.")
				.getBoolean("DoubleXP", true);

		builder.popPrefix();
	}

	public float bonusXPModifier = 2.0F;

	public MonsterCharm() {
		super(ItemBaseCurio.getDefaultProperties().rarity(Rarity.EPIC));
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flagIn) {
		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");

		if (Minecraft.getInstance().hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.monsterCharm1", ChatFormatting.GOLD, undeadDamageBonus.getValue().asPercentage() + "%");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.monsterCharm2", ChatFormatting.GOLD, hostileDamageBonus.getValue().asPercentage() + "%");
			if (bonusLootingEnabled.getValue()) {
				ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.monsterCharm3");
			}
			if (doubleXPEnabled.getValue()) {
				ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
				ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.monsterCharm4");
			}
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}
	}

	@Override
	public int getLootingLevel(SlotContext slotContext, @Nullable DamageSource source, @Nullable LivingEntity target, int baseLooting, ItemStack curio) {
		// Port: upstream always adds 1, ignoring BonusLooting; the port honors the option,
		// matching the tooltip. Applied through MixinEnchantmentHelper.
		int bonus = bonusLootingEnabled.getValue() ? 1 : 0;
		return super.getLootingLevel(slotContext, source, target, baseLooting, curio) + bonus;
	}

	/**
	 * Port: upstream's LivingHurtEvent case for this charm (EnigmaticEventHandler), called from
	 * MixinLivingEntity at the head of actuallyHurt, where Forge fires that event.
	 */
	public static float modifyHurtAmount(LivingEntity target, DamageSource source, float amount) {
		if (!(target instanceof Monster || target instanceof EnderDragon))
			return amount;
		if (target instanceof EnderDragon && source.getEntity() instanceof ServerPlayer splayer) {
			Quote.POOR_CREATURE.playOnceIfUnlocked(splayer, 60);
		}
		if (!(source.getEntity() instanceof Player player) || !SuperpositionHandler.hasCurio(player, EnigmaticItems.MONSTER_CHARM))
			return amount;

		Mob mob = (Mob) target;

		if (mob.isInvertedHealAndHarm())
			return amount * undeadDamageBonus.getValue().asModifier(true);
		else if (mob.isAggressive() || mob instanceof Creeper) {
			if (mob instanceof Enderman || mob instanceof ZombifiedPiglin || mob instanceof Blaze || mob instanceof Guardian
					|| mob instanceof ElderGuardian || !mob.canTeleport(mob.level(), mob.level()))
				return amount;
			return amount * hostileDamageBonus.getValue().asModifier(true);
		}

		return amount;
	}

	/**
	 * Port: upstream's LivingExperienceDropEvent case for this charm, called from
	 * MixinLivingEntity in dropExperience. Upstream ignores the DoubleXP option here; the port
	 * honors it, matching the tooltip.
	 */
	public static int modifyExperience(LivingEntity entity, @Nullable Player attacker, int experience) {
		if (!doubleXPEnabled.getValue() || !(entity instanceof Monster) || attacker == null)
			return experience;
		if (!SuperpositionHandler.hasCurio(attacker, EnigmaticItems.MONSTER_CHARM))
			return experience;
		return experience + (int) (experience * (EnigmaticItems.MONSTER_CHARM.bonusXPModifier - 1));
	}

}
