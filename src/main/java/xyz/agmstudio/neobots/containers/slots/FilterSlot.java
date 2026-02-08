package xyz.agmstudio.neobots.containers.slots;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class FilterSlot extends Slot {
    private final Consumer<ItemStack> onSet;

    public FilterSlot(@NotNull ItemStack filter, int x, int y, Consumer<ItemStack> onSet) {
        super(new SimpleContainer(1), 0, x, y);
        if (!filter.isEmpty()) container.setItem(0, filter.copy());
        this.onSet = onSet;
    }

    @Override public void set(@NotNull ItemStack stack) {
        super.set(stack.copyWithCount(1));
        if (onSet != null) onSet.accept(stack);
    }

    @Override public boolean mayPickup(@NotNull Player player) { return false; }
    @Override public boolean mayPlace(@NotNull ItemStack stack) { return false; }
}