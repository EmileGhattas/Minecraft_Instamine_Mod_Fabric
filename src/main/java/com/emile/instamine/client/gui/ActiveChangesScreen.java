package com.emile.instamine.client.gui;

import com.emile.instamine.config.ConfigManager;
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
import java.util.Map;

public final class ActiveChangesScreen extends Screen {
    private final Screen parent;
    private final List<Identifier> editedBlocks = new ArrayList<>();

    public ActiveChangesScreen(Screen parent) {
        super(Text.literal("InstaMine – Active Changes"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearChildren();
        editedBlocks.clear();

        for (Identifier b : ConfigManager.viewAll().keySet()) {
            editedBlocks.add(b);
        }

        int y = 60;
        for (Identifier id : editedBlocks) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal(id.toString()), b -> {
                Block block = Registries.BLOCK.get(id);
                if (block != null) {
                    MinecraftClient.getInstance().setScreen(new BlockDetailsScreen(this, block));
                }
            }).dimensions(this.width / 2 - 160, y, 320, 20).build());
            y += 22;
        }

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> {
            MinecraftClient.getInstance().setScreen(parent);
        }).dimensions(this.width / 2 - 75, this.height - 28, 150, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, 0x88000000);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "Active Changes", this.width / 2, 20, 0xFFFFFF);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
