package xyz.agmstudio.neobots.menus;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.containers.slotgroups.SlotGroupHolder;
import xyz.agmstudio.neobots.containers.slots.FilterSlot;
import xyz.agmstudio.neobots.containers.slots.PreviewSlot;
import xyz.agmstudio.neobots.gui.Texture;
import xyz.agmstudio.neobots.modules.abstracts.data.ModuleTransferData;
import xyz.agmstudio.neobots.utils.NeoBotsHelper;

public abstract class TransferModuleMenu<D extends ModuleTransferData> extends AbstractMenu {
    private static final Texture BG = new Texture("textures/gui/one_slot_panel.png", 176, 204);

    private final D data;
    private final ItemStack module;
    private final FilterSlot filterSlot;
    private final SlotGroupHolder filterHolder;
    private final IconButton skipButton;

    // GUI Variables - Not synced!
    private int count;
    public boolean skip;

    public TransferModuleMenu(MenuType<?> menu, int id, Inventory inv, D data) {
        super(menu, id, inv);
        this.data = data;
        this.module = inv.player.getMainHandItem();
        this.count = this.data.getCount();
        this.skip = this.data.getSkip();

        this.filterSlot = new FilterSlot(data.getFilter(), 26, 48, this::updateFilter);
        this.filterHolder = SlotGroupHolder.of(this, filterSlot);

        addPlayerInventoryTitle(8, 110);
        addPlayerInventory(8, 122, this.data.getStack());

        addSlot(new PreviewSlot(data.getStack(), 18, 80));

        // Setup GUI
        addScrollInput(51, 51, 96, 10).withRange(1, 577)
                .setState(this.data.getCount())
                .titled(Component.literal("Count"))
                .calling(value -> {
                    count = value;
                    sendPacket(0, count);
                });
        skipButton = addIconButton(40, 79, AllIcons.I_SKIP_MISSING).withCallback(() -> {
            skip = !skip;
            sendPacket(0, skip);
            updateIconButtons();
        });
        addIconButton(148, 79, AllIcons.I_CONFIRM).withCallback(() -> {
            sendPacket(1, true);
            inventory.player.closeContainer();
        });


        addTitleCentered(4).withColor(0x582424);
        addLabel(s -> NeoBotsHelper.countAsStacks(count), 54, 52).withColor(0xffffff).withShadow();

        int targetColor = 0xcc0000;
        Component target = Component.literal("Right click to set target");
        if (this.data.getTarget() != null) {
            targetColor = 0xffffff;
            target = inventory.player.level().getBlockState(this.data.getTarget()).getBlock().getName()
                    .append(Component.literal(" (" + this.data.getTarget().toShortString() + ")"));
        }
        addLabel(target, 30, 28).withColor(targetColor).withShadow();
    }

    @Override protected boolean isIconButtonActive(IconButton button) {
        return button == skipButton && skip;
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

    @Override protected Texture getBackground() {
        return BG;
    }
}