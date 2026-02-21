package xyz.agmstudio.neobots.gui;

import net.minecraft.client.gui.GuiGraphics;
import xyz.agmstudio.neobots.menus.AbstractMenu;

import java.util.function.BiConsumer;

public record ScreenDrawer(BiConsumer<AbstractMenu.Screen<?>, GuiGraphics> drawer, boolean drawBeforeBg) {
    public void draw(AbstractMenu.Screen<?> screen, GuiGraphics g) {
        drawer.accept(screen, g);
    }
}