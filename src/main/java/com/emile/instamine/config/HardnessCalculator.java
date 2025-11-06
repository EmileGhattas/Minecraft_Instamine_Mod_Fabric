package com.emile.instamine.config;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public final class HardnessCalculator {

    // Calculates the hardness for 1-tick instant mining with Efficiency V + Haste II
    public static float computeInstamineHardness(float baseToolSpeed, int effLevel, int hasteLevel) {
        float effBonus = effLevel > 0 ? effLevel * effLevel + 1 : 0;
        float hasteBonus = 1.0F + 0.2F * hasteLevel;

        float totalSpeed = (baseToolSpeed + effBonus) * hasteBonus;

        // Minecraft formula for dig progress per tick = totalSpeed / hardness / 30
        // To break instantly (1 tick), hardness = totalSpeed / 30
        return totalSpeed / 30.0F;
    }

    // Generic version that works for any tool (pickaxe, axe, shovel, hoe, etc.)
    public static float computeInstamineHardness(BlockState state, Item toolItem) {
        ItemStack stack = new ItemStack(toolItem);
        float baseSpeed = stack.getMiningSpeedMultiplier(state);

        // Use default Eff V + Haste II
        return computeInstamineHardness(baseSpeed, 5, 2);
    }

    // Optional helper to get a tool name for logs or GUI
    public static String getToolName(Item tool) {
        Identifier id = Registries.ITEM.getId(tool);
        return id != null ? id.toString() : "<unknown_tool>";
    }
}
