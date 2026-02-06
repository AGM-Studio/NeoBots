package xyz.agmstudio.neobots.containers.slotgroups;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import xyz.agmstudio.neobots.containers.slots.LockedSlot;

public interface SlotCreator<T extends Slot> {
    T create(int index, int x, int y);

    static SlotCreator<Slot> lockedSlotCreator(Container container, int locked) {
        return (i, x, y) -> {
            if (i == locked) return new LockedSlot(container, i, x, y);
            return new Slot(container, i, x, y);
        };
    }

    static SlotCreator<Slot> lockedSlotCreator(Container container, ItemStack locked) {
        return (i, x, y) -> {
            if (container.getItem(i) == locked) return new LockedSlot(container, i, x, y);
            return new Slot(container, i, x, y);
        };
    }
}