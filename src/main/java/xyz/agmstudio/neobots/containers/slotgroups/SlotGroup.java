package xyz.agmstudio.neobots.containers.slotgroups;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.containers.slots.NeoSlot;
import xyz.agmstudio.neobots.menus.abstracts.AbstractMenu;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

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
    protected int slotSizeX = 18;
    protected int slotSizeY = 18;

    protected List<ClientSlotGroup.FrameBuilder> frames = new ArrayList<>();
    protected List<ClientSlotGroup.LabelBuilder> labels = new ArrayList<>();

    protected final List<NeoSlot> slots = new ArrayList<>();
    protected final List<SlotGroup> children = new ArrayList<>();

    public SlotGroup(Container container, int w, int h, int x, int y) {
        this.creator = SlotCreator.defaultCreator(container);
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

        this.slotSizeX = parent.slotSizeX;
        this.slotSizeY = parent.slotSizeY;
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

    public SlotGroup withSlotSize(int x, int y) {
        this.slotSizeX = x;
        this.slotSizeY = y;
        return this;
    }

    public SlotGroup withSlotCreator(@NotNull SlotCreator<? extends Slot> slotCreator) {
        this.creator = slotCreator;
        return this;
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
        return build(menu, true);
    }
    public SlotGroupHolder build(AbstractMenu menu, boolean isVisible) {
        if (this != root)
            return root.build(menu);

        int startIndex = menu.slots.size();
        SlotGroupHolder holder = new SlotGroupHolder(startIndex);
        buildInto(menu, holder);
        holder.setOffsetIndex(offset);
        holder.setVisible(isVisible);
        menu.registerSlotGroup(holder);
        return holder;
    }
    private void buildInto(@NotNull AbstractMenu menu, SlotGroupHolder holder) {
        slots.clear();
        int maxByGrid = offset + w * h;
        int maxByLimit = limit > -1 ? offset + limit : Integer.MAX_VALUE;
        int last = Math.min(Math.min(maxByGrid, maxByLimit), container.getContainerSize());
        for (int i = offset; i < last; i++) {
            int index = i - offset;
            int px = x + (index % w) * (paddingX + slotSizeX);
            int py = y + (index / w) * (paddingY + slotSizeY);

            NeoSlot slot = creator.create(indexOf(i), px, py);
            ADD_SLOT_METHOD.accept(menu, slot);
            slots.add(slot);
        }

        int width = w * (slotSizeX + paddingX) - paddingX;
        int height = h * (slotSizeY + paddingY) - paddingY;
        holder.append(menu, slots, x, y, width, height);

        for (SlotGroup child : children) child.buildInto(menu, holder);
    }
}