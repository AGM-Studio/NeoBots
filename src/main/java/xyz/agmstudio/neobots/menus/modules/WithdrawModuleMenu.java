package xyz.agmstudio.neobots.menus.modules;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.containers.slotgroups.SlotGroupHolder;
import xyz.agmstudio.neobots.gui.Texture;
import xyz.agmstudio.neobots.menus.AbstractMenu;
import xyz.agmstudio.neobots.modules.WithdrawModule;
import xyz.agmstudio.neobots.utils.NeoBotsHelper;

public class WithdrawModuleMenu extends AbstractMenu {
    private static final Texture BG = new Texture("textures/gui/one_slot_panel.png", 176, 204);

    private final WithdrawModule.Data data;
    private final SimpleContainer filterContainer;
    private final SlotGroupHolder filterHolder;
    private final IconButton skipButton;

    // GUI Variables - Not synced!
    private int count;
    public boolean skip;

    public WithdrawModuleMenu(int id, Inventory inv, FriendlyByteBuf ignored) {
        this(id, inv);
    }
    public WithdrawModuleMenu(int id, Inventory inv) {
        super(WithdrawModule.MENU.get(), id, inv);
        this.filterContainer = new SimpleContainer(1) {
            @Override public int getMaxStackSize() {
                return 1;
            }
            @Override public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
                return !stack.isEmpty();
            }
            @Override public void setChanged() {
                super.setChanged();
                updateFilter();
            }
        };

        data = new WithdrawModule.Data(inv.player.level(), inv.player.getMainHandItem());
        if (!data.getFilter().isEmpty()) filterContainer.setItem(0, data.getFilter().copy());
        count = data.getCount();
        skip = data.getSkip();

        filterHolder = SlotGroupHolder.of(this, new Slot(filterContainer, 0, 26, 48));

        addPlayerInventoryTitle(8, 110);
        addPlayerInventory(8, 122, data.getStack());

        // Setup GUI
        addScrollInput(51, 51, 96, 10).withRange(1, 577)
                .setState(data.getCount())
                .titled(Component.literal("Count"))
                .calling(value -> {
                    count = value;
                    sendPacket(0, value);
                });
        skipButton = addIconButton(40, 79, AllIcons.I_SKIP_MISSING).withCallback(() -> {
            skip = !skip;
            sendPacket(0, skip);
            updateIconButtons();
        });

        addTitleCentered(4).withColor(0x582424);
        addLabel(s -> NeoBotsHelper.countAsStacks(count), 54, 52).withColor(0xffffff).withShadow();

        int targetColor = 0xcc0000;
        Component target = Component.literal("Right click to set target");
        if (data.getTarget() != null) {
            targetColor = 0xffffff;
            target = inventory.player.level().getBlockState(data.getTarget()).getBlock().getName()
                    .append(Component.literal(" (" + data.getTarget().toShortString() + ")"));
        }
        addLabel(target, 30, 28).withColor(targetColor).withShadow();
    }

    @Override protected boolean isIconButtonActive(IconButton button) {
        return button == skipButton && skip;
    }
    @Override public void handlePacket(int id, boolean value) {
        data.setSkip(value);
        data.save();
    }
    @Override public void handlePacket(int id, int value) {
        if (value < 1 || value > 576) return;
        data.setCount(value);
        data.save();
    }
    private void updateFilter() {
        ItemStack filter = filterContainer.getItem(0);
        data.setFilter(filter);
        data.save();
    }

    @Override public boolean stillValid(@NotNull Player player) {
        return player.getMainHandItem() == data.getStack() || player.getOffhandItem() == data.getStack();
    }

    @Override public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (stack == data.getStack()) return ItemStack.EMPTY;
        SlotGroupHolder source = findGroup(index);
        if (source == null) return ItemStack.EMPTY;

        boolean moved;
        if (source == filterHolder) moved = moveTo(playerInventoryGroup, stack, true);
        else if (source == playerInventoryGroup) moved = moveTo(filterHolder, stack, false);
        else return ItemStack.EMPTY;

        if (!moved) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        slot.onTake(player, stack);
        return copy;
    }

    @Override protected Texture getBackground() {
        return BG;
    }
}