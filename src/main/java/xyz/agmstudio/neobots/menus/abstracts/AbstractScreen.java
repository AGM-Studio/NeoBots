package xyz.agmstudio.neobots.menus.abstracts;

import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import net.createmod.catnip.gui.element.ScreenElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.containers.slotgroups.ClientSlotGroup;
import xyz.agmstudio.neobots.menus.gui.Drawable;
import xyz.agmstudio.neobots.menus.gui.Label;
import xyz.agmstudio.neobots.menus.gui.Texture;
import xyz.agmstudio.neobots.network.MenuPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractScreen<T extends AbstractMenu> extends AbstractContainerScreen<T> {
    protected final List<Consumer<AbstractScreen<?>>> onInitActions = new ArrayList<>();
    protected final List<Supplier<Drawable.Drawer>> drawers = new ArrayList<>();
    protected final List<WidgetHolder<?>> widgets = new ArrayList<>();
    protected final List<Supplier<Label>> labels = new ArrayList<>();
    protected final List<ClientSlotGroup> groups = new ArrayList<>();

    public AbstractScreen(T menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageWidth = getWidth();
        imageHeight = getHeight();
    }

    public Component getPlayerInventoryTitle() {
        return playerInventoryTitle;
    }

    @Override
    protected void init() {
        super.init();

        onInitActions.forEach(c -> c.accept(this));
        for (WidgetHolder<?> widget : widgets) addRenderableWidget(widget.init(this));
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int x, int y) {
        drawers.stream().map(Supplier::get).filter(Objects::nonNull).forEach(d -> d.draw(this, g, true));
        groups.forEach(group -> group.renderBg(this, g, true));
        getBackground().draw(g, leftPos, topPos);
        drawers.stream().map(Supplier::get).filter(Objects::nonNull).forEach(d -> d.draw(this, g, false));
        groups.forEach(group -> group.renderBg(this, g, false));

        groups.forEach(group -> group.render(this, g));
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics g, int mouseX, int mouseY) {
        labels.stream().map(Supplier::get).filter(Objects::nonNull).forEach(l -> l.render(this, g));
        groups.forEach(group -> group.renderLabels(this, g));
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

    protected Label addLabel(Function<AbstractScreen<?>, Component> text, int x, int y) {
        Label label = new Label(text, x, y);
        this.labels.add(() -> label);
        return label;
    }
    protected Label addLabel(Component text, int x, int y) {
        return addLabel(s -> text, x, y);
    }
    public void addLabel(Supplier<Label> label) {
        this.labels.add(label);
    }
    protected Label addTitle(int x, int y) {
        return addLabel(AbstractScreen::getTitle, x, y);
    }
    protected Label addTitleCentered(int y) {
        return addLabel(AbstractScreen::getTitle, getWidth() / 2, y).centered();
    }
    protected Label addPlayerInventoryTitle(int x, int y) {
        return addLabel(AbstractScreen::getPlayerInventoryTitle, x, y);
    }

    protected ScrollInput addScrollInput(int x, int y, int w, int h) {
        ScrollInput input = new ScrollInput(x, y, w, h);
        this.widgets.add(new WidgetHolder<>(input, x, y));
        return input;
    }
    protected Button addButton(String text, Button.OnPress onPress, int x, int y, int w, int h) {
        return addButton(Component.translatable(text), onPress, x, y, w, h);
    }
    protected Button addButton(Component text, Button.OnPress onPress, int x, int y, int w, int h) {
        Button button = Button.builder(text, onPress).pos(x, y).size(w, h).build();
        this.widgets.add(new  WidgetHolder<>(button, x, y));
        return button;
    }

    protected IconButton addIconButton(int x, int y, ScreenElement element) {
        IconButton button = new IconButton(x, y, element);
        this.widgets.add(new WidgetHolder<>(button, x, y));
        return button;
    }
    protected void updateIconButtons() {
        for (WidgetHolder<?> widget: widgets)
            if (widget.get() instanceof IconButton button) button.green = isIconButtonActive(button);
    }
    protected boolean isIconButtonActive(IconButton button) {
        return false;
    }

    public void addTextureDrawer(Supplier<Drawable.Drawer> drawer) {
        this.drawers.add(drawer);
    }
    public void addTextureDrawer(Drawable.Drawer drawer) {
        this.drawers.add(() -> drawer);
    }
    public void addInitListener(Consumer<AbstractScreen<?>> consumer) {
        this.onInitActions.add(consumer);
    }

    protected abstract Texture getBackground();
    public int getWidth() {
        return getBackground().sizeX;
    }
    public int getHeight() {
        return getBackground().sizeY;
    }

    public void registerSlotGroup(ClientSlotGroup holder) {
        groups.add(holder);
    }

    // ---- Packets
    protected void sendPacket(int id, int value) {
        PacketDistributor.sendToServer(new MenuPacket.IntegerPayload(id, value));
    }
    protected void sendPacket(int id, double value) {
        PacketDistributor.sendToServer(new MenuPacket.DoublePayload(id, value));
    }
    protected void sendPacket(int id, boolean value) {
        PacketDistributor.sendToServer(new MenuPacket.BooleanPayload(id, value));
    }
    protected void sendPacket(int id, String value) {
        PacketDistributor.sendToServer(new MenuPacket.StringPayload(id, value));
    }
}