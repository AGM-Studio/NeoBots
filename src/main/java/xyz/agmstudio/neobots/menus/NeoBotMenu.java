package xyz.agmstudio.neobots.menus;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.modules.BotModuleItem;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

public class NeoBotMenu extends AbstractContainerMenu {
    private final NeoBotEntity bot;
    protected final DataSlot activeModule = DataSlot.standalone();

    private final int moduleSlotSize;
    private final int upgradeSlotSize;

    private static NeoBotEntity captureBot(Level level, FriendlyByteBuf buf) {
        Entity entity = level.getEntity(buf.readInt());
        if (entity instanceof NeoBotEntity bot) return bot;
        throw new IllegalStateException("The provided entity is not a NeoBotEntity!");
    }
    public NeoBotMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, captureBot(inv.player.level(), buf));
    }

    public NeoBotMenu(int id, Inventory inv, NeoBotEntity bot) {
        super(NeoBots.NEOBOT_INVENTORY.get(), id);
        this.bot = bot;

        addDataSlot(activeModule);

        SimpleContainer modules = bot.getModuleInventory();
        moduleSlotSize = modules.getContainerSize();
        addRectangleShapedSlots(modules, 4, (int) Math.ceil(moduleSlotSize / 4.0), 6, 19, 0, -1, (c, index, x, y) ->
            this.addSlot(new Slot(c, index, x, y) {
                @Override public boolean mayPlace(@NotNull ItemStack stack) {
                    return isModuleItem(stack);
                }
            })
        );

        SimpleContainer upgrades = bot.getUpgradeInventory();
        upgradeSlotSize = upgrades.getContainerSize();
        addRectangleShapedSlots(upgrades, 1, upgradeSlotSize, 258, 12,0, 7, (c, index, x, y) ->
            this.addSlot(new Slot(upgrades, index, x, y) {
                @Override public boolean mayPlace(@NotNull ItemStack stack) {
                    return isUpgradeItem(stack);
                }
            })
        );

        // Player inventory
        addRectangleShapedSlots(inv, 9, 3, 87, 84, 9);
        addRectangleShapedSlots(inv, 9, 1, 87, 142, 0, 9);
    }

    private static boolean isModuleItem(ItemStack stack) {
        return stack.getItem() instanceof BotModuleItem;
    }
    private static boolean isUpgradeItem(ItemStack stack) {
        return false;
    }

    @Override public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();


        if (index < moduleSlotSize +  upgradeSlotSize) { // --- FROM BOT → PLAYER ---
            if (!moveItemStackTo(stack, moduleSlotSize + upgradeSlotSize, moduleSlotSize + upgradeSlotSize + 36, true))
                return ItemStack.EMPTY;
        } else { // --- FROM PLAYER → BOT ---
            if (isModuleItem(stack)) {
                if (!moveItemStackTo(stack, 0, moduleSlotSize, false))
                    return ItemStack.EMPTY;
            } else if (isUpgradeItem(stack)) {
                if (!moveItemStackTo(stack, moduleSlotSize, moduleSlotSize + upgradeSlotSize, false))
                    return ItemStack.EMPTY;
            } else return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        slot.onTake(player, stack);
        return copy;
    }

    @Override public boolean stillValid(@NotNull Player player) {
        return bot.isAlive() && player.distanceTo(bot) < 8.0F;
    }
    @Override public void broadcastChanges() {
        super.broadcastChanges();
        if (bot == null || bot.level().isClientSide) return;

        activeModule.set(bot.getActiveModuleIndex());
    }

    public NeoBotEntity getBot() {
        return bot;
    }

    public void addRectangleShapedSlots(Container inv, int w, int h, int x, int y, int offset) {
        NeoBotMenu.this.addRectangleShapedSlots(inv, w, h, x, y, offset, -1, (c, index, px, py) ->
            this.addSlot(new Slot(c, index, px, py))
        );
    }
    public void addRectangleShapedSlots(Container inv, int w, int h, int x, int y, int offset, int limit) {
        NeoBotMenu.this.addRectangleShapedSlots(inv, w, h, x, y, offset, limit, (c, index, px, py) ->
            this.addSlot(new Slot(c, index, px, py))
        );
    }
    public void addRectangleShapedSlots(Container inv, int w, int h, int x, int y, int offset, int limit, MenuSlotCreator creator) {
        int size = inv.getContainerSize();
        for (int j = 0; j < h; ++j) for (int i = 0; i < w; i++) {
            int index = j * w + i + offset;
            if (index >= size) continue;
            if (limit > -1 && index >= limit) continue;
            creator.create(inv, index, x + i * 18, y + j * 18);
        }
    }

    public interface MenuSlotCreator {
        void create(Container inv, int index, int x, int y);
    }
}
