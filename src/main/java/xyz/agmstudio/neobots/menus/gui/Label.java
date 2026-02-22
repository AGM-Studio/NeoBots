package xyz.agmstudio.neobots.menus.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import xyz.agmstudio.neobots.menus.abstracts.AbstractScreen;

import java.util.List;
import java.util.function.Function;

public class Label {
    private final Function<AbstractScreen<?>, Component> text;
    private final int x;
    private final int y;
    private int color = 0x404040;
    private boolean shadow = false;
    private boolean center = false;
    private int maxWidth = -1;
    private float scale = 1.0f;

    public Label(Function<AbstractScreen<?>, Component> text, int x, int y) {
        this.text = text;
        this.x = x;
        this.y = y;
    }

    public Label centered() {
        this.center = true;
        return this;
    }

    public Label withColor(int color) {
        this.color = color;
        return this;
    }

    public Label withShadow() {
        this.shadow = true;
        return this;
    }

    public Label width(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public Label scale(float scale) {
        this.scale = scale;
        return this;
    }

    public void render(AbstractScreen<?> screen, GuiGraphics g) {
        Font font = screen.getMinecraft().font;
        Component text = this.text.apply(screen);
        if (maxWidth > 0) {
            List<FormattedCharSequence> lines = font.split(text, (int) (maxWidth / scale));
            for (int i = 0; i < lines.size(); i++) {
                int yOffset = y + i * (font.lineHeight + 1);
                draw(screen, g, lines.get(i), font, x, yOffset);
            }
        } else draw(screen, g, text.getVisualOrderText(), font, x, y);
    }

    private void draw(AbstractScreen<?> screen, GuiGraphics g, FormattedCharSequence text, Font font, int x, int y) {
        int dx = center ? (screen.getMenu().getWidth() - font.width(text)) / 2 : x;
        g.pose().pushPose();
        g.pose().scale(scale, scale, 1.0f);
        g.drawString(font, text, Math.round(dx / scale), Math.round(y / scale), color, shadow);
        g.pose().popPose();
    }
}