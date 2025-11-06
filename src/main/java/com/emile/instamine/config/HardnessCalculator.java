package com.emile.instamine.config;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;

public class HardnessCalculator {

    /**
     * Calculates the hardness value required for a block to be mined in exactly one tick,
     * assuming Efficiency V and Haste II with the given tool.
     *
     * @param toolStack The tool used to mine the block.
     * @param state The block's default state.
     * @return The required hardness value.
     */
    public static float computeInstamineHardness(ItemStack toolStack, BlockState state) {
        // Vanilla mining formula: Break time (ticks) = hardness * 30 / miningSpeed
        // To make it 1 tick => hardness = miningSpeed / 30

        float baseSpeed = toolStack.getMiningSpeedMultiplier(state);
        int effLevel = 5;   // Efficiency V
        int hasteLevel = 2; // Haste II

        // Efficiency adds (effLevel^2 + 1)
        float effBonus = effLevel * effLevel + 1.0f;

        // Haste adds +20% per level
        float hasteMultiplier = 1.0f + (0.2f * hasteLevel);

        // Final mining speed
        float totalSpeed = (baseSpeed + effBonus) * hasteMultiplier;

        // To achieve 1 tick mining, we invert the vanilla mining formula
        return totalSpeed / 30.0f;
    }
}
