package xyz.agmstudio.neobots.containers.slots;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class NeoSlot extends Slot {
    private boolean active = true;
    public NeoSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override public boolean isActive() {
        return active;
    }
    public NeoSlot setActive(boolean active) {
        this.active = active;
        return this;
    }
}