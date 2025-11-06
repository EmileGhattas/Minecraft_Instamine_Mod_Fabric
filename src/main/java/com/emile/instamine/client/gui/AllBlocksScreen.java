package com.emile.instamine.client.gui;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class AllBlocksScreen extends Screen {
    private static final int PER_PAGE = 12;
    private final Screen parent;
    private final List<Block> blocks = new ArrayList<>();
    private int page = 0;

    public AllBlocksScreen(Screen parent) {
        super(Text.literal("InstaMine – All Blocks"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        blocks.clear();
        Registries.BLOCK.stream().forEach(blocks::add);
        drawPage();
    }

    private void drawPage() {
        this.clearChildren();

        int start = page * PER_PAGE;
        int end = Math.min(start + PER_PAGE, blocks.size());
        int y = 60;

        for (int i = start; i < end; i++) {
            Block b = blocks.get(i);
            Identifier id = Registries.BLOCK.getId(b);
            this.addDrawableChild(
                    ButtonWidget.builder(Text.literal(id == null ? "<unknown>" : id.toString()), btn -> {
                        MinecraftClient.getInstance().setScreen(new BlockDetailsScreen(this, b));
                    }).dimensions(this.width / 2 - 160, y, 320, 20).build()
            );
            y += 22;
        }

        // Prev / Next
        if (page > 0) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("< Prev"), b -> {
                page--; drawPage();
            }).dimensions(this.width / 2 - 160, this.height - 52, 70, 20).build());
        }
        if (end < blocks.size()) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Next >"), b -> {
                page++; drawPage();
            }).dimensions(this.width / 2 + 90, this.height - 52, 70, 20).build());
        }

        // Back
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> {
            MinecraftClient.getInstance().setScreen(parent);
        }).dimensions(this.width / 2 - 75, this.height - 28, 150, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0x88000000);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "All Blocks", this.width / 2, 20, 0xFFFFFF);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
