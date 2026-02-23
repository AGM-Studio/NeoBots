package xyz.agmstudio.neobots.menus;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.block.battery.BatteryItem;
import xyz.agmstudio.neobots.containers.slotgroups.SlotGroupHolder;
import xyz.agmstudio.neobots.index.CNBMenus;
import xyz.agmstudio.neobots.menus.abstracts.AbstractMenu;
import xyz.agmstudio.neobots.modules.abstracts.item.ModuleItem;
import xyz.agmstudio.neobots.robos.NeoBotEntity;
import xyz.agmstudio.neobots.upgrades.UpgradeItem;


public class NeoBotMenu extends AbstractMenu {
    protected final NeoBotEntity bot;
    protected final DataSlot activeModule = DataSlot.standalone();
    protected final DataSlot moduleCapacity = DataSlot.standalone();

    protected final SlotGroupHolder batteryGroup;
    protected final SlotGroupHolder moduleGroup;
    protected final SlotGroupHolder upgradeGroup;
    protected final SlotGroupHolder botInventoryGroup;

    public static @NotNull NeoBotMenu create(int id, Inventory inv, NeoBotEntity bot) {
        return new NeoBotMenu(CNBMenus.NEOBOT_INVENTORY.get(), id, inv, bot);
    }
    private static NeoBotEntity captureBot(@NotNull Level level, @NotNull FriendlyByteBuf buf) {
        Entity entity = level.getEntity(buf.readInt());
        if (entity instanceof NeoBotEntity bot) return bot;
        throw new IllegalStateException("The provided entity is not a NeoBotEntity!");
    }
    public NeoBotMenu(MenuType<NeoBotMenu> type, int id, Inventory inv, FriendlyByteBuf buf) {
        this(type, id, inv, captureBot(inv.player.level(), buf));
    }
    public NeoBotMenu(MenuType<NeoBotMenu> type, int id, Inventory inv, @NotNull NeoBotEntity bot) {
        super(type, id, inv);
        this.bot = bot;

        addDataSlot(activeModule);
        addDataSlot(moduleCapacity);

        moduleGroup       = addSlotGroup(bot.getModuleInventory(), 5, 8, -108, 40).pad(2)
                .withSlotSize(20, 20).build(this);
        upgradeGroup      = addSlotGroup(bot.getUpgradeInventory(), 5, 4, -108, 40).pad(2)
                .withSlotSize(20, 20).build(this);
        botInventoryGroup = addSlotGroup(bot.getInventory(), 3, 3, -71, 46).pad(2).build(this);
        batteryGroup      = addSlotGroup(bot.getBatteryInventory(), 1, 1, 118, 70).build(this);

        upgradeGroup.setVisible(false);
        botInventoryGroup.setVisible(false);
        addPlayerInventory(24, 116, 2, 5, 18);
    }

    @Override public void handlePacket(int id, boolean value) {
        if (id == 0) {
            if (value) bot.setState(NeoBotEntity.State.RUNNING);    // Run Button
            else bot.setState(NeoBotEntity.State.STOPPED);          // Stop Button
        } else if (id == 1 && value) {                              // Reset Tasks Button
            bot.setActiveModule(0);
            bot.setState(NeoBotEntity.State.RUNNING);
        }
    }

    @Override public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem())
            return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        SlotGroupHolder from = findGroup(index);
        if (from == null)
            return ItemStack.EMPTY;

        boolean moved;

        if (from == moduleGroup || from == upgradeGroup || from == botInventoryGroup) {
            moved = moveTo(playerInventoryGroup, stack, true);

        } else if (from == playerInventoryGroup) {
            if (ModuleItem.isModule(stack)) moved = moveTo(moduleGroup, stack, false);
            else if (stack.getItem() instanceof UpgradeItem) moved = moveTo(upgradeGroup, stack, false);
            else if (stack.getItem() instanceof BatteryItem) moved = moveTo(batteryGroup, stack, false);
            else moved = moveTo(botInventoryGroup, stack, false);
        } else return ItemStack.EMPTY;

        if (!moved) return ItemStack.EMPTY;
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

        activeModule.set(bot.getActiveModuleIndex());
        moduleCapacity.set(bot.getModuleCapacity());
    }
}