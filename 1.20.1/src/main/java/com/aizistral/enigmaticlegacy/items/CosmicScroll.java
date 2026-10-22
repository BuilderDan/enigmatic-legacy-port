package com.aizistral.enigmaticlegacy.items;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.aizistral.enigmaticlegacy.EnigmaticLegacy;
import com.aizistral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.aizistral.enigmaticlegacy.api.items.IHidden;
import com.aizistral.enigmaticlegacy.handlers.DevotedBelieversHandler;
import com.aizistral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.aizistral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.aizistral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.aizistral.enigmaticlegacy.packets.clients.PacketCosmicRevive;
import com.aizistral.omniconfig.wrappers.Omniconfig;
import com.aizistral.omniconfig.wrappers.OmniconfigWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
// Port: Forge PacketDistributor -> platform Services.NETWORK.
import com.aizistral.enigmaticlegacy.platform.Services;
import com.aizistral.enigmaticlegacy.platform.network.TargetPoint;
// Port: Curios SlotContext -> platform SlotContext (Trinkets on Fabric).
import com.aizistral.enigmaticlegacy.platform.accessory.SlotContext;

public class CosmicScroll extends ItemBaseCurio implements IHidden {
	private static final ResourceLocation ADVANCEMENT = new ResourceLocation(EnigmaticLegacy.MODID, "main/cosmic_scroll");
	public static Omniconfig.PerhapsParameter unchosenDamageBonus;
	public static Omniconfig.PerhapsParameter unchosenKnockbackBonus;
	public static Omniconfig.PerhapsParameter etheriumShieldThreshold;
	public static Omniconfig.IntParameter deathProtectionCooldown;

	@SubscribeConfig
	public static void handleConfig(OmniconfigWrapper builder) {
		builder.pushPrefix("CosmicScroll");

		etheriumShieldThreshold = builder
				.comment("Alternative Etherium Shield health requirement for those who bear The Architect's Favor. Defined as percentage.")
				.max(100)
				.getPerhaps("EtheriumShieldThreshold", 80);

		unchosenDamageBonus = builder
				.comment("Attack damage bonus of The Architect's Favor against non-chosen players.")
				.getPerhaps("UnchosenDamageBonus", 100);

		unchosenKnockbackBonus = builder
				.comment("Knockback bonus of The Architect's Favor against non-chosen players.")
				.getPerhaps("UnchosenKnockbackBonus", 100);

		deathProtectionCooldown = builder
				.comment("Cooldown of death protection ability of The Architect's Favor. Measured in seconds.")
				.getInt("DeathProtectionCooldown", 600);

		builder.popPrefix();
	}

	public CosmicScroll() {
		super(getDefaultProperties().rarity(Rarity.EPIC));
		// Port: the Forge event handlers below are called from EnigmaticEventHandler and the client.
	}

	@Override
	public CreativeModeTab getCreativeTab() {
		return null;
	}

	@Override
	// Port: @OnlyIn(Dist.CLIENT) removed (see CHANGES.md).
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");

		if (Screen.hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cosmicScroll1");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cosmicScroll2");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cosmicScroll3");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cosmicScroll4", ChatFormatting.GOLD, etheriumShieldThreshold + "%");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cosmicScroll5");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cosmicScroll6");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cosmicScroll7");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cosmicScroll8");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cosmicScroll9");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cosmicScroll10");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cosmicScroll11", ChatFormatting.GOLD, deathProtectionCooldown);
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cosmicScroll12", ChatFormatting.GOLD, unchosenDamageBonus + "%", unchosenKnockbackBonus + "%");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cosmicScroll13");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cosmicScroll14");
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cosmicScrollLore1");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cosmicScrollLore2");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}

		if (this.hasCooldown(stack)) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.cosmicScrollCooldown", ChatFormatting.GOLD, (this.getCooldown(stack)/20));
		}

		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
		ItemLoreHelper.indicateBlessedOnesOnly(list);
	}

	public boolean hasCooldown(ItemStack scroll) {
		return this.getCooldown(scroll) > 0;
	}

	public int getCooldown(ItemStack scroll) {
		if (scroll.is(this))
			return ItemNBTHelper.getInt(scroll, "ReviveCooldown", 0);
		else
			return 0;
	}

	public void setCooldown(ItemStack scroll, int cooldown) {
		if (scroll.is(this)) {
			ItemNBTHelper.setInt(scroll, "ReviveCooldown", cooldown);
		}
	}

	@Override
	public boolean canEquip(SlotContext context, ItemStack stack) {
		return super.canEquip(context, stack) && context.entity() instanceof Player player && SuperpositionHandler.isTheBlessedOne(player);
	}

	// Port: was Curios' getAttributesTooltip override, which cleared the attribute lines.
	@Override
	public boolean hidesAttributeTooltip(ItemStack stack) {
		return true;
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
		Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();

		attributes.put(Attributes.LUCK, new AttributeModifier(
				UUID.fromString("290d5f76-87aa-4f7c-9c1a-9aef2fe25d05"),
				EnigmaticLegacy.MODID+":luck_bonus", 1, AttributeModifier.Operation.ADDITION));

		// ARCANE SCROLL SLOT BONUS
		// Port: CuriosApi.getCuriosHelper() -> Services.ACCESSORIES.
		Services.ACCESSORIES.addSlotModifier(attributes, "scroll",
				UUID.fromString("fb4dfa90-1df4-4e26-86a9-481dcdd830c5"), 1, Operation.ADDITION);

		return attributes;
	}

	@Override
	public void curioTick(SlotContext context, ItemStack stack) {
		if (!context.entity().level().isClientSide)
			if (this.hasCooldown(stack)) {
				this.setCooldown(stack, this.getCooldown(stack) - 1);
			}
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity pEntity, int pSlotId, boolean pIsSelected) {
		if (!level.isClientSide && pEntity instanceof LivingEntity entity) {
			ItemStack scrollStack = SuperpositionHandler.getCurioStack(entity, this);

			if (stack != scrollStack && this.hasCooldown(stack)) {
				this.setCooldown(stack, this.getCooldown(stack) - 1);
			}
		}
	}

	@Override
	public void onEquip(SlotContext context, ItemStack prevStack, ItemStack stack) {
		if (context.entity() instanceof ServerPlayer player && DevotedBelieversHandler.isDevotedBeliever(player)) {
			if (!SuperpositionHandler.hasAdvancement(player, ADVANCEMENT)) {
				SuperpositionHandler.grantAdvancement(player, ADVANCEMENT);
			}
		}
	}

	/**
	 * Port: was a LOWEST-priority Forge {@code ItemTooltipEvent} handler. The client calls it from
	 * Fabric's {@code ItemTooltipCallback}, which also runs after the item's own tooltip lines.
	 * {@code PROXY.getClientPlayer()} became the {@code clientPlayer} parameter.
	 */
	public void onTooltip(ItemStack stack, @Nullable Player clientPlayer, List<Component> tooltip) {
		if (stack.is(this)) {
			if (clientPlayer != null && SuperpositionHandler.isTheBlessedOne(clientPlayer))
				return;

			for (int i = 0; i < tooltip.size(); i++) {
				Component component = tooltip.get(i);
				String text = component.getString();
				tooltip.set(i, Component.literal(SuperpositionHandler.obscureString(text)).setStyle(component.getStyle()));
			}
		}
	}

	/**
	 * Port: was a Forge {@code LivingDeathEvent} handler; called from
	 * {@code EnigmaticEventHandler.onLivingDeath}.
	 *
	 * @return false to cancel the death (upstream: {@code event.setCanceled(true)}).
	 */
	public boolean onLivingDeath(LivingEntity entity) {
		if (entity instanceof Player && !entity.level().isClientSide) {
			Player player = (Player) entity;

			if (SuperpositionHandler.isTheBlessedOne(player)) {
				ItemStack scroll = SuperpositionHandler.getCurioStack(player, this);

				if (scroll != null && !this.hasCooldown(scroll)) {
					player.setHealth(player.getMaxHealth() / 2);

					player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 2));
					player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 1));
					player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1200, 0));
					player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 1));

					this.setCooldown(scroll, deathProtectionCooldown.getValue() * 20);

					Services.NETWORK.sendToNear(new TargetPoint(player.getX(), player.getY(), player.getZ(), 128,
							player.level().dimension()), new PacketCosmicRevive(player.getId(), 0));
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Port: was a Forge {@code LivingHurtEvent} handler. Its bonus only hits players, so it is
	 * called from {@code EnigmaticEventHandler.onPlayerHurt}.
	 *
	 * @return the new damage amount.
	 */
	public float onEntityHurt(Player victimPlayer, DamageSource source, float amount) {
		if (amount >= Float.MAX_VALUE)
			return amount;

		if (source.getDirectEntity() instanceof Player && source.is(DamageTypes.PLAYER_ATTACK)) {
			Player player = (Player) source.getDirectEntity();
			float bonusDamage = 0F;

			if (SuperpositionHandler.hasArchitectsFavor(player)) {
				if (!SuperpositionHandler.isTheBlessedOne(victimPlayer)) {
					bonusDamage += amount * unchosenDamageBonus.getValue().asModifier(false);
					victimPlayer.setSecondsOnFire(4);
				}
			}

			return amount + bonusDamage;
		}

		return amount;
	}

	/** Port: was a Forge {@code PlayerLoggedInEvent} handler; called from {@code EnigmaticEventHandler.onPlayerJoin}. */
	public void onPlayerJoin(ServerPlayer player) {
		if (SuperpositionHandler.isTheBlessedOne(player) && !SuperpositionHandler.hasAdvancement(player,
				new ResourceLocation(EnigmaticLegacy.MODID, "book/accessories/cosmic_scroll_obscure"))) {
			SuperpositionHandler.grantAdvancement(player, new ResourceLocation(EnigmaticLegacy.MODID, "book/accessories/cosmic_scroll_obscure"));
		}
	}

}
