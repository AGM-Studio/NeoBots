package xyz.agmstudio.neobots.menus.modules;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.menus.AbstractMenu;
import xyz.agmstudio.neobots.modules.WithdrawModule;
import xyz.agmstudio.neobots.utils.NeoBotsHelper;

public class WithdrawModuleMenu extends AbstractMenu {
    private static final ResourceLocation BG =
            ResourceLocation.fromNamespaceAndPath(NeoBots.MOD_ID, "textures/gui/one_slot_panel.png");

    private final ItemStack moduleStack;
    private final SimpleContainer filterContainer;

    // GUI Variables - Not synced!
    private int count;

    public WithdrawModuleMenu(int id, Inventory inv, FriendlyByteBuf ignored) {
        this(id, inv);
    }
    public WithdrawModuleMenu(int id, Inventory inv) {
        super(WithdrawModule.MENU.get(), id, inv);

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

        WithdrawModule.DataComponent data = getComponent();
        data.filter().ifPresent(filter -> filterContainer.setItem(0, filter.copy()));
        count = data.count();

        this.addSlot(new Slot(filterContainer, 0, 26, 48));
        addPlayerInventory(8, 92,  moduleStack);

        // Setup GUI
        addScrollInput(51, 51, 96, 10).withRange(1, 577)
                .setState(getCount())
                .titled(Component.literal("Count"))
                .calling(value -> {
                    count = value;
                    sendInventoryClickPacket(this.count);
                });

        addTitleCentered(4).withColor(0x582424);
        addLabel(s -> NeoBotsHelper.countAsStacks(count), 54, 52).withColor(0xffffff).withShadow();

        int targetColor = 0x990000;
        Component target = Component.literal("Right click to set target");
        if (getPos() != null) {
            targetColor = 0xffffff;
            target = inventory.player.level().getBlockState(getPos()).getBlock().getName()
                    .append(Component.literal(" (" + getPos().toShortString() + ")"));
        }
        addLabel(target, 30, 28).withColor(targetColor).withShadow();
    }

    private WithdrawModule.DataComponent getComponent() {
        return WithdrawModule.DataComponent.extract(moduleStack != null ? moduleStack : inventory.player.getMainHandItem());
    }

    private void updateComponentFromSlot() {
        ItemStack filter = filterContainer.getItem(0);

        WithdrawModule.DataComponent component = getComponent().withFilter(filter);
        moduleStack.set(WithdrawModule.COMPONENT.get(), component);
    }

    public int getCount() {
        return getComponent().count();
    }
    public BlockPos getPos() {
        return getComponent().source().orElse(null);
    }
    public ResourceKey<Level> getDimension() {
        return getComponent().dimension().orElse(null);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return player.getMainHandItem() == moduleStack || player.getOffhandItem() == moduleStack;
    }

    @Override public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (stack == moduleStack) return ItemStack.EMPTY;

        if (index == 0) {
            if (!moveItemStackTo(stack, 1, 37, true))
                return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(stack, 0, 1, false))
                return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        slot.onTake(player, stack);
        return copy;
    }

    @Override public boolean clickMenuButton(@NotNull Player player, int id) {
        if (id < 1 || id > 64) return false;

        WithdrawModule.DataComponent component = getComponent().withCount(id);
        moduleStack.set(WithdrawModule.COMPONENT.get(), component);
        return true;
    }

    @Override protected ResourceLocation getBackground() {
        return BG;
    }
    @Override protected int getWidth() {
        return 176;
    }
    @Override protected int getHeight() {
        return 174;
    }
}