package xyz.agmstudio.neobots.containers.slotgroups;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.containers.BotFilteredContainer;
import xyz.agmstudio.neobots.gui.FrameTexture;
import xyz.agmstudio.neobots.gui.ScreenDrawer;
import xyz.agmstudio.neobots.gui.Texture;
import xyz.agmstudio.neobots.menus.AbstractMenu;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class SlotGroup {
    protected static final BiConsumer<AbstractMenu, Slot> ADD_SLOT_METHOD = captureAddSlotMethod();
    private static @NotNull BiConsumer<AbstractMenu, Slot> captureAddSlotMethod() {
        try {
            Method method = AbstractContainerMenu.class.getDeclaredMethod("addSlot", Slot.class);
            method.setAccessible(true);
            return (menu, slot) -> {
                try {
                    method.invoke(menu, slot);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            };
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    protected Function<Integer, Texture> texture = null;
    protected int textureSizeX = 20;
    protected int textureSizeY = 20;
    protected int textureOffsetX = 1;
    protected int textureOffsetY = 1;

    private final SlotGroup root;
    protected SlotCreator<? extends Slot> creator;
    protected final Container container;
    public final int w;
    public final int h;
    public final int x;
    public final int y;
    protected int limit = -1;
    protected int offset = 0;
    protected int paddingX = 0;
    protected int paddingY = 0;

    protected FrameBuilder frame;

    protected final List<Slot> slots = new ArrayList<>();
    protected final List<SlotGroup> children = new ArrayList<>();

    public SlotGroup(Container container, int w, int h, int x, int y) {
        if (container instanceof BotFilteredContainer bfc) this.creator = bfc.slotBuilder();
        else this.creator = (i, px, py) -> new Slot(container, i, px, py);

        this.root = this;
        this.container = container;
        this.w = w;
        this.h = h;
        this.x = x;
        this.y = y;
    }
    private SlotGroup(SlotGroup parent, int w, int h, int x, int y) {
        this.container = parent.container;
        this.creator = parent.creator;
        this.root = parent.root;
        this.w = w;
        this.h = h;
        this.x = x;
        this.y = y;
        // Inherit important stuff
        this.texture = parent.texture;
        this.textureSizeX = parent.textureSizeX;
        this.textureSizeY = parent.textureSizeY;
        this.textureOffsetX = parent.textureOffsetX;
        this.textureOffsetY = parent.textureOffsetY;
        this.paddingX = parent.paddingX;
        this.paddingY = parent.paddingY;
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

    public SlotGroup withTexture(Function<Integer, Texture> provider) {
        this.texture = provider;
        return this;
    }
    public SlotGroup withTexture(Texture texture) {
        return withTexture(i -> texture);
    }
    public SlotGroup withTextureSize(int x, int y) {
        this.textureSizeX = x;
        this.textureSizeY = y;
        return this;
    }

    public SlotGroup withTextureOffset(int x, int y) {
        this.textureOffsetX = x;
        this.textureOffsetY = y;
        return this;
    }

    public SlotGroup withSlotCreator(@NotNull SlotCreator<? extends Slot> slotCreator) {
        this.creator = slotCreator;
        return this;
    }

    public FrameBuilder withFrame(FrameTexture frame) {
        this.frame = new FrameBuilder(this, frame);
        return this.frame;
    }

    public SlotGroup then(int w, int h, int x, int y) {
        SlotGroup child = new SlotGroup(this, w, h, x, y);
        this.children.add(child);
        return child;
    }

    public int indexOf(int i) {
        return i;
    }

    public SlotGroupHolder build(AbstractMenu menu) {
        if (this != root)
            return root.build(menu);

        int startIndex = menu.slots.size();
        SlotGroupHolder holder = new SlotGroupHolder(startIndex);
        buildInto(menu, holder);
        menu.registerSlotGroup(holder);
        if (frame != null) menu.addTextureDrawer(frame.getDrawer(x, y, holder));
        return holder;
    }
    private void buildInto(@NotNull AbstractMenu menu, SlotGroupHolder holder) {
        slots.clear();
        int maxByGrid = offset + w * h;
        int maxByLimit = limit > -1 ? offset + limit : Integer.MAX_VALUE;
        int last = Math.min(Math.min(maxByGrid, maxByLimit), container.getContainerSize());
        for (int i = offset; i < last; i++) {
            int index = i - offset;
            int px = x + (index % w) * (paddingX + textureSizeX);
            int py = y + (index / w) * (paddingY + textureSizeY);

            Slot slot = creator.create(indexOf(i), px, py);
            ADD_SLOT_METHOD.accept(menu, slot);
            slots.add(slot);
        }

        int width = w * (textureSizeX + paddingX) - paddingX;
        int height = h * (textureSizeY + paddingY) - paddingY;
        holder.append(menu, slots, x, y, width, height);

        for (SlotGroup child : children) child.buildInto(menu, holder);
    }

    public void render(AbstractMenu.Screen<?> screen, GuiGraphics g) {
        if (texture == null) return;
        int offX = screen.getGuiLeft() - textureOffsetX;
        int offY = screen.getGuiTop() - textureOffsetY;
        for (Slot slot: slots) {
            if (!slot.isActive()) continue;
            Texture t = this.texture.apply(slot.index - offset);
            if (t == null) continue;
            t.drawScaled(g, slot.x + offX, slot.y + offY, textureSizeX, textureSizeY);
        }
    }

    public static class FrameBuilder {
        private final SlotGroup group;
        private final FrameTexture texture;
        private int offsetX = 0;
        private int offsetY = 0;
        private int offsetW = 0;
        private int offsetH = 0;
        private int width = 0;
        private int height = 0;
        private boolean drawBeforeBg = false;
        private boolean tiled = false;

        private FrameBuilder(SlotGroup group, FrameTexture texture) {
            this.group = group;
            this.texture = texture;
        }
        public FrameBuilder offset(int offsetX, int offsetY) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            return this;
        }
        public FrameBuilder offsetSize(int offsetW, int offsetH) {
            this.offsetW = offsetW;
            this.offsetH = offsetH;
            return this;
        }
        public FrameBuilder minWidth(int width) {
            this.width = width;
            return this;
        }
        public FrameBuilder minHeight(int height) {
            this.height = height;
            return this;
        }
        public FrameBuilder minSize(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }
        public FrameBuilder tiled() {
            this.tiled = true;
            return this;
        }
        public FrameBuilder drawBeforeBg() {
            this.drawBeforeBg = true;
            return this;
        }
        public SlotGroup build() {
            return group;
        }
        public SlotGroupHolder build(AbstractMenu menu) {
            return this.group.build(menu);
        }

        public ScreenDrawer getDrawer(int x, int y, SlotGroupHolder holder) {
            return texture.drawerAround(
                    x - offsetX - group.textureOffsetX,
                    y - offsetY - group.textureOffsetY,
                    Math.max(holder.activeWidth() + group.textureSizeX - 16 + offsetX + offsetW, width),
                    Math.max(holder.activeHeight() + group.textureSizeY - 16 + offsetY + offsetH, height),
                    tiled, drawBeforeBg
            );
        }
    }
}