package xyz.agmstudio.neobots.containers.slotgroups;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.containers.BotFilteredContainer;
import xyz.agmstudio.neobots.containers.slots.LockedSlot;
import xyz.agmstudio.neobots.containers.slots.NeoSlot;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface SlotCreator<T extends NeoSlot> {
    T create(int index, int x, int y);

    @Contract(pure = true)
    static @NotNull SlotCreator<NeoSlot> lockedSlotCreator(Container container, int locked) {
        return (i, x, y) -> {
            if (i == locked) return new LockedSlot(container, i, x, y);
            return new NeoSlot(container, i, x, y);
        };
    }

    @Contract(pure = true)
    static @NotNull SlotCreator<NeoSlot> lockedSlotCreator(Container container, ItemStack locked) {
        return (i, x, y) -> {
            if (container.getItem(i) == locked) return new LockedSlot(container, i, x, y);
            return new NeoSlot(container, i, x, y);
        };
    }

    @Contract(pure = true)
    static @NotNull SlotCreator<? extends NeoSlot> defaultCreator(Container container) {
        if (container instanceof BotFilteredContainer bfc) return bfc.slotBuilder();
        return (i, x, y) -> new NeoSlot(container,  i, x, y);
    }
}