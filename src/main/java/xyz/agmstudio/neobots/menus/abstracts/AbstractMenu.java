package xyz.agmstudio.neobots.menus.abstracts;

import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import net.createmod.catnip.gui.element.ScreenElement;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.agmstudio.neobots.containers.slotgroups.SlotCreator;
import xyz.agmstudio.neobots.containers.slotgroups.SlotGroup;
import xyz.agmstudio.neobots.containers.slotgroups.SlotGroupHolder;
import xyz.agmstudio.neobots.containers.slots.FilterSlot;
import xyz.agmstudio.neobots.containers.slots.NeoSlot;
import xyz.agmstudio.neobots.menus.gui.Drawable;
import xyz.agmstudio.neobots.menus.gui.FrameTexture;
import xyz.agmstudio.neobots.menus.gui.Label;
import xyz.agmstudio.neobots.menus.gui.Texture;
import xyz.agmstudio.neobots.network.MenuPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractMenu extends AbstractContainerMenu {
    protected static final Texture SLOT_TEXTURE = new Texture("textures/gui/single_slot.png", 20, 20);
    protected static final Texture ACTIVE_SLOT_TEXTURE = new Texture("textures/gui/single_slot_active.png", 20 , 20);
    protected static final Texture UPGRADE_SLOT_TEXTURE = new Texture("textures/gui/upgrade_slot.png", 20, 20);
    protected static final Texture PACKAGE_SLOT_TEXTURE = new Texture("textures/gui/package_slot.png", 18, 18);
    protected static final FrameTexture BRASS_FRAME = new FrameTexture("textures/gui/brass_frame.png", 64, 64).margin(19, 6).tiled(true);
    protected static final FrameTexture PACKAGE_FRAME = new FrameTexture("textures/gui/package_frame.png", 80, 72).margin(11, 12);

    protected final List<Consumer<AbstractScreen<?>>> onInitActions = new ArrayList<>();
    protected final List<Supplier<Drawable.Drawer>> drawers = new ArrayList<>();
    protected final List<WidgetHolder<?>> widgets = new ArrayList<>();
    protected final List<SlotGroup> slotGroups = new ArrayList<>();
    protected final List<SlotGroupHolder> slotHolders = new ArrayList<>();
    protected final List<Supplier<Label>> labels = new ArrayList<>();
    protected final Inventory inventory;

    protected SlotGroupHolder playerInventoryGroup = null;

    protected AbstractMenu(@Nullable MenuType<?> type, int id, Inventory inv) {
        super(type, id);
        this.inventory = inv;
    }

    protected SlotGroup addSlotGroup(Container inv, int w, int h, int x, int y) {
        SlotGroup group = new SlotGroup(inv, w, h, x, y);
        this.slotGroups.add(group);
        return group;
    }
    protected void addPlayerInventory(int x, int y) {
        addPlayerInventory(x, y, 0, 4, 18, null);
    }
    protected void addPlayerInventory(int x, int y, ItemStack lockedStack) {
        addPlayerInventory(x, y, 0, 4, 18, SlotCreator.lockedSlotCreator(inventory, lockedStack));
    }
    protected void addPlayerInventory(int x, int y, int slotPadding, int hotbarPadding, int textureSize) {
        addPlayerInventory(x, y, slotPadding, hotbarPadding, textureSize, null);
    }
    protected void addPlayerInventory(int x, int y, int slotPadding, int hotbarPadding, int textureSize, SlotCreator<? extends NeoSlot> creator) {
        if (creator == null) creator = SlotCreator.defaultCreator(inventory);
        playerInventoryGroup = addSlotGroup(inventory, 9, 3, x, y).offset(9).pad(slotPadding).withTextureSize(textureSize, textureSize).withSlotCreator(creator)
                .then(9, 1, x, y + hotbarPadding + 2 * slotPadding + 54).limit(9).build(this);
    }

    protected SlotGroupHolder findGroup(int index) {
        return slotHolders.stream().filter(g -> g.containsIndex(index)).findFirst().orElseThrow();
    }

    protected boolean moveTo(@NotNull SlotGroupHolder group, ItemStack stack, boolean reverse) {
        return moveItemStackTo(stack, group.firstIndex(), group.lastIndexExclusive(), reverse);
    }

    protected Label addLabel(Function<AbstractScreen<?>, Component> text, int x, int y) {
        Label label = new Label(text, x, y);
        this.labels.add(() -> label);
        return label;
    }
    protected Label addLabel(Component text, int x, int y) {
        return addLabel(s -> text, x, y);
    }
    public void addLabel(Supplier<Label> label) {
        this.labels.add(label);
    }
    protected Label addTitle(int x, int y) {
        return addLabel(AbstractScreen::getTitle, x, y);
    }
    protected Label addTitleCentered(int y) {
        return addLabel(AbstractScreen::getTitle, getWidth() / 2, y).centered();
    }
    protected Label addPlayerInventoryTitle(int x, int y) {
        return addLabel(AbstractScreen::getPlayerInventoryTitle, x, y);
    }

    protected ScrollInput addScrollInput(int x, int y, int w, int h) {
        ScrollInput input = new ScrollInput(x, y, w, h);
        this.widgets.add(new WidgetHolder<>(input, x, y));
        return input;
    }
    protected Button addButton(String text, Button.OnPress onPress, int x, int y, int w, int h) {
        return addButton(Component.translatable(text), onPress, x, y, w, h);
    }
    protected Button addButton(Component text, Button.OnPress onPress, int x, int y, int w, int h) {
        Button button = Button.builder(text, onPress).pos(x, y).size(w, h).build();
        this.widgets.add(new  WidgetHolder<>(button, x, y));
        return button;
    }

    protected IconButton addIconButton(int x, int y, ScreenElement element) {
        IconButton button = new IconButton(x, y, element);
        this.widgets.add(new WidgetHolder<>(button, x, y));
        return button;
    }
    protected void updateIconButtons() {
        for (WidgetHolder<?> widget: widgets)
            if (widget.get() instanceof IconButton button) button.green = isIconButtonActive(button);
    }
    protected boolean isIconButtonActive(IconButton button) {
        return false;
    }

    public void addTextureDrawer(Supplier<Drawable.Drawer> drawer) {
        this.drawers.add(drawer);
    }
    public void addTextureDrawer(Drawable.Drawer drawer) {
        this.drawers.add(() -> drawer);
    }
    public void addInitListener(Consumer<AbstractScreen<?>> consumer) {
        this.onInitActions.add(consumer);
    }

    protected abstract Texture getBackground();
    public int getWidth() {
        return getBackground().sizeX;
    }
    public int getHeight() {
        return getBackground().sizeY;
    }
    public void registerSlotGroup(SlotGroupHolder holder) {
        slotHolders.add(holder);
    }

    @Override
    public void clicked(int id, int dragType, @NotNull ClickType clickType, @NotNull Player player) {
        if (id >= 0 && id < slots.size()) {
            if (slots.get(id) instanceof FilterSlot filterSlot) {
                ItemStack carried = getCarried();
                if (!carried.isEmpty()) filterSlot.set(carried);
                else filterSlot.set(ItemStack.EMPTY);
                return;
            }
        }

        super.clicked(id, dragType, clickType, player);
    }

    // Packet Handlers
    public void handlePacket(int id, int value) {}
    public void handlePacket(int id, double value) {}
    public void handlePacket(int id, boolean value) {}
    public void handlePacket(int id, String value) {}

    protected void sendPacket(int id, int value) {
        PacketDistributor.sendToServer(new MenuPacket.IntegerPayload(id, value));
    }
    protected void sendPacket(int id, double value) {
        PacketDistributor.sendToServer(new MenuPacket.DoublePayload(id, value));
    }
    protected void sendPacket(int id, boolean value) {
        PacketDistributor.sendToServer(new MenuPacket.BooleanPayload(id, value));
    }
    protected void sendPacket(int id, String value) {
        PacketDistributor.sendToServer(new MenuPacket.StringPayload(id, value));
    }
}