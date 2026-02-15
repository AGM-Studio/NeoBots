package xyz.agmstudio.neobots.menus;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
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
import xyz.agmstudio.neobots.gui.Texture;
import xyz.agmstudio.neobots.index.CNBMenus;
import xyz.agmstudio.neobots.modules.abstracts.item.ModuleItem;
import xyz.agmstudio.neobots.robos.NeoBotEntity;
import xyz.agmstudio.neobots.upgrades.BotUpgradeItem;


public class NeoBotMenu extends AbstractMenu {
    private static final Texture BG = new Texture("textures/gui/neobot.png", 224, 215);

    private final NeoBotEntity bot;
    protected final DataSlot botState = DataSlot.standalone();
    protected final DataSlot activeModule = DataSlot.standalone();
    protected final DataSlot moduleCapacity = DataSlot.standalone();

    private final SlotGroupHolder batteryGroup;
    private final SlotGroupHolder moduleGroup;
    private final SlotGroupHolder upgradeGroup;
    private final SlotGroupHolder botInventoryGroup;

    private boolean active;
    private final IconButton stop;
    private final IconButton start;

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

        addDataSlot(botState);
        addDataSlot(activeModule);
        addDataSlot(moduleCapacity);

        batteryGroup      = addSlotGroup(bot.getBatteryInventory(), 1, 1, 24, 70).build(this);
        moduleGroup       = addSlotGroup(bot.getModuleInventory(), 5, 8, -108, 40).pad(2).withTextureOffset(2, 2).withTexture(i -> i == activeModule.get() ? ACTIVE_SLOT_TEXTURE : SLOT_TEXTURE).build(this);
        upgradeGroup      = addSlotGroup(bot.getUpgradeInventory(), 3, 4, 228, 40).pad(2).withTextureOffset(2, 2).withTexture(UPGRADE_SLOT_TEXTURE).build(this);
        botInventoryGroup = addSlotGroup(bot.getInventory(), 3, 3, 144, 28).build(this);

        addPlayerInventoryTitle(112, 100).centered().withColor(0x000000);
        addPlayerInventory(24, 116, 2, 5, 18);

        stop = addIconButton(81, 69, AllIcons.I_STOP).withCallback(() -> {
            sendPacket(0, false);
            updateIconButtons();
        });
        start = addIconButton(103, 69, AllIcons.I_PLAY).withCallback(() -> {
            sendPacket(0, true);
            updateIconButtons();
        });

        addIconButton(53, 69, AllIcons.I_REFRESH).withCallback(() -> sendPacket(1, true));
        updateIconButtons();

        // Setup GUI
        addLabel(s -> bot.getDisplayName(), 112, 4).withColor(0xffffff).withShadow().centered();
        addLabel(s -> NeoBotEntity.TASK_STATUS.get(bot), 15, 27).withColor(0xffffff).withShadow().width(108).scale(0.85f);
        addLabel(Component.literal("Modules"), -112, 23).withColor(0x582424);
        addLabel(Component.literal("Upgrades"), 225, 23).withColor(0x582424);
        addTextureDrawer(SIMPLE_FRAME.frameDrawer(-116, 19, 128, moduleGroup.activeHeight() + 42, 3, 16, true, true));
        addTextureDrawer(SIMPLE_FRAME.frameDrawer(210, 19, 86, upgradeGroup.activeHeight() + 42, 3, 16, true, true));

        // Offset the screen upward (GUI SCALE 4)
        addInitListener(s -> s.offset(-30, -24));
    }

    @Override public void handlePacket(int id, boolean value) {
        if (id == 0) {
            if (value) { // Run Button
                bot.setState(NeoBotEntity.State.RUNNING);
                botState.set(1);
            } else {     // Stop Button
                bot.setState(NeoBotEntity.State.STOPPED);
                botState.set(0);
            }
        } else if (id == 1 && value) {  // Reset Tasks Button
            bot.setActiveModule(0);
            bot.setState(NeoBotEntity.State.RUNNING);
            botState.set(1);
            updateIconButtons();
        }
    }

    @Override protected void updateIconButtons() {
        stop.active = botState.get() == 1;
        start.active = botState.get() != 1 && botState.get() != -2;
        super.updateIconButtons();
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
            else if (BotUpgradeItem.isUpgrade(stack)) moved = moveTo(upgradeGroup, stack, false);
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
        if (bot == null || bot.level().isClientSide) return;

        botState.set(bot.getState().getValue());
        activeModule.set(bot.getActiveModuleIndex());
        moduleCapacity.set(bot.getModuleCapacity());
    }

    @Override protected Texture getBackground() {
        return BG;
    }
}