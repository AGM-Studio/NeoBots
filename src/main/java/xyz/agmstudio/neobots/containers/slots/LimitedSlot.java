package xyz.agmstudio.neobots.containers.slots;

import net.minecraft.world.Container;

public class LimitedSlot extends NeoSlot {
    private final int count;

    public LimitedSlot(Container container, int index, int x, int y, int count) {
        super(container, index, x, y);
        this.count = count;
    }

    @Override
    public int getMaxStackSize() {
        return count;
    }
}