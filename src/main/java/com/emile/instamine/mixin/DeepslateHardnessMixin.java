package com.emile.instamine.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackSpeedMixin {

	@Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
	private void instamine$boostDeepslateSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
		ItemStack stack = (ItemStack)(Object)this;

		if (stack.is(Items.NETHERITE_PICKAXE) && state.is(Blocks.DEEPSLATE)) {
			float vanillaSpeed = cir.getReturnValue();
			float boostedSpeed = vanillaSpeed * 3.0F; // triple the mining speed
			cir.setReturnValue(boostedSpeed);
		}
	}
}
