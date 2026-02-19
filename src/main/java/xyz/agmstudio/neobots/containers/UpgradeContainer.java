package xyz.agmstudio.neobots.containers;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.robos.NeoBotEntity;
import xyz.agmstudio.neobots.upgrades.UpgradeItem;

public class UpgradeContainer extends BotFilteredContainer {
    public UpgradeContainer(NeoBotEntity bot, int size) {
        super(bot, size);
    }

    @Override public boolean isItemValid(ItemStack stack) {
        return stack.getItem() instanceof UpgradeItem;
    }

    @Override public void setItem(int slot, @NotNull ItemStack stack) {
        ItemStack previous = getItem(slot);
        if (!previous.isEmpty() && previous.getItem() instanceof UpgradeItem old) old.onRemoved(bot, previous);

        super.setItem(slot, stack);

        if (!stack.isEmpty() && stack.getItem() instanceof UpgradeItem upgrade) upgrade.onInstalled(bot, stack);
    }

    @Override public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack removed = super.removeItem(slot, amount);
        if (!removed.isEmpty() && removed.getItem() instanceof UpgradeItem upgrade) upgrade.onRemoved(bot, removed);

        return removed;
    }

    @Override public @NotNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = super.removeItemNoUpdate(slot);
        if (!removed.isEmpty() && removed.getItem() instanceof UpgradeItem upgrade) upgrade.onRemoved(bot, removed);

        return removed;
    }

    @Override public int getMaxStackSize() {
        return 1;
    }
}