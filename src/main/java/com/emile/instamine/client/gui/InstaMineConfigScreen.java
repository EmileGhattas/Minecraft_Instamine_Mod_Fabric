package com.emile.instamine.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class InstaMineConfigScreen extends Screen {
    private final Screen parent;

    private TextFieldWidget searchField;
    private ButtonWidget allBlocksTab;
    private ButtonWidget activeChangesTab;

    private AllBlocksPanel allBlocksPanel;
    private ActiveChangesPanel activeChangesPanel;

    private boolean showingAllBlocks = true;

    public InstaMineConfigScreen(Screen parent) {
        super(Text.literal("InstaMine Config"));
        this.parent = parent;
    }

    public void registerButton(ButtonWidget button) {
        this.addDrawableChild(button);
    }

    public InstaMineConfigScreen() {
        this(MinecraftClient.getInstance().currentScreen);
    }

    @Override
    protected void init() {
        int cx = this.width / 2;

        // Search
        this.searchField = new TextFieldWidget(this.textRenderer, cx - 100, 30, 200, 20, Text.literal("Search"));
        this.addDrawableChild(this.searchField);

        // Tabs
        this.allBlocksTab = ButtonWidget.builder(Text.literal("All Blocks"), b -> showingAllBlocks = true)
                .dimensions(cx - 155, 30, 70, 20).build();
        this.activeChangesTab = ButtonWidget.builder(Text.literal("Active Changes"), b -> showingAllBlocks = false)
                .dimensions(cx + 85, 30, 90, 20).build();
        this.addDrawableChild(this.allBlocksTab);
        this.addDrawableChild(this.activeChangesTab);

        // Panels (helpers, not widgets)
        int contentTop = 60;
        int contentHeight = this.height - 90;
        this.allBlocksPanel = new AllBlocksPanel(this, this.textRenderer, cx, contentTop, this.width, contentHeight);
        this.activeChangesPanel = new ActiveChangesPanel(this, this.textRenderer, cx, contentTop, this.width, contentHeight);

        // Back
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"),
                        b -> MinecraftClient.getInstance().setScreen(parent))
                .dimensions(this.width / 2 - 50, this.height - 30, 100, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // ✅ Simple static dim background (no blur, no crash)
        ctx.fillGradient(0, 0, this.width, this.height, 0xA0101010, 0xA0101010);

        ctx.drawCenteredTextWithShadow(this.textRenderer, "InstaMine Configuration", this.width / 2, 10, 0xFFFFFF);

        String q = this.searchField.getText();
        if (this.showingAllBlocks) {
            this.allBlocksPanel.setSearchQuery(q);
            this.allBlocksPanel.render(ctx, mouseX, mouseY, delta);
        } else {
            this.activeChangesPanel.setSearchQuery(q);
            this.activeChangesPanel.render(ctx, mouseX, mouseY, delta);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return this.showingAllBlocks
                ? this.allBlocksPanel.mouseScrolled(mouseX, mouseY, verticalAmount)
                : this.activeChangesPanel.mouseScrolled(mouseX, mouseY, verticalAmount);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }
}
