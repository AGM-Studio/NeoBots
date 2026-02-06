package xyz.agmstudio.neobots.menus;

import com.simibubi.create.foundation.gui.widget.ScrollInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.agmstudio.neobots.containers.slotgroups.SlotCreator;
import xyz.agmstudio.neobots.containers.slotgroups.SlotGroup;
import xyz.agmstudio.neobots.gui.Texture;

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
    protected final List<WidgetFactory<AbstractWidget>> widgets = new ArrayList<>();
    protected final List<SlotGroup> slotGroups = new ArrayList<>();
    protected final List<Label> labels = new ArrayList<>();
    protected final Inventory inventory;

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
        addPlayerInventoryTitle(x, y - 12);
        addSlotGroup(inventory, 9, 3, x, y).offset(9).withTextureSize(18, 18).build(this);
        addSlotGroup(inventory, 9, 1, x, y + 58).limit(9).withTextureSize(18, 18).build(this);
    }
    protected void addPlayerInventory(int x, int y, ItemStack lockedStack) {
        addPlayerInventoryTitle(x, y - 12);
        SlotCreator<Slot> creator = SlotCreator.lockedSlotCreator(inventory, lockedStack);
        addSlotGroup(inventory, 9, 3, x, y).offset(9).withSlotCreator(creator).withTextureSize(18, 18).build(this);
        addSlotGroup(inventory, 9, 1, x, y + 58).limit(9).withSlotCreator(creator).withTextureSize(18, 18).build(this);
    }
    protected void addPlayerInventory(int x, int y, int p, int o, int s) {
        addSlotGroup(inventory, 9, 3, x, y).offset(9).pad(p).withTextureSize(s, s).build(this);
        addSlotGroup(inventory, 9, 1, x, y + o + 2 * p + 54).limit(9).pad(p).withTextureSize(s, s).build(this);
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
        this.widgets.add(s -> {
            input.setX(x + s.getGuiLeft());
            input.setY(y + s.getGuiTop());
            return input;
        });
        return input;
    }
    protected Button addButton(String text, Button.OnPress onPress, int x, int y, int w, int h) {
        return addButton(Component.literal(text), onPress, x, y, w, h);
    }
    protected Button addButton(Component text, Button.OnPress onPress, int x, int y, int w, int h) {
        Button button = Button.builder(text, onPress).pos(x, y).size(w, h).build();
        this.widgets.add(s -> {
            button.setX(x + s.getGuiLeft());
            button.setY(y + s.getGuiTop());
            return button;
        });
        return button;
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

    protected static class Label {
        private final Function<Screen<?>, Component> text;
        private final int x;
        private final int y;
        private int color = 0x404040;
        private boolean shadow = false;
        private boolean center = false;

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

        public void render(Screen<?> screen, GuiGraphics g) {
            Font font = screen.getMinecraft().font;
            Component text = this.text.apply(screen);
            int x = center ? (screen.getMenu().getWidth() - font.width(text.getVisualOrderText())) / 2 : this.x;
            g.drawString(font, text, x, y, color, shadow);
        }

    }

    public interface WidgetFactory<T extends AbstractWidget> {
        T create(Screen<?> screen);
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

            menu.widgets.forEach(w -> addRenderableWidget(w.create(this)));
            menu.onInitActions.forEach(c -> c.accept(this));
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

    @OnlyIn(Dist.CLIENT)
    protected boolean sendInventoryClickPacket(int value) {
        MultiPlayerGameMode mode = Minecraft.getInstance().gameMode;
        if (mode == null) return false;
        mode.handleInventoryButtonClick(containerId, value);
        return true;
    }
}