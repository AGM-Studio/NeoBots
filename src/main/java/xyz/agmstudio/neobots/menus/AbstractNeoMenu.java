package xyz.agmstudio.neobots.menus;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xyz.agmstudio.neobots.containers.BotFilteredContainer;

public abstract class AbstractNeoMenu extends AbstractContainerMenu {
    protected AbstractNeoMenu(@Nullable MenuType<?> type, int id) {
        super(type, id);
    }

    public void addRectangleShapedSlots(Container inv, int w, int h, int x, int y, int offset) {
        addRectangleShapedSlots(inv, w, h, x, y, offset, -1, null);
    }
    public void addRectangleShapedSlots(Container inv, int w, int h, int x, int y, int offset, int limit) {
        addRectangleShapedSlots(inv, w, h, x, y, offset, limit, null);
    }
    public void addRectangleShapedSlots(Container inv, int w, int h, int x, int y, int offset, int limit, SlotCreator creator) {
        if (creator == null) {
            if (inv instanceof BotFilteredContainer bfc) creator = bfc.slotBuilder();
            else creator = (i, px, py) -> new Slot(inv, i, px, py);
        }
        int maxByGrid = offset + w * h;
        int maxByLimit = limit > -1 ? offset + limit : Integer.MAX_VALUE;
        int last = Math.min(Math.min(maxByGrid, maxByLimit), inv.getContainerSize());
        for (int i = offset; i < last; i++) {
            int index = i - offset;
            int px = x + (index % w) * 18;
            int py = y + (index / w) * 18;
            this.addSlot(creator.create(i, px, py));
        }
    }

    public interface SlotCreator {
        Slot create(int index, int x, int y);

        static SlotCreator lockedSlotCreator(Container container, int locked) {
            return (i, x, y) -> {
                if (i == locked) return new Slot(container, i, x, y) {
                    @Override public boolean mayPickup(Player player) {
                        return false;
                    }
                    @Override public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                };

                return new Slot(container, i, x, y);
            };
        }

        static SlotCreator lockedSlotCreator(Container container, ItemStack locked) {
            return (i, x, y) -> {
                if (container.getItem(i) == locked) return new Slot(container, i, x, y) {
                    @Override public boolean mayPickup(Player player) {
                        return false;
                    }
                    @Override public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                };

                return new Slot(container, i, x, y);
            };
        }
    }
}
