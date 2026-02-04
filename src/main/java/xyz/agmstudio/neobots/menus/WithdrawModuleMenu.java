package xyz.agmstudio.neobots.menus;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.components.WithdrawModuleComponent;

import java.util.Optional;

public class WithdrawModuleMenu extends AbstractNeoMenu {
    private final ItemStack moduleStack;
    private final SimpleContainer filterContainer;

    public WithdrawModuleMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv);
    }
    public WithdrawModuleMenu(int id, Inventory inv) {
        super(NeoBots.WITHDRAW_MENU.get(), id);

        this.moduleStack = inv.player.getMainHandItem();
        this.filterContainer = new SimpleContainer(1) {
            @Override public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
                return !stack.isEmpty();
            }
            @Override public void setChanged() {
                super.setChanged();
                updateComponentFromSlot();
            }
        };

        WithdrawModuleComponent data = getComponent();
        if (data.filter().isPresent()) filterContainer.setItem(0, data.filter().get().copy());
        this.addSlot(new Slot(filterContainer, 0, 152, 16) {
            @Override public int getMaxStackSize() {
                return 1;
            }
        });

        // Player inventory
        addRectangleShapedSlots(inv, 9, 3, 8, 55, 9, -1, SlotCreator.lockedSlotCreator(inv, moduleStack));
        addRectangleShapedSlots(inv, 9, 1, 8, 113, 0, 9, SlotCreator.lockedSlotCreator(inv, moduleStack));
    }

    private WithdrawModuleComponent getComponent() {
        WithdrawModuleComponent component = moduleStack.get(NeoBots.WITHDRAW.get());
        if (component != null) return component;
        return new WithdrawModuleComponent(null, null, 1, Optional.empty());
    }

    private void updateComponentFromSlot() {
        ItemStack filter = filterContainer.getItem(0);

        WithdrawModuleComponent component = getComponent().withFilter(filter);
        moduleStack.set(NeoBots.WITHDRAW.get(), component);
    }

    public int updateCount(int value) {
        int count = Math.max(1, Math.min(64, value));

        WithdrawModuleComponent component = getComponent().withCount(count);
        moduleStack.set(NeoBots.WITHDRAW.get(), component);
        return count;
    }

    public int getCount() {
        return getComponent().count();
    }
    public BlockPos getPos() {
        return getComponent().source();
    }
    public ResourceKey<Level> getDimension() {
        return getComponent().dimension();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return player.getMainHandItem() == moduleStack || player.getOffhandItem() == moduleStack;
    }

    @Override public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (stack == moduleStack) return ItemStack.EMPTY;

        if (index == 0) {
            if (!moveItemStackTo(stack, 1, 37, true))
                return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(stack, 0, 1, false))
                return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        slot.onTake(player, stack);
        return copy;
    }

    @Override public boolean clickMenuButton(@NotNull Player player, int id) {
        if (id < 1 || id > 64) return false;

        WithdrawModuleComponent component = getComponent().withCount(id);
        moduleStack.set(NeoBots.WITHDRAW.get(), component);
        return true;
    }
}
