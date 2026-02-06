package xyz.agmstudio.neobots.menus;

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
import xyz.agmstudio.neobots.gui.Texture;
import xyz.agmstudio.neobots.modules.BotModuleItem;
import xyz.agmstudio.neobots.robos.NeoBotEntity;
import xyz.agmstudio.neobots.upgrades.BotUpgradeItem;

public class NeoBotMenu extends AbstractMenu {
    private static final Texture BG = new Texture("textures/gui/neobot.png", 224, 215);

    private final NeoBotEntity bot;
    protected final DataSlot activeModule = DataSlot.standalone();
    protected final DataSlot moduleCapacity = DataSlot.standalone();

    private final int moduleSlotSize;
    private final int upgradeSlotSize;
    private final int inventorySlotSize;

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

        moduleSlotSize = bot.getModuleInventory().getContainerSize();
        upgradeSlotSize = bot.getUpgradeInventory().getContainerSize();
        inventorySlotSize = bot.getInventory().getContainerSize();

        addDataSlot(activeModule);
        addDataSlot(moduleCapacity);

        addSlotGroup(bot.getModuleInventory(), 5, 8, -108, 40).pad(2).withTextureOffset(2, 2).withTexture(i -> i == activeModule.get() ? ACTIVE_SLOT_TEXTURE : SLOT_TEXTURE).build(this);
        addSlotGroup(bot.getUpgradeInventory(), 3, 4, 228, 40).pad(2).withTextureOffset(2, 2).withTexture(UPGRADE_SLOT_TEXTURE).build(this);
        addSlotGroup(bot.getInventory(), 4, 7, 24, 64).build(this);

        addPlayerInventoryTitle(112, 100).centered().withColor(0x000000);
        addPlayerInventory(24, 116, 2, 5, 18);

        // Setup GUI // Todo: SlotGroup Framing and shift click support.
        addLabel(s -> bot.getDisplayName(), 112, 4).withColor(0xffffff).withShadow().centered();
        addLabel(Component.literal("Modules"), -112, 23).withColor(0x000000);
        addLabel(Component.literal("Upgrades"), 225, 23).withColor(0x000000);
        addTextureDrawer(SIMPLE_FRAME.frameDrawer(-116, 19, 128, 128, 3, 16, true, true));
        addTextureDrawer(SIMPLE_FRAME.frameDrawer(210, 19, 86, 128, 3, 16, true, true));

        // Offset the screen upward (GUI SCALE 4)
        addInitListener(s -> s.offset(-20, -24));
    }

    @Override public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index < moduleSlotSize +  upgradeSlotSize) { // --- FROM BOT → PLAYER ---
            if (!moveItemStackTo(stack, moduleSlotSize + upgradeSlotSize + inventorySlotSize, moduleSlotSize + upgradeSlotSize + inventorySlotSize + 36, true))
                return ItemStack.EMPTY;
        } else { // --- FROM PLAYER → BOT ---
            if (BotModuleItem.isModule(stack)) {
                if (!moveItemStackTo(stack, 0, moduleSlotSize, false))
                    return ItemStack.EMPTY;
            } else if (BotUpgradeItem.isUpgrade(stack)) {
                if (!moveItemStackTo(stack, moduleSlotSize, moduleSlotSize + upgradeSlotSize, false))
                    return ItemStack.EMPTY;
            } else {
                if (!moveItemStackTo(stack, moduleSlotSize + upgradeSlotSize, moduleSlotSize + upgradeSlotSize + inventorySlotSize, false))
                    return ItemStack.EMPTY;
            }
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

    @Override protected Texture getBackground() {
        return BG;
    }
}