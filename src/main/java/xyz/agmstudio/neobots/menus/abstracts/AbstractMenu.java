package xyz.agmstudio.neobots.menus.abstracts;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.agmstudio.neobots.containers.slotgroups.SlotCreator;
import xyz.agmstudio.neobots.containers.slotgroups.SlotGroup;
import xyz.agmstudio.neobots.containers.slotgroups.SlotGroupHolder;
import xyz.agmstudio.neobots.containers.slots.FilterSlot;
import xyz.agmstudio.neobots.containers.slots.NeoSlot;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMenu extends AbstractContainerMenu {
    protected final List<SlotGroup> slotGroups = new ArrayList<>();
    protected final List<SlotGroupHolder> slotHolders = new ArrayList<>();
    protected final Inventory inventory;

    protected SlotGroupHolder playerInventoryGroup = null;

    protected AbstractMenu(@Nullable MenuType<?> type, int id, Inventory inv) {
        super(type, id);
        this.inventory = inv;
    }

    protected SlotGroup addSlotGroup(Container inv, int w, int h, int x, int y) {
        SlotGroup group = new SlotGroup(inv, w, h, x, y);
        this.slotGroups.add(group);
        return group;
    }
    protected void addPlayerInventory(int x, int y) {
        addPlayerInventory(x, y, 0, 4, 18, null);
    }
    protected void addPlayerInventory(int x, int y, ItemStack lockedStack) {
        addPlayerInventory(x, y, 0, 4, 18, SlotCreator.lockedSlotCreator(inventory, lockedStack));
    }
    protected void addPlayerInventory(int x, int y, int slotPadding, int hotbarPadding, int slotSize) {
        addPlayerInventory(x, y, slotPadding, hotbarPadding, slotSize, null);
    }
    protected void addPlayerInventory(int x, int y, int slotPadding, int hotbarPadding, int slotSize, SlotCreator<? extends NeoSlot> creator) {
        if (creator == null) creator = SlotCreator.defaultCreator(inventory);
        playerInventoryGroup = addSlotGroup(inventory, 9, 3, x, y).offset(9).pad(slotPadding).withSlotSize(slotSize, slotSize).withSlotCreator(creator)
                .then(9, 1, x, y + hotbarPadding + 2 * slotPadding + 54).limit(9).build(this);
    }

    protected SlotGroupHolder findGroup(int index) {
        return slotHolders.stream().filter(g -> g.containsIndex(index)).findFirst().orElseThrow();
    }

    protected boolean moveTo(@NotNull SlotGroupHolder group, ItemStack stack, boolean reverse) {
        return moveItemStackTo(stack, group.firstIndex(), group.lastIndexExclusive(), reverse);
    }

    public void registerSlotGroup(SlotGroupHolder holder) {
        slotHolders.add(holder);
    }

    @Override
    public void clicked(int id, int dragType, @NotNull ClickType clickType, @NotNull Player player) {
        if (id >= 0 && id < slots.size()) {
            if (slots.get(id) instanceof FilterSlot filterSlot) {
                ItemStack carried = getCarried();
                if (!carried.isEmpty()) filterSlot.set(carried);
                else filterSlot.set(ItemStack.EMPTY);
                return;
            }
        }

        super.clicked(id, dragType, clickType, player);
    }

    public Inventory getInventory() {
        return inventory;
    }

    // Packet Handlers
    public void handlePacket(int id, int value) {}
    public void handlePacket(int id, double value) {}
    public void handlePacket(int id, boolean value) {}
    public void handlePacket(int id, String value) {}
}