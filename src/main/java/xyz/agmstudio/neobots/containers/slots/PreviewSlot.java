package xyz.agmstudio.neobots.containers.slots;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PreviewSlot extends Slot {
    public PreviewSlot(ItemStack item, int x, int y) {
        super(new SimpleContainer(1), 0, x, y);
        container.setItem(0, item);
    }
    @Override public boolean mayPickup(@NotNull Player player) {
        return false;
    }
    @Override public boolean mayPlace(@NotNull ItemStack stack) {
        return false;
    }
}