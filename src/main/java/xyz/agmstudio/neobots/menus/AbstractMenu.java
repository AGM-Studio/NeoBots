package xyz.agmstudio.neobots.menus;

import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import net.createmod.catnip.gui.element.ScreenElement;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.agmstudio.neobots.containers.slotgroups.SlotCreator;
import xyz.agmstudio.neobots.containers.slotgroups.SlotGroup;
import xyz.agmstudio.neobots.containers.slotgroups.SlotGroupHolder;
import xyz.agmstudio.neobots.containers.slots.FilterSlot;
import xyz.agmstudio.neobots.gui.Texture;
import xyz.agmstudio.neobots.network.MenuPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractMenu extends AbstractContainerMenu {
    protected static final Texture SLOT_TEXTURE = new Texture("textures/gui/single_slot.png", 20, 20);
    protected static final Texture ACTIVE_SLOT_TEXTURE = new Texture("textures/gui/single_slot_active.png", 20 , 20);
    protected static final Texture UPGRADE_SLOT_TEXTURE = new Texture("textures/gui/upgrade_slot.png", 20, 20);
    protected static final Texture SIMPLE_FRAME = new Texture("textures/gui/simple_frame.png", 64, 64);

    protected final List<Consumer<Screen<?>>> onInitActions = new ArrayList<>();
    protected final List<Texture.Drawer> drawers = new ArrayList<>();
    protected final List<WidgetHolder<?>> widgets = new ArrayList<>();
    protected final List<SlotGroup> slotGroups = new ArrayList<>();
    protected final List<SlotGroupHolder> slotHolders = new ArrayList<>();
    protected final List<Label> labels = new ArrayList<>();
    protected final Inventory inventory;

    protected SlotGroupHolder playerInventoryGroup = null;

    protected AbstractMenu(@Nullable MenuType<?> type, int id, Inventory inv) {
        super(type, id);
        this.inventory = inv;
    }

    protected SlotGroup addSlotGroup(Container inv, int w, int h, int x, int y) {
        SlotGroup group = new SlotGroup(inv, w, h, x, y);
        this.slotGroups.add(group);
        return group;
    }
    protected void addPlayerInventory(int x, int y) {
        addPlayerInventory(x, y, 0, 4, 18, null);
    }
    protected void addPlayerInventory(int x, int y, ItemStack lockedStack) {
        addPlayerInventory(x, y, 0, 4, 18, SlotCreator.lockedSlotCreator(inventory, lockedStack));
    }
    protected void addPlayerInventory(int x, int y, int slotPadding, int hotbarPadding, int textureSize) {
        addPlayerInventory(x, y, slotPadding, hotbarPadding, textureSize, null);
    }
    protected void addPlayerInventory(int x, int y, int slotPadding, int hotbarPadding, int textureSize, SlotCreator<Slot> creator) {
        if (creator == null) creator = SlotCreator.defaultCreator(inventory);
        playerInventoryGroup = addSlotGroup(inventory, 9, 3, x, y).offset(9).pad(slotPadding).withTextureSize(textureSize, textureSize).withSlotCreator(creator)
                .then(9, 1, x, y + hotbarPadding + 2 * slotPadding + 54).limit(9).build(this);
    }

    protected SlotGroupHolder findGroup(int index) {
        return slotHolders.stream().filter(g -> g.containsIndex(index)).findFirst().orElseThrow();
    }

    protected boolean moveTo(@NotNull SlotGroupHolder group, ItemStack stack, boolean reverse) {
        return moveItemStackTo(stack, group.firstIndex(), group.lastIndexExclusive(), reverse);
    }

    protected Label addLabel(Function<Screen<?>, Component> text, int x, int y) {
        Label label = new Label(text, x, y);
        this.labels.add(label);
        return label;
    }
    protected Label addLabel(Component text, int x, int y) {
        return addLabel(s -> text, x, y);
    }
    protected Label addTitle(int x, int y) {
        return addLabel(Screen::getTitle, x, y);
    }
    protected Label addTitleCentered(int y) {
        return addLabel(Screen::getTitle, getWidth() / 2, y).centered();
    }
    protected Label addPlayerInventoryTitle(int x, int y) {
        return addLabel(Screen::getPlayerInventoryTitle, x, y);
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
            if (widget.widget instanceof IconButton button) button.green = isIconButtonActive(button);
    }
    protected boolean isIconButtonActive(IconButton button) {
        return false;
    }

    protected void addTextureDrawer(Texture.Drawer drawer) {
        this.drawers.add(drawer);
    }
    public void addInitListener(Consumer<Screen<?>> consumer) {
        this.onInitActions.add(consumer);
    }

    protected abstract Texture getBackground();
    protected int getWidth() {
        return getBackground().sizeX;
    }
    protected int getHeight() {
        return getBackground().sizeY;
    }
    public void registerSlotGroup(SlotGroupHolder holder) {
        slotHolders.add(holder);
    }

    protected static class Label {
        private final Function<Screen<?>, Component> text;
        private final int x;
        private final int y;
        private int color = 0x404040;
        private boolean shadow = false;
        private boolean center = false;
        private int maxWidth = -1;
        private float scale = 1.0f;

        protected Label(Function<Screen<?>, Component> text, int x, int y) {
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

        public void render(Screen<?> screen, GuiGraphics g) {
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
        private void draw(Screen<?> screen, GuiGraphics g, FormattedCharSequence text, Font font, int x, int y) {
            int dx = center ? (screen.getMenu().getWidth() - font.width(text)) / 2 : x;
            g.pose().pushPose();
            g.pose().scale(scale, scale, 1.0f);
            g.drawString(font, text, Math.round(dx / scale), Math.round(y / scale), color, shadow);
            g.pose().popPose();
        }
    }
    public static class WidgetHolder<T extends AbstractWidget> {
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
        public T init(Screen<?> screen) {
            widget.setX(x + screen.getGuiLeft());
            widget.setY(y + screen.getGuiTop());
            return widget;
        }
    }
    public static class Screen<T extends AbstractMenu> extends AbstractContainerScreen<T> {
        public Screen(T menu, Inventory inv, Component title) {
            super(menu, inv, title);
        }
        public Component getPlayerInventoryTitle() {
            return playerInventoryTitle;
        }

        @Override protected void init() {
            super.init();

            menu.onInitActions.forEach(c -> c.accept(this));
            for (WidgetHolder<?> widget: menu.widgets) addRenderableWidget(widget.init(this));
        }

        @Override public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            super.render(g, mouseX, mouseY, partialTick);
            this.renderTooltip(g, mouseX, mouseY);
        }

        @Override protected void renderBg(@NotNull GuiGraphics g, float partialTick, int x, int y) {
            menu.drawers.stream().filter(Texture.Drawer::drawBeforeBg).forEach(d -> d.draw(this, g));
            getMenu().getBackground().draw(g, leftPos, topPos);
            menu.drawers.stream().filter(d -> !d.drawBeforeBg()).forEach(d -> d.draw(this, g));

            menu.slotGroups.forEach(group -> group.render(this, g));
        }

        @Override protected void renderLabels(@NotNull GuiGraphics g, int mouseX, int mouseY) {
            menu.labels.forEach(l -> l.render(this, g));
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

    @Override
    public void clicked(int id, int dragType, @NotNull ClickType clickType, @NotNull Player player) {
        if (id >= 0 && id < slots.size()) {
            if (slots.get(id) instanceof FilterSlot filterSlot) {
                ItemStack carried = getCarried();
                if (!carried.isEmpty()) filterSlot.set(carried);
                else filterSlot.set(ItemStack.EMPTY);
                return;
            }
        }

        super.clicked(id, dragType, clickType, player);
    }

    // Packet Handlers
    public void handlePacket(int id, int value) {}
    public void handlePacket(int id, double value) {}
    public void handlePacket(int id, boolean value) {}
    public void handlePacket(int id, String value) {}

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