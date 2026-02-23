package xyz.agmstudio.neobots.menus;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.containers.slotgroups.SlotGroupHolder;
import xyz.agmstudio.neobots.containers.slots.FilterSlot;
import xyz.agmstudio.neobots.containers.slots.PreviewSlot;
import xyz.agmstudio.neobots.index.CNBMenus;
import xyz.agmstudio.neobots.menus.abstracts.AbstractMenu;
import xyz.agmstudio.neobots.modules.abstracts.data.ModuleData;
import xyz.agmstudio.neobots.modules.abstracts.data.ModuleTransferData;
import xyz.agmstudio.neobots.modules.abstracts.item.ModuleItem;

public class TransferModuleMenu extends AbstractMenu {
    protected final ModuleTransferData data;
    protected final ItemStack module;
    protected final FilterSlot filterSlot;
    protected final SlotGroupHolder filterHolder;

    public static @NotNull TransferModuleMenu create(int id, Inventory inv) {
        return new TransferModuleMenu(CNBMenus.TRANSFER_MODULE.get(), id, inv);
    }
    public TransferModuleMenu(MenuType<?> menu, int id, Inventory inv) {
        super(menu, id, inv);
        this.module = inv.player.getMainHandItem();
        if (module.getItem() instanceof ModuleItem<?, ?> m) {
            ModuleData data = m.getData(inv.player.level(), this.module);
            if (data instanceof ModuleTransferData td) this.data = td;
            else throw new IllegalArgumentException("Invalid module type for menu");
        } else throw new IllegalArgumentException("Invalid module type for menu");

        this.filterSlot = new FilterSlot(data.getFilter(), 26, 48, this::updateFilter);
        this.filterHolder = SlotGroupHolder.of(this, filterSlot);

        addPlayerInventory(8, 122, this.data.getStack());
        addSlot(new PreviewSlot(data.getStack(), 18, 80));
    }

    @Override public void handlePacket(int id, boolean value) {
        if (id == 0) {
            data.setSkip(value);
            data.save();
        } else if (id == 1) {
            data.save(inventory.player.getMainHandItem());
        }
    }
    @Override public void handlePacket(int id, int value) {
        if (value < 1 || value > 576) return;
        data.setCount(value);
        data.save();
    }
    private void updateFilter(ItemStack filter) {
        data.setFilter(filter);
        data.save();
    }

    @Override public boolean stillValid(@NotNull Player player) {
        return player.getMainHandItem() == module;
    }

    @Override public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (stack == data.getStack()) return ItemStack.EMPTY;
        SlotGroupHolder source = findGroup(index);
        if (source == null) return ItemStack.EMPTY;

        if (source == filterHolder && filterSlot.hasItem()) filterSlot.set(ItemStack.EMPTY);
        else if (source == playerInventoryGroup && !filterSlot.hasItem()) filterSlot.set(stack.copy());
        else return ItemStack.EMPTY;

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        slot.onTake(player, stack);
        return copy;
    }
}