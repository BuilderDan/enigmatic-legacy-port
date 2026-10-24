package com.aizistral.etherium.core;

import net.minecraft.core.Holder;
import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.objects.Perhaps;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public interface IEtheriumConfig {

	public Ingredient getRepairMaterial();

	public CreativeModeTab getCreativeTab();

	public String getOwnerMod();

	public Holder<ArmorMaterial> getArmorMaterial();

	public Tier getToolMaterial();

	public Perhaps getShieldThreshold(@Nullable Player player);

	public Perhaps getShieldReduction();

	public boolean disableAOEShiftInhibition();

	public SoundEvent getAOESoundOn();

	public SoundEvent getAOESoundOff();

	public SoundEvent getShieldTriggerSound();

	public int getAxeMiningVolume();

	public int getScytheMiningVolume();

	public int getPickaxeMiningRadius();

	public int getPickaxeMiningDepth();

	public int getShovelMiningRadius();

	public int getShovelMiningDepth();

	public int getSwordCooldown();

	public int getAOEBoost(@Nullable Player player);

	public void knockBack(LivingEntity entityIn, float strength, double xRatio, double zRatio);

	public boolean isStandalone();

}
