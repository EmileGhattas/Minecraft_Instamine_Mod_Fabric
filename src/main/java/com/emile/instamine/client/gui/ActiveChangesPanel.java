package com.emile.instamine.client.gui;

import com.emile.instamine.config.ConfigManager;
import net.minecraft.block.Block;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class ActiveChangesPanel {
    private final Screen owner;
    private final TextRenderer tr;
    private final int top;
    private final int height;
    private final int width;
    private final int centerX;

    private String query = "";
    private double scroll = 0;
    private double scrollVel = 0;
    private long lastScrollNanos = 0;
    private long lastWheelNanos = 0;

    private static final int ROW_H = 24;
    private static final int FADE_MS = 900;

    private final List<Identifier> blocks = new ArrayList<>();

    ActiveChangesPanel(Screen owner, TextRenderer tr, int centerX, int contentTop, int fullWidth, int contentHeight) {
        this.owner = owner;
        this.tr = tr;
        this.centerX = centerX;
        this.top = contentTop;
        this.height = contentHeight;
        this.width = fullWidth;
        rebuild();
    }

    void setSearchQuery(String q) {
        if (q == null) q = "";
        if (!q.equals(this.query)) {
            this.query = q;
            rebuild();
            clampScroll();
        }
    }

    boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
        this.scrollVel += -verticalAmount * 28.0;
        this.lastWheelNanos = System.nanoTime();
        return true;
    }

    void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        long now = System.nanoTime();
        if (lastScrollNanos == 0) lastScrollNanos = now;
        double dt = (now - lastScrollNanos) / 1_000_000_000.0;
        lastScrollNanos = now;

        double decel = 9.0;
        if (scrollVel > 0) scrollVel = Math.max(0, scrollVel - decel * dt * 60);
        else if (scrollVel < 0) scrollVel = Math.min(0, scrollVel + decel * dt * 60);

        scroll += scrollVel * dt;
        clampScroll();

        int visible = height / ROW_H + 2;
        int first = Math.max(0, (int) (scroll / ROW_H));
        int y = top - ((int) scroll % ROW_H);

        int btnW = Math.min(320, width - 120);
        int left = (width - btnW) / 2;

        for (int i = 0; i < visible && first + i < blocks.size(); i++) {
            Identifier id = blocks.get(first + i);
            Block b = Registries.BLOCK.get(id);

            ButtonWidget w = ButtonWidget.builder(Text.literal(id.toString()),
                            btn -> MinecraftClient.getInstance().setScreen(new BlockDetailsScreen(owner, b)))
                    .dimensions(left, y, btnW, 20)
                    .build();

// Tell the owner to register it safely
            if (owner instanceof InstaMineConfigScreen screen) {
                screen.registerButton(w);
            }
            ctx.drawItem(b.asItem().getDefaultStack(), left - 24, y + 2);

            y += ROW_H;
        }


        long msSinceWheel = (System.nanoTime() - lastWheelNanos) / 1_000_000;
        if (msSinceWheel < FADE_MS && blocks.size() > 0) {
            int content = blocks.size() * ROW_H;
            int trackH = height - 4;
            float visibleFrac = Math.min(1f, (float) height / (float) content);
            int barH = Math.max(24, (int) (trackH * visibleFrac));
            float scrollFrac = (float) (scroll / Math.max(1, content - height));
            int barY = top + 2 + (int) ((trackH - barH) * scrollFrac);
            int x = width - 8;
            int alpha = 0x80 * (FADE_MS - (int) msSinceWheel) / FADE_MS;
            int color = (alpha << 24) | 0xFFFFFF;
            ctx.fill(x, barY, x + 4, barY + barH, color);
        }
    }

    private void rebuild() {
        this.blocks.clear();
        for (Map.Entry<Identifier, Map<Identifier, Float>> e : ConfigManager.viewAll().entrySet()) {
            Identifier id = e.getKey();
            String s = id.toString();
            if (query.isEmpty() || s.contains(query.toLowerCase())) {
                this.blocks.add(id);
            }
        }
    }

    private void clampScroll() {
        int content = Math.max(0, blocks.size() * ROW_H - height);
        if (scroll < 0) scroll = 0;
        if (scroll > content) scroll = content;
        if (content == 0) scroll = 0;
    }
}
