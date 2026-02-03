package xyz.agmstudio.neobots.containers;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.menus.NeoBotMenu;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

public abstract class BotFilteredContainer extends SimpleContainer {
    protected final NeoBotEntity bot;
    public BotFilteredContainer(NeoBotEntity bot, int size) {
        super(size);
        this.bot = bot;
    }

    /**
     * Creates the slot for the Menus using it
     */
    public NeoBotMenu.SlotCreator slotBuilder() {
        return (i, x, y) -> new BotFilteredSlot(this, i, x, y);
    }

    /** What items are allowed in this container */
    public abstract boolean isItemValid(ItemStack stack);

    /** How many slots are currently usable */
    public int getActiveSlots() {
        return getContainerSize();
    }

    /** Reads the tag and loads it */
    public void loadTag(@NotNull CompoundTag tag, String key, HolderLookup.@NotNull Provider access) {
        this.fromTag(tag.getList(key, 10), access);
    }

    /** Creates a tag holding data and saves it on the tag */
    public void saveTag(@NotNull CompoundTag tag, String key, HolderLookup.@NotNull Provider access) {
        tag.put(key, this.createTag(access));
    }

    @Override public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
        return index < getActiveSlots() && isItemValid(stack);
    }
    @Override public void setChanged() {
        super.setChanged();
        bot.setChanged();
    }

    public NeoBotEntity getBot() {
        return bot;
    }
}
