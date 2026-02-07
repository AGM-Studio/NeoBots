package xyz.agmstudio.neobots.containers.slotgroups;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.containers.slots.LockedSlot;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface SlotCreator<T extends Slot> {
    T create(int index, int x, int y);

    @Contract(pure = true)
    static @NotNull SlotCreator<Slot> lockedSlotCreator(Container container, int locked) {
        return (i, x, y) -> {
            if (i == locked) return new LockedSlot(container, i, x, y);
            return new Slot(container, i, x, y);
        };
    }

    @Contract(pure = true)
    static @NotNull SlotCreator<Slot> lockedSlotCreator(Container container, ItemStack locked) {
        return (i, x, y) -> {
            if (container.getItem(i) == locked) return new LockedSlot(container, i, x, y);
            return new Slot(container, i, x, y);
        };
    }

    @Contract(pure = true)
    static @NotNull SlotCreator<Slot> defaultCreator(Container container) {
        return (i, x, y) -> new Slot(container,  i, x, y);
    }
}