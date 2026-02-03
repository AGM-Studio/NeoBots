package xyz.agmstudio.neobots.containers;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

public abstract class BotFilteredContainer<T extends Item> extends SimpleContainer {
    protected final NeoBotEntity bot;
    private final Class<T> type;
    public BotFilteredContainer(NeoBotEntity bot, int size, Class<T> type) {
        super(size);
        this.bot = bot;
        this.type = type;
    }

    /** What items are allowed in this container */
    public boolean isItemValid(ItemStack stack) {
        return stack.getItem().getClass().isAssignableFrom(type);
    }

    /** How many slots are currently usable */
    public int getActiveSlots() {
        return getContainerSize();
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
