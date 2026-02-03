package xyz.agmstudio.neobots.menus;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
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
import xyz.agmstudio.neobots.containers.ModuleContainer;
import xyz.agmstudio.neobots.containers.UpgradeContainer;
import xyz.agmstudio.neobots.modules.BotModuleItem;
import xyz.agmstudio.neobots.robos.NeoBotEntity;
import xyz.agmstudio.neobots.upgrades.BotUpgradeItem;

import java.util.function.Predicate;

public class NeoBotMenu extends AbstractContainerMenu {
    private final NeoBotEntity bot;
    protected final DataSlot activeModule = DataSlot.standalone();
    protected final DataSlot moduleCapacity = DataSlot.standalone();

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
        addDataSlot(moduleCapacity);

        ModuleContainer modules = bot.getModuleInventory();
        addRectangleShapedSlots(modules, 4, 8, 6, 19, 0, -1, ModuleContainer.Slot.builder(bot));
        moduleSlotSize = modules.getContainerSize();

        UpgradeContainer upgrades = bot.getUpgradeInventory();
        addRectangleShapedSlots(upgrades, 1, 7, 258, 12,0, 7, conditionalSlotCreator(BotUpgradeItem::isUpgrade));
        upgradeSlotSize = upgrades.getContainerSize();

        // Player inventory
        addRectangleShapedSlots(inv, 9, 3, 87, 84, 9);
        addRectangleShapedSlots(inv, 9, 1, 87, 142, 0, 9);
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
            if (BotModuleItem.isModule(stack)) {
                if (!moveItemStackTo(stack, 0, moduleSlotSize, false))
                    return ItemStack.EMPTY;
            } else if (BotUpgradeItem.isUpgrade(stack)) {
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
        moduleCapacity.set(bot.getModuleCapacity());
    }

    public NeoBotEntity getBot() {
        return bot;
    }

    public MenuSlotCreator conditionalSlotCreator(Predicate<ItemStack> place) {
        return (c, i, x, y) -> new Slot(c, i, x, y) {
            @Override public boolean mayPlace(@NotNull ItemStack stack) {
                return place.test(stack);
            }
        };
    }
    public void addRectangleShapedSlots(Container inv, int w, int h, int x, int y, int offset) {
        addRectangleShapedSlots(inv, w, h, x, y, offset, -1, null);
    }
    public void addRectangleShapedSlots(Container inv, int w, int h, int x, int y, int offset, int limit) {
        addRectangleShapedSlots(inv, w, h, x, y, offset, limit, null);
    }
    public void addRectangleShapedSlots(Container inv, int w, int h, int x, int y, int offset, int limit, MenuSlotCreator creator) {
        if (creator == null) creator = net.minecraft.world.inventory.Slot::new;
        int maxByGrid = offset + w * h;
        int maxByLimit = limit > -1 ? offset + limit : Integer.MAX_VALUE;
        int last = Math.min(Math.min(maxByGrid, maxByLimit), inv.getContainerSize());
        for (int i = offset; i < last; i++) {
            int index = i - offset;
            int px = x + (index % w) * 18;
            int py = y + (index / w) * 18;
            this.addSlot(creator.create(inv, i, px, py));
        }
    }

    public interface MenuSlotCreator {
        Slot create(Container inv, int index, int x, int y);
    }
}
