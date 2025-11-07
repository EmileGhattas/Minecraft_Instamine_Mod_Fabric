package com.emile.instamine.client.gui;

import com.emile.instamine.config.ConfigManager;
import com.emile.instamine.config.HardnessCalculator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class BlockDetailsScreen extends Screen {
    private static final int ROW_H = 24;
    private static final int START_Y = 64;

    private final Screen parent;
    private final Block block;
    private final List<Row> rows = new ArrayList<>();

    private record Row(Item tool, TextFieldWidget field, ButtonWidget insta, ButtonWidget reset) {}

    public BlockDetailsScreen(Screen parent, Block block) {
        super(Text.literal("InstaMine – Block Details"));
        this.parent = parent;
        this.block = block;
    }

    @Override
    protected void init() {
        this.clearChildren();
        this.rows.clear();

        Identifier blockId = Registries.BLOCK.getId(block);
        BlockState state = block.getDefaultState();

        List<Item> tools = Registries.ITEM.stream()
                .filter(i ->
                        i instanceof AxeItem ||
                                i instanceof ShovelItem ||
                                i instanceof HoeItem ||
                                new ItemStack(i).isIn(ItemTags.PICKAXES))
                .toList();

        int cx = this.width / 2;
        int y = START_Y;

        for (Item tool : tools) {
            Identifier toolId = Registries.ITEM.getId(tool);

            TextFieldWidget field = new TextFieldWidget(this.textRenderer, cx - 60, y, 60, 20, Text.literal("Hardness"));
            float current = ConfigManager.hasHardness(blockId, toolId)
                    ? ConfigManager.getHardness(blockId, toolId)
                    : block.getHardness();
            field.setText(String.format("%.3f", current));

            field.setChangedListener(s -> {
                try {
                    float v = Float.parseFloat(s);
                    ConfigManager.setCustomHardness(blockId, toolId, v);
                } catch (NumberFormatException ignored) {}
            });

            ButtonWidget insta = ButtonWidget.builder(Text.literal("InstaMine"), b -> {
                float speed = new ItemStack(tool).getMiningSpeedMultiplier(state);
                // your HardnessCalculator uses the Eff5/Haste2 policy internally
                float target = HardnessCalculator.computeInstamineHardness(speed, 5,2);
                field.setText(String.format("%.3f", target));
                ConfigManager.setCustomHardness(blockId, toolId, target);
            }).dimensions(cx + 10, y, 80, 20).build();

            ButtonWidget reset = ButtonWidget.builder(Text.literal("Default"), b -> {
                ConfigManager.resetHardness(blockId, toolId); // removes entry
                field.setText(String.format("%.3f", block.getHardness()));
            }).dimensions(cx + 95, y, 80, 20).build();

            this.addDrawableChild(field);
            this.addDrawableChild(insta);
            this.addDrawableChild(reset);
            this.rows.add(new Row(tool, field, insta, reset));

            y += ROW_H;
        }

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"),
                        b -> MinecraftClient.getInstance().setScreen(parent))
                .dimensions(this.width / 2 - 50, this.height - 30, 100, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // ✅ Replace blur with static dim background
        ctx.fillGradient(0, 0, this.width, this.height, 0xA0101010, 0xA0101010);

        // your title or block info
        ctx.drawCenteredTextWithShadow(this.textRenderer,
                "Block Details: " + this.block.getName().getString(),
                this.width / 2, 10, 0xFFFFFF);

        // rest of your rendering logic (text, buttons, etc.)
        super.render(ctx, mouseX, mouseY, delta);
    }


    @Override
    public void close() { this.client.setScreen(parent); }
}
