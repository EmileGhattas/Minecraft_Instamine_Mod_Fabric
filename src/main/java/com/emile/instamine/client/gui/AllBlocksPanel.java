package com.emile.instamine.client.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

final class AllBlocksPanel {
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

    private final List<Block> filtered = new ArrayList<>();

    AllBlocksPanel(Screen owner, TextRenderer tr, int centerX, int contentTop, int fullWidth, int contentHeight) {
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
        this.scrollVel += -verticalAmount * 28.0; // smooth like modmenu
        this.lastWheelNanos = System.nanoTime();
        return true;
    }

    void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // integrate smooth scroll
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

        int cx = centerX;
        int btnW = Math.min(320, width - 120);
        int left = (width - btnW) / 2;

        for (int i = 0; i < visible && first + i < filtered.size(); i++) {
            Block b = filtered.get(first + i);
            Identifier id = Registries.BLOCK.getId(b);

            ButtonWidget w = ButtonWidget.builder(
                            Text.literal(id.toString()),
                            btn -> MinecraftClient.getInstance().setScreen(new BlockDetailsScreen(owner, b))
                    )
                    .dimensions(left, y, btnW, 20)
                    .build();

            w.render(ctx, mouseX, mouseY, delta);

            // block icon
            ctx.drawItem(b.asItem().getDefaultStack(), left - 24, y + 2);

            y += ROW_H;
        }


        // scrollbar (only while scrolling)
        long msSinceWheel = (System.nanoTime() - lastWheelNanos) / 1_000_000;
        if (msSinceWheel < FADE_MS && filtered.size() > 0) {
            int content = filtered.size() * ROW_H;
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
        this.filtered.clear();
        for (Block b : Registries.BLOCK) {
            Identifier id = Registries.BLOCK.getId(b);
            String s = id.toString();
            if (query.isEmpty() || s.contains(query.toLowerCase())) {
                this.filtered.add(b);
            }
        }
    }

    private void clampScroll() {
        int content = Math.max(0, filtered.size() * ROW_H - height);
        if (scroll < 0) scroll = 0;
        if (scroll > content) scroll = content;
        if (content == 0) scroll = 0;
    }
}
