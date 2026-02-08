package xyz.agmstudio.neobots.menus;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.containers.slotgroups.SlotGroupHolder;
import xyz.agmstudio.neobots.containers.slots.PreviewSlot;
import xyz.agmstudio.neobots.gui.Texture;
import xyz.agmstudio.neobots.modules.abstracts.ModuleItem;
import xyz.agmstudio.neobots.robos.NeoBotEntity;
import xyz.agmstudio.neobots.upgrades.BotUpgradeItem;

public class NeoBotMenu extends AbstractMenu {
    private static final Texture BG = new Texture("textures/gui/neobot.png", 224, 226);

    private final NeoBotEntity bot;
    protected final DataSlot botState = DataSlot.standalone();
    protected final DataSlot activeModule = DataSlot.standalone();
    protected final DataSlot moduleCapacity = DataSlot.standalone();

    private final SlotGroupHolder moduleGroup;
    private final SlotGroupHolder upgradeGroup;
    private final SlotGroupHolder botInventoryGroup;
    private final PreviewSlot modulePreviewSlot;

    private boolean active;
    private final IconButton stop;
    private final IconButton start;

    private static NeoBotEntity captureBot(Level level, FriendlyByteBuf buf) {
        Entity entity = level.getEntity(buf.readInt());
        if (entity instanceof NeoBotEntity bot) return bot;
        throw new IllegalStateException("The provided entity is not a NeoBotEntity!");
    }
    public NeoBotMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, captureBot(inv.player.level(), buf));
    }

    public NeoBotMenu(int id, Inventory inv, NeoBotEntity bot) {
        super(NeoBots.NEOBOT_INVENTORY.get(), id, inv);
        this.bot = bot;

        addDataSlot(botState);
        addDataSlot(activeModule);
        addDataSlot(moduleCapacity);

        moduleGroup       = addSlotGroup(bot.getModuleInventory(), 5, 8, -108, 40).pad(2).withTextureOffset(2, 2).withTexture(i -> i == activeModule.get() ? ACTIVE_SLOT_TEXTURE : SLOT_TEXTURE).build(this);
        upgradeGroup      = addSlotGroup(bot.getUpgradeInventory(), 3, 4, 228, 40).pad(2).withTextureOffset(2, 2).withTexture(UPGRADE_SLOT_TEXTURE).build(this);
        botInventoryGroup = addSlotGroup(bot.getInventory(), 4, 7, 24, 64).build(this);

        addPlayerInventoryTitle(112, 215).centered().withColor(0x000000);
        addPlayerInventory(24, 127, 2, 5, 18);

        modulePreviewSlot = new PreviewSlot(bot.getModuleInventory().getModuleStack(), 16, 27);
        addSlot(modulePreviewSlot);

        stop = addIconButton(179, 100, AllIcons.I_STOP).withCallback(() -> {
            sendPacket(0, false);
            updateIconButtons();
        });
        start = addIconButton(201, 100, AllIcons.I_PLAY).withCallback(() -> {
            sendPacket(0, true);
            updateIconButtons();
        });

        addIconButton(151, 100, AllIcons.I_REFRESH).withCallback(() -> sendPacket(1, true));
        updateIconButtons();

        // Setup GUI // Todo: SlotGroup Framing and shift click support.
        addLabel(s -> bot.getDisplayName(), 112, 4).withColor(0xffffff).withShadow().centered();
        addLabel(s -> NeoBotEntity.TASK_STATUS.get(bot), 43, 31).withColor(0xffffff).withShadow();
        addLabel(Component.literal("Modules"), -112, 23).withColor(0x582424);
        addLabel(Component.literal("Upgrades"), 225, 23).withColor(0x582424);
        addTextureDrawer(SIMPLE_FRAME.frameDrawer(-116, 19, 128, 128, 3, 16, true, true));
        addTextureDrawer(SIMPLE_FRAME.frameDrawer(210, 19, 86, 128, 3, 16, true, true));

        // Offset the screen upward (GUI SCALE 4)
        addInitListener(s -> s.offset(-30, -24));
    }

    @Override public void handlePacket(int id, boolean value) {
        if (id == 0) {
            if (value) bot.setState(NeoBotEntity.State.RUNNING);
            else bot.setState(NeoBotEntity.State.STOPPED);
        } else if (id == 1 && value) {
            bot.setActiveModule(0);
        }
    }

    @Override protected void updateIconButtons() {
        super.updateIconButtons();
        stop.active = botState.get() == 1;
        start.active = botState.get() != 1 && botState.get() != -1;
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
        modulePreviewSlot.set(bot.getModuleInventory().getModuleStack());
    }

    @Override protected Texture getBackground() {
        return BG;
    }
}