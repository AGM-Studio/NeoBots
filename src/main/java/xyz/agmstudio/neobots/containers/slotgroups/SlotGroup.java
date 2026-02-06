package xyz.agmstudio.neobots.containers.slotgroups;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.containers.BotFilteredContainer;
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

    protected final List<Slot> slots = new ArrayList<>();

    public SlotGroup(Container container, int w, int h, int x, int y) {
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

    public int indexOf(int i) {
        return i;
    }

    public void build(AbstractMenu menu) {
        this.slots.clear();
        int maxByGrid = offset + w * h;
        int maxByLimit = limit > -1 ? offset + limit : Integer.MAX_VALUE;
        int last = Math.min(Math.min(maxByGrid, maxByLimit), container.getContainerSize());
        for (int i = offset; i < last; i++) {
            int index = i - offset;
            int px = x + (index % w) * (paddingX + textureSizeX);
            int py = y + (index / w) * (paddingY + textureSizeY);
            Slot slot = creator.create(indexOf(i), px, py);
            ADD_SLOT_METHOD.accept(menu, slot);
            this.slots.add(slot);
        }
    }

    public void render(AbstractMenu.Screen<?> screen, GuiGraphics g) {
        if (texture == null) return;
        int maxByGrid = offset + w * h;
        int maxByLimit = limit > -1 ? offset + limit : Integer.MAX_VALUE;
        int maxByActive = (container instanceof BotFilteredContainer bfc) ? bfc.getActiveSlots() : container.getContainerSize();
        int last = Math.min(Math.min(maxByGrid, maxByLimit), maxByActive);
        int x = this.x + screen.getGuiLeft() - textureOffsetX;
        int y = this.y + screen.getGuiTop() - textureOffsetY;
        for (int i = offset; i < last; i++) {
            int index = i - offset;
            Texture texture = this.texture.apply(i);
            if (texture == null) continue;

            int px = x + (index % w) * (paddingX + textureSizeX);
            int py = y + (index / w) * (paddingY + textureSizeY);
            texture.draw(g, px, py);
        }
    }
}