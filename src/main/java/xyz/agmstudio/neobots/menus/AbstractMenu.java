package xyz.agmstudio.neobots.menus;

import com.simibubi.create.foundation.gui.widget.ScrollInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.containers.BotFilteredContainer;
import xyz.agmstudio.neobots.containers.slots.LockedSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractMenu extends AbstractContainerMenu {
    protected static final ResourceLocation SLOT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NeoBots.MOD_ID, "textures/gui/single_slot.png");
    protected static final ResourceLocation ACTIVE_SLOT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NeoBots.MOD_ID, "textures/gui/single_slot_active.png");
    protected static final ResourceLocation UPGRADE_SLOT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NeoBots.MOD_ID, "textures/gui/upgrade_slot.png");

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
        addSlotGroup(inventory, 9, 3, x, y).offset(9).build(this);
        addSlotGroup(inventory, 9, 1, x, y + 58).limit(9).build(this);
    }
    protected void addPlayerInventory(int x, int y, ItemStack lockedStack) {
        addPlayerInventoryTitle(x, y - 12);
        SlotCreator creator = SlotCreator.lockedSlotCreator(inventory, lockedStack);
        addSlotGroup(inventory, 9, 3, x, y).offset(9).withSlotCreator(creator).build(this);
        addSlotGroup(inventory, 9, 1, x, y + 58).limit(9).withSlotCreator(creator).build(this);
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

    protected abstract ResourceLocation getBackground();
    protected abstract int getWidth();
    protected abstract int getHeight();

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
    protected static class SlotGroup {
        private Function<Integer, ResourceLocation> texture = null;
        private int textureX = 18;
        private int textureY = 18;

        private SlotCreator creator;
        private final Container container;
        private final int w;
        private final int h;
        private final int x;
        private final int y;
        private int limit    = -1;
        private int offset   = 0;
        private int paddingX = 0;
        private int paddingY = 0;

        protected SlotGroup(Container container, int w, int h, int x, int y) {
            if (container instanceof BotFilteredContainer bfc) this.creator = bfc.slotBuilder();
            else this.creator = (i, px, py) -> new Slot(container, i, px, py);

            this.container = container;
            this.w = w;
            this.h = h;
            this.x = x;
            this.y = y;
        }
        public SlotGroup offset(int offset) {
            this.offset = offset;
            return this;
        }
        public SlotGroup limit(int limit) {
            this.limit = limit;
            return this;
        }
        public SlotGroup pad(int pad) {
            this.paddingX = pad;
            this.paddingY = pad;
            return this;
        }
        public SlotGroup pad(int padX, int padY) {
            this.paddingX = padX;
            this.paddingY = padY;
            return this;
        }
        public SlotGroup withTexture(Function<Integer, ResourceLocation> provider) {
            this.texture = provider;
            return this;
        }
        public SlotGroup withTexture(Function<Integer, ResourceLocation> provider, int textureX, int textureY) {
            this.texture = provider;
            this.textureX = textureX;
            this.textureY = textureY;
            return this;
        }
        public SlotGroup withTexture(ResourceLocation texture) {
            return withTexture(i ->  texture);
        }
        public SlotGroup withTexture(ResourceLocation texture, int textureX, int textureY) {
            return withTexture(i ->  texture, textureX, textureY);
        }
        public SlotGroup withSlotCreator(@NotNull SlotCreator slotCreator) {
            this.creator = slotCreator;
            return this;
        }

        public void build(AbstractMenu menu) {
            int maxByGrid = offset + w * h;
            int maxByLimit = limit > -1 ? offset + limit : Integer.MAX_VALUE;
            int last = Math.min(Math.min(maxByGrid, maxByLimit), container.getContainerSize());
            int padX = paddingX + textureX;
            int padY = paddingY + textureY;
            for (int i = offset; i < last; i++) {
                int index = i - offset;
                int px = x + (index % w) * padX;
                int py = y + (index / w) * padY;
                menu.addSlot(creator.create(i, px, py));
            }
        }
        public void render(Screen<?> screen, GuiGraphics g) {
            if (texture == null) return;
            int maxByGrid = offset + w * h;
            int maxByLimit = limit > -1 ? offset + limit : Integer.MAX_VALUE;
            int maxByActive = (container instanceof  BotFilteredContainer bfc) ? bfc.getActiveSlots() : container.getContainerSize();
            int last = Math.min(Math.min(maxByGrid, maxByLimit), maxByActive);
            int x = this.x + screen.getGuiLeft() - 1;
            int y = this.y + screen.getGuiTop() - 1;
            int padX = paddingX + textureX;
            int padY = paddingY + textureY;
            for (int i = offset; i < last; i++) {
                int index = i - offset;
                int px = x + (index % w) * padX;
                int py = y + (index / w) * padY;
                g.blit(texture.apply(i), px, py, 0, 0, textureX, textureY, textureX, textureY);
            }
        }
    }

    public interface SlotCreator {
        Slot create(int index, int x, int y);

        static SlotCreator lockedSlotCreator(Container container, int locked) {
            return (i, x, y) -> {
                if (i == locked) return new LockedSlot(container, i, x, y);
                return new Slot(container, i, x, y);
            };
        }

        static SlotCreator lockedSlotCreator(Container container, ItemStack locked) {
            return (i, x, y) -> {
                if (container.getItem(i) == locked) return new LockedSlot(container, i, x, y);
                return new Slot(container, i, x, y);
            };
        }
    }
    public interface WidgetFactory<T extends AbstractWidget> {
        T create(Screen<?> screen);
    }

    public static class Screen<T extends AbstractMenu> extends AbstractContainerScreen<T> {
        public Screen(T menu, Inventory inv, Component title) {
            super(menu, inv, title);
            this.imageWidth = menu.getWidth();
            this.imageHeight = menu.getHeight();
        }
        public Component getPlayerInventoryTitle() {
            return playerInventoryTitle;
        }

        @Override protected void init() {
            super.init();

            menu.widgets.forEach(w -> addRenderableWidget(w.create(this)));
        }

        @Override protected void renderBg(@NotNull GuiGraphics g, float partialTick, int x, int y) {
            g.blit(getMenu().getBackground(), leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

            menu.slotGroups.forEach(group -> group.render(this, g));
        }

        @Override protected void renderLabels(@NotNull GuiGraphics g, int mouseX, int mouseY) {
            menu.labels.forEach(l -> l.render(this, g));
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