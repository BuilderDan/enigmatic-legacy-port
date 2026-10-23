package com.aizistral.enigmaticlegacy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.ItemStack;

/**
 * Port: Forge's access transformer makes {@code PiglinAi.wasHurtBy}, {@code getBarterResponseItems}
 * and {@code throwItems} public; Fabric reaches them through these invokers instead.
 * {@code isBarterCurrency} stands in for Forge's {@code ItemStack.isPiglinCurrency}.
 */
@Mixin(PiglinAi.class)
public interface AccessorPiglinAi {
	@Invoker("wasHurtBy")
	static void enigmaticlegacy$wasHurtBy(Piglin piglin, LivingEntity entity) {
		throw new AssertionError();
	}

	@Invoker("getBarterResponseItems")
	static List<ItemStack> enigmaticlegacy$getBarterResponseItems(Piglin piglin) {
		throw new AssertionError();
	}

	@Invoker("throwItems")
	static void enigmaticlegacy$throwItems(Piglin piglin, List<ItemStack> stacks) {
		throw new AssertionError();
	}

	@Invoker("isBarterCurrency")
	static boolean enigmaticlegacy$isBarterCurrency(ItemStack stack) {
		throw new AssertionError();
	}
}
