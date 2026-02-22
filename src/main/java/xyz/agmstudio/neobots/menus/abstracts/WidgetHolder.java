package xyz.agmstudio.neobots.menus.abstracts;

import net.minecraft.client.gui.components.AbstractWidget;

public class WidgetHolder<T extends AbstractWidget> {
    private final T widget;
    public final int x;
    public final int y;

    public WidgetHolder(T widget, int x, int y) {
        this.widget = widget;
        this.x = x;
        this.y = y;
    }

    public T get() {
        return widget;
    }

    public T init(AbstractScreen<?> screen) {
        widget.setX(x + screen.getGuiLeft());
        widget.setY(y + screen.getGuiTop());
        return widget;
    }
}