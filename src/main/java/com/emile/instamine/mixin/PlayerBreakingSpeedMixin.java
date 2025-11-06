package com.emile.instamine.mixin;

import com.emile.instamine.config.ConfigManager;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerBreakingSpeedMixin {

    @Inject(method = "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    private void modifyBreakingSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        ItemStack tool = player.getMainHandStack();
        World world = player.getEntityWorld();

        Identifier blockId = Registries.BLOCK.getId(state.getBlock());
        Identifier toolId = Registries.ITEM.getId(tool.getItem());

        if (!ConfigManager.hasHardness(blockId, toolId)) return;

        float configuredHardness = ConfigManager.getHardness(blockId, toolId);
        float vanillaHardness = state.getHardness(world, player.getBlockPos());
        if (vanillaHardness <= 0) return;

        float toolSpeed = tool.getMiningSpeedMultiplier(state);

        float speedFactor = vanillaHardness / configuredHardness;
        float adjustedSpeed = cir.getReturnValue() * speedFactor;

        cir.setReturnValue(adjustedSpeed);
    }
}
