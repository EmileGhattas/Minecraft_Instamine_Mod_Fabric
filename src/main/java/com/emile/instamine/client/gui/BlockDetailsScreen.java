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
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.stream.Collectors;


import java.util.ArrayList;
import java.util.List;

public final class BlockDetailsScreen extends Screen {
    private final Screen parent;
    private final Block block;

    private static final int ROW_H = 24;
    private static final int START_Y = 60;

    private record Row(Item tool, TextFieldWidget field, ButtonWidget insta, ButtonWidget reset) {}

    private final List<Row> rows = new ArrayList<>();

    public BlockDetailsScreen(Screen parent, Block block) {
        super(Text.literal("InstaMine – Block Details"));
        this.parent = parent;
        this.block = block;
    }

    @Override
    protected void init() {
        this.clearChildren();
        rows.clear();

        Identifier blockId = Registries.BLOCK.getId(block);
        BlockState state = block.getDefaultState();

        // Find tools: any Axe/Shovel/Hoe OR in #pickaxes; must be effective (speed>1)
        List<Item> tools = Registries.ITEM.stream()
                .filter(i ->
                        i instanceof AxeItem ||
                                i instanceof ShovelItem ||
                                i instanceof HoeItem ||
                                new ItemStack(i).isIn(ItemTags.PICKAXES))
                .collect(Collectors.toList());


        int centerX = this.width / 2;
        int y = START_Y;

        for (Item tool : tools) {
            Identifier toolId = Registries.ITEM.getId(tool);

            // Text box with current value or vanilla
            TextFieldWidget field = new TextFieldWidget(this.textRenderer, centerX - 60, y, 60, 20, Text.literal("Hardness"));
            float current = ConfigManager.hasHardness(blockId, toolId)
                    ? ConfigManager.getHardness(blockId, toolId)
                    : block.getHardness();
            field.setText(String.format("%.3f", current));

            field.setChangedListener(s -> {
                try {
                    float v = Float.parseFloat(s);
                    ConfigManager.setHardness(blockId, toolId, v);
                } catch (NumberFormatException ignored) {}
            });

            ButtonWidget insta = ButtonWidget.builder(Text.literal("InstaMine"), b -> {
                float instamine = HardnessCalculator.computeInstamineHardness(new ItemStack(tool), state);
                field.setText(String.format("%.3f", instamine));
                ConfigManager.setHardness(blockId, toolId, instamine);
            }).dimensions(centerX + 10, y, 80, 20).build();

            ButtonWidget reset = ButtonWidget.builder(Text.literal("Default"), b -> {
                ConfigManager.resetHardness(blockId, toolId);
                field.setText(String.format("%.3f", block.getHardness()));
            }).dimensions(centerX + 95, y, 80, 20).build();

            this.addDrawableChild(field);
            this.addDrawableChild(insta);
            this.addDrawableChild(reset);
            rows.add(new Row(tool, field, insta, reset));

            y += ROW_H;
        }

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> {
            MinecraftClient.getInstance().setScreen(parent);
        }).dimensions(this.width / 2 - 75, this.height - 28, 150, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0x88000000);
        Identifier id = Registries.BLOCK.getId(block);
        ctx.drawCenteredTextWithShadow(this.textRenderer,
                "Edit Hardness – " + (id == null ? "<unknown>" : id),
                this.width / 2, 20, 0xFFFFFF);

        int labelX = this.width / 2 - 150;
        int y = START_Y + 6;
        for (Row row : rows) {
            Identifier tid = Registries.ITEM.getId(row.tool());
            ctx.drawTextWithShadow(this.textRenderer, tid == null ? "<item>" : tid.toString(), labelX, y, 0xAAAAAA);
            y += ROW_H;
        }
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
