package com.stormpop;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/** The STORM HUD, opened with /storm. */
public class StormHudScreen extends Screen {
    private final Screen parent;
    private final List<ButtonWidget> styleButtons = new ArrayList<>();
    private ButtonWidget toggleButton;
    private ButtonWidget intensityButton;
    private ButtonWidget timeButton;

    public StormHudScreen(Screen parent) {
        super(Text.literal("STORM's pop particles"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        styleButtons.clear();
        int left = this.width / 2 - 155;
        int right = this.width / 2 + 5;
        int top = this.height / 2 - 70;

        // left column: the 7 styles
        StormStyle[] styles = StormStyle.values();
        for (int i = 0; i < styles.length; i++) {
            StormStyle s = styles[i];
            ButtonWidget b = ButtonWidget.builder(styleLabel(s), btn -> {
                StormConfig.style = s;
                StormConfig.save();
                refreshLabels();
            }).dimensions(left, top + i * 22, 150, 20).build();
            styleButtons.add(b);
            this.addDrawableChild(b);
        }

        // right column: controls
        toggleButton = ButtonWidget.builder(Text.empty(), btn -> {
            StormConfig.enabled = !StormConfig.enabled;
            StormConfig.save();
            refreshLabels();
        }).dimensions(right, top, 150, 20).build();
        this.addDrawableChild(toggleButton);

        intensityButton = ButtonWidget.builder(Text.empty(), btn -> {
            StormConfig.intensity = StormConfig.intensity % 3 + 1;
            StormConfig.save();
            refreshLabels();
        }).dimensions(right, top + 22, 150, 20).build();
        this.addDrawableChild(intensityButton);

        // Time: how long the particles stay (1, 2 or 3 seconds)
        timeButton = ButtonWidget.builder(Text.empty(), btn -> {
            StormConfig.lifeSeconds = StormConfig.lifeSeconds % 3 + 1;
            StormConfig.save();
            refreshLabels();
        }).dimensions(right, top + 44, 150, 20).build();
        this.addDrawableChild(timeButton);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn -> close())
                .dimensions(right, top + 6 * 22, 150, 20).build());

        refreshLabels();
    }

    private Text styleLabel(StormStyle s) {
        boolean selected = StormConfig.style == s;
        return Text.literal((selected ? "> " : "") + s.displayName)
                .styled(st -> st.withColor(s == StormStyle.RAINBOW ? 0xFF77FF : s.color));
    }

    private void refreshLabels() {
        StormStyle[] styles = StormStyle.values();
        for (int i = 0; i < styles.length; i++) {
            styleButtons.get(i).setMessage(styleLabel(styles[i]));
        }
        toggleButton.setMessage(Text.literal("Pop effect: " + (StormConfig.enabled ? "ON" : "OFF")));
        String level = switch (StormConfig.intensity) { case 1 -> "Low"; case 3 -> "High"; default -> "Normal"; };
        intensityButton.setMessage(Text.literal("Intensity: " + level));
        timeButton.setMessage(Text.literal("Time: " + StormConfig.lifeSeconds
                + (StormConfig.lifeSeconds == 1 ? " second" : " seconds")));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 100, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Inspired by impact flash-lite by flamesentinell"),
                this.width / 2, this.height / 2 + 100, 0xAAAAAA);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }
}
