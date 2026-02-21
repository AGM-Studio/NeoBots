package xyz.agmstudio.neobots.gui;

import net.minecraft.client.gui.GuiGraphics;
import xyz.agmstudio.neobots.menus.AbstractMenu;

public interface Drawable {
    void draw(GuiGraphics g, int x, int y);
    default Drawer at(int x, int y) {
        return at(x, y, false);
    }
    default Drawer at(int x, int y, boolean beforeBg) {
        return new Drawer(this, x, y, beforeBg);
    }

    class Drawer {
        private final Drawable drawable;
        private final int x, y;
        private final boolean beforeBg;

        private Drawer(Drawable drawable, int x, int y, boolean beforeBg) {
            this.drawable = drawable;
            this.beforeBg = beforeBg;
            this.x = x;
            this.y = y;
        }
        public void draw(AbstractMenu.Screen<?> s, GuiGraphics g, boolean isBeforeBg) {
            if (beforeBg == isBeforeBg) drawable.draw(g, x + s.getGuiLeft(), y + s.getGuiTop());
        }
    }
}