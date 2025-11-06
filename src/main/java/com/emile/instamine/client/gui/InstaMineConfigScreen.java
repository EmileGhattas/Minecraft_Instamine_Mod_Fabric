package com.emile.instamine.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class InstaMineConfigScreen extends Screen {
    private final Screen parent;

    public InstaMineConfigScreen(Screen parent) {
        super(Text.literal("InstaMine – Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 2 - 10;

        // All Blocks
        this.addDrawableChild(ButtonWidget.builder(Text.literal("All Blocks"), b -> {
            MinecraftClient.getInstance().setScreen(new AllBlocksScreen(this));
        }).dimensions(centerX - 160, y - 20, 150, 20).build());

        // Active Changes
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Active Changes"), b -> {
            MinecraftClient.getInstance().setScreen(new ActiveChangesScreen(this));
        }).dimensions(centerX + 10, y - 20, 150, 20).build());

        // Back
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> {
            MinecraftClient.getInstance().setScreen(parent);
        }).dimensions(centerX - 75, this.height - 28, 150, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // draw a plain background instead of blur
        ctx.fill(0, 0, this.width, this.height, 0x88000000);
        super.render(ctx, mouseX, mouseY, delta);
    }


    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
