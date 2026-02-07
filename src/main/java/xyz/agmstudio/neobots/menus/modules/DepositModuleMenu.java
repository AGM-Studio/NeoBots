package xyz.agmstudio.neobots.menus.modules;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.containers.slotgroups.SlotGroupHolder;
import xyz.agmstudio.neobots.gui.Texture;
import xyz.agmstudio.neobots.menus.AbstractMenu;
import xyz.agmstudio.neobots.modules.DepositModule;
import xyz.agmstudio.neobots.utils.NeoBotsHelper;

public class DepositModuleMenu extends AbstractMenu {
    private static final Texture BG = new Texture("textures/gui/one_slot_panel.png", 176, 204);

    private final ItemStack moduleStack;
    private final SimpleContainer filterContainer;
    private final SlotGroupHolder filterHolder;
    private final IconButton skipButton;

    // GUI Variables - Not synced!
    private int count;
    public boolean skip = false;

    public DepositModuleMenu(int id, Inventory inv, FriendlyByteBuf ignored) {
        this(id, inv);
    }
    public DepositModuleMenu(int id, Inventory inv) {
        super(DepositModule.MENU.get(), id, inv);

        this.moduleStack = inv.player.getMainHandItem();
        this.filterContainer = new SimpleContainer(1) {
            @Override public int getMaxStackSize() {
                return 1;
            }
            @Override public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
                return !stack.isEmpty();
            }
            @Override public void setChanged() {
                super.setChanged();
                updateComponentFromSlot();
            }
        };

        DepositModule.DataComponent data = getComponent();
        data.filter().ifPresent(filter -> filterContainer.setItem(0, filter.copy()));
        count = data.count();
        skip = false;  // TODO Save in the module

        filterHolder = SlotGroupHolder.of(this, new Slot(filterContainer, 0, 26, 48));

        addPlayerInventoryTitle(8, 110);
        addPlayerInventory(8, 122, moduleStack);

        // Setup GUI
        addScrollInput(51, 51, 96, 10).withRange(1, 577)
                .setState(getCount())
                .titled(Component.literal("Count"))
                .calling(value -> {
                    count = value;
                    sendPacket(0, count);
                });
        skipButton = addIconButton(40, 80, AllIcons.I_SKIP_MISSING).withCallback(() -> {
            skip = !skip;
            sendPacket(0, skip);
            updateIconButtons();
        });

        addTitleCentered(4).withColor(0x582424);
        addLabel(s -> NeoBotsHelper.countAsStacks(count), 54, 52).withColor(0xffffff).withShadow();

        int targetColor = 0xcc0000;
        Component target = Component.literal("Right click to set target");
        if (getPos() != null) {
            targetColor = 0xffffff;
            target = inventory.player.level().getBlockState(getPos()).getBlock().getName()
                    .append(Component.literal(" (" + getPos().toShortString() + ")"));
        }
        addLabel(target, 30, 28).withColor(targetColor).withShadow();
    }

    @Override protected boolean isIconButtonActive(IconButton button) {
        return button == skipButton && skip;
    }
    @Override public void handlePacket(int id, boolean value) {
        skip = value;
    }
    @Override public void handlePacket(int id, int value) {
        if (value < 1 || value > 576) return;

        DepositModule.DataComponent component = getComponent().withCount(value);
        moduleStack.set(DepositModule.COMPONENT.get(), component);
    }

    private DepositModule.DataComponent getComponent() {
        return DepositModule.DataComponent.extract(moduleStack != null ? moduleStack : inventory.player.getMainHandItem());
    }

    private void updateComponentFromSlot() {
        ItemStack filter = filterContainer.getItem(0);
        moduleStack.set(DepositModule.COMPONENT.get(), getComponent().withFilter(filter));
    }

    public int getCount() {
        return getComponent().count();
    }
    public BlockPos getPos() {
        return getComponent().target().orElse(null);
    }
    public ResourceKey<Level> getDimension() {
        return getComponent().dimension().orElse(null);
    }

    @Override public boolean stillValid(@NotNull Player player) {
        return player.getMainHandItem() == moduleStack || player.getOffhandItem() == moduleStack;
    }

    @Override public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (stack == moduleStack) return ItemStack.EMPTY;
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