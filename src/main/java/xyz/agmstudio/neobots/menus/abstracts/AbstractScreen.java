package xyz.agmstudio.neobots.menus.abstracts;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

public class AbstractScreen<T extends AbstractMenu> extends AbstractContainerScreen<T> {
    public AbstractScreen(T menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageWidth = getMenu().getWidth();
        imageHeight = getMenu().getHeight();
    }

    public Component getPlayerInventoryTitle() {
        return playerInventoryTitle;
    }

    @Override
    protected void init() {
        super.init();

        menu.onInitActions.forEach(c -> c.accept(this));
        for (WidgetHolder<?> widget : menu.widgets) addRenderableWidget(widget.init(this));
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int x, int y) {
        menu.drawers.stream().map(Supplier::get).filter(Objects::nonNull).forEach(d -> d.draw(this, g, true));
        getMenu().getBackground().draw(g, leftPos, topPos);
        menu.drawers.stream().map(Supplier::get).filter(Objects::nonNull).forEach(d -> d.draw(this, g, false));

        menu.slotGroups.forEach(group -> group.render(this, g));
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics g, int mouseX, int mouseY) {
        menu.labels.stream().map(Supplier::get).filter(Objects::nonNull).forEach(l -> l.render(this, g));
    }

    public void offsetTop(int i) {
        this.topPos += i;
    }

    public void offsetLeft(int i) {
        this.leftPos += i;
    }

    public void offset(int t, int l) {
        this.topPos += t;
        this.leftPos += l;
    }
}