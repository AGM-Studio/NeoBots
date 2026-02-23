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
import xyz.agmstudio.neobots.index.CNBMenus;
import xyz.agmstudio.neobots.menus.abstracts.AbstractMenu;
import xyz.agmstudio.neobots.menus.gui.Texture;
import xyz.agmstudio.neobots.modules.abstracts.item.ModuleItem;
import xyz.agmstudio.neobots.robos.NeoBotEntity;
import xyz.agmstudio.neobots.upgrades.UpgradeItem;


public class NeoBotMenu extends AbstractMenu {
    private static final Texture BG = new Texture("textures/gui/neobot.png", 224, 215);

    private final NeoBotEntity bot;
    protected final DataSlot activeModule = DataSlot.standalone();
    protected final DataSlot moduleCapacity = DataSlot.standalone();

    private final SlotGroupHolder batteryGroup;
    private final SlotGroupHolder moduleGroup;
    private final IconButton moduleButton;
    private final SlotGroupHolder upgradeGroup;
    private final IconButton upgradeButton;
    private final SlotGroupHolder botInventoryGroup;
    private final IconButton botInventoryButton;

    private int state;
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

        addDataSlot(activeModule);
        addDataSlot(moduleCapacity);

        moduleGroup       = addSlotGroup(bot.getModuleInventory(), 5, 8, -108, 40).pad(2).withTextureOffset(2, 2)
                .withTexture(i -> i == activeModule.get() ? ACTIVE_SLOT_TEXTURE : SLOT_TEXTURE)
                .addLabel(Component.translatable("gui.create_neobots.modules_tab"), -112, 23).withColor(0x582424).build()
                .withFrame(BRASS_FRAME).minWidth(128).drawBeforeBg().build(this);
        upgradeGroup      = addSlotGroup(bot.getUpgradeInventory(), 5, 4, -108, 40).pad(2).withTextureOffset(2, 2)
                .withTexture(UPGRADE_SLOT_TEXTURE)
                .addLabel(Component.translatable("gui.create_neobots.upgrades_tab"), -112, 23).withColor(0x582424).build()
                .withFrame(BRASS_FRAME).minWidth(128).drawBeforeBg().build(this);
        botInventoryGroup = addSlotGroup(bot.getInventory(), 3, 3, -71, 46).pad(2).withTextureSize(18, 18)
                .withTexture(PACKAGE_SLOT_TEXTURE)
                .addLabel(Component.translatable("gui.create_neobots.bot_inventory"), -86, 23).withColor(0x582424).build()
                .withFrame(BRASS_FRAME).minWidth(128).offset(12, 7).offsetSize(0, 11) .drawBeforeBg().build()
                .withFrame(PACKAGE_FRAME).offset(0, -4).drawBeforeBg().build(this);
        batteryGroup      = addSlotGroup(bot.getBatteryInventory(), 1, 1, 118, 70).build(this);

        upgradeGroup.setVisible(false);
        botInventoryGroup.setVisible(false);
        addPlayerInventoryTitle(112, 100).centered().withColor(0x000000);
        addPlayerInventory(24, 116, 2, 5, 18);

        state = NeoBotEntity.STATE.get(bot);
        stop  = addIconButton(167,  69, AllIcons.I_PAUSE).withCallback(() -> {
            state = 0; updateIconButtons();
            sendPacket(0, false);
        });
        start = addIconButton(189, 69, AllIcons.I_PLAY).withCallback(() -> {
            state = 1; updateIconButtons();
            sendPacket(0, true);
        });

        addIconButton(139, 69, AllIcons.I_REFRESH).withCallback(() -> {
            state = 1; updateIconButtons();
            sendPacket(1, true);
        });
        moduleButton = addIconButton(17, 69, AllIcons.I_TOOLBOX).withCallback(() -> {
            moduleGroup.setVisible(true);
            upgradeGroup.setVisible(false);
            botInventoryGroup.setVisible(false);
            updateIconButtons();
        });
        upgradeButton = addIconButton(39, 69, AllIcons.I_PRIORITY_VERY_HIGH).withCallback(() -> {
            moduleGroup.setVisible(false);
            upgradeGroup.setVisible(true);
            botInventoryGroup.setVisible(false);
            updateIconButtons();
        });
        botInventoryButton = addIconButton(61, 69, AllIcons.I_DICE).withCallback(() -> {
            moduleGroup.setVisible(false);
            upgradeGroup.setVisible(false);
            botInventoryGroup.setVisible(true);
            updateIconButtons();
        });

        updateIconButtons();

        // Setup GUI
        addLabel(s -> bot.getDisplayName(), 112, 4).withColor(0xffffff).withShadow().centered();
        addLabel(s -> NeoBotEntity.TASK_STATUS.get(bot), 15, 27).withColor(0xffffff).withShadow().width(194);
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

    @Override protected void updateIconButtons() {
        stop.active = state == 1;
        start.active = state != 1 && state != -2;

        super.updateIconButtons();
    }

    @Override protected boolean isIconButtonActive(IconButton button) {
        if (button == moduleButton) return moduleGroup.isVisible();
        if (button == upgradeButton) return upgradeGroup.isVisible();
        if (button == botInventoryButton) return botInventoryGroup.isVisible();
        return false;
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

    @Override protected Texture getBackground() {
        return BG;
    }
}