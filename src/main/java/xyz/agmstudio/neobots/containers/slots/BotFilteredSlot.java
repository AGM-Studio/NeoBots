package xyz.agmstudio.neobots.containers.slots;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.containers.BotFilteredContainer;

public class BotFilteredSlot extends NeoSlot {
    private final BotFilteredContainer container;
    private final int index;

    public BotFilteredSlot(BotFilteredContainer container, int index, int x, int y) {
        super(container, index, x, y);
        this.container = container;
        this.index = index;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return container.canPlaceItem(index, stack);
    }

    @Override
    public boolean mayPickup(@NotNull Player player) {
        return index < container.getActiveSlots();
    }

    @Override public boolean isActive() {
        return super.isActive() && index < container.getActiveSlots();
    }
}