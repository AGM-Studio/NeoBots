package xyz.agmstudio.neobots.containers.slotgroups;

import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.menus.AbstractMenu;

import java.util.ArrayList;
import java.util.List;

public final class SlotGroupHolder {
    private final List<Slot> slots = new ArrayList<>();

    private final int firstIndex;
    private int lastIndexExclusive = -1;

    private int minX = Integer.MAX_VALUE;
    private int minY = Integer.MAX_VALUE;
    private int maxX = Integer.MIN_VALUE;
    private int maxY = Integer.MIN_VALUE;


    public static @NotNull SlotGroupHolder of(AbstractMenu menu, Slot... slots) {
        return of(menu, 16, 16, slots);
    }
    public static @NotNull SlotGroupHolder of(AbstractMenu menu, int sizeX, int sizeY, Slot... slots) {
        SlotGroupHolder holder = new SlotGroupHolder(menu.slots.size());

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (Slot slot : slots) {
            SlotGroup.ADD_SLOT_METHOD.accept(menu, slot);
            holder.slots.add(slot);

            int x = slot.x;
            int y = slot.y;
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x + sizeX);
            maxY = Math.max(maxY, y + sizeY);
        }

        holder.lastIndexExclusive = menu.slots.size();
        holder.minX = minX;
        holder.minY = minY;
        holder.maxX = maxX;
        holder.maxY = maxY;

        menu.registerSlotGroup(holder);
        return holder;
    }


    public SlotGroupHolder(int firstIndex) {
        this.firstIndex = firstIndex;
    }

    void append(AbstractMenu menu, List<Slot> newSlots, int x, int y, int width, int height) {
        lastIndexExclusive = menu.slots.size();
        slots.addAll(newSlots);
        minX = Math.min(minX, x);
        minY = Math.min(minY, y);
        maxX = Math.max(maxX, x + width);
        maxY = Math.max(maxY, y + height);
    }

    // ---------- Queries ----------
    public boolean containsIndex(int index) {
        return index >= firstIndex && index < lastIndexExclusive;
    }
    public int firstIndex() {
        return firstIndex;
    }
    public int lastIndexExclusive() {
        return lastIndexExclusive;
    }
    public List<Slot> slots() {
        return List.copyOf(slots);
    }

    // ---------- Geometry ----------
    public int x() { return minX; }
    public int y() { return minY; }
    public int width() { return maxX - minX; }
    public int height() { return maxY - minY; }
    public int activeWidth() {
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        for (Slot slot : slots) {
            if (!slot.isActive()) continue;
            minX = Math.min(minX, slot.x);
            maxX = Math.max(maxX, slot.x + 16);
        }


        if (minX == Integer.MAX_VALUE) return 0;
        return maxX - minX;
    }

    public int activeHeight() {
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (Slot slot : slots) {
            if (!slot.isActive()) continue;
            minY = Math.min(minY, slot.y);
            maxY = Math.max(maxY, slot.y + 16);
        }

        if (minY == Integer.MAX_VALUE) return 0;
        return maxY - minY;
    }

}