package xyz.agmstudio.neobots.containers;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.robos.NeoBotEntity;
import xyz.agmstudio.neobots.upgrades.BotUpgradeItem;

public class UpgradeContainer extends BotFilteredContainer<BotUpgradeItem> {
    public UpgradeContainer(NeoBotEntity bot, int size) {
        super(bot, size, BotUpgradeItem.class);
    }

    @Override public void setItem(int slot, @NotNull ItemStack stack) {
        ItemStack previous = getItem(slot);
        if (!previous.isEmpty() && previous.getItem() instanceof BotUpgradeItem old) old.onRemoved(bot, previous);

        super.setItem(slot, stack);

        if (!stack.isEmpty() && stack.getItem() instanceof BotUpgradeItem upgrade) upgrade.onInstalled(bot, stack);
    }

    @Override public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack removed = super.removeItem(slot, amount);
        if (!removed.isEmpty() && removed.getItem() instanceof BotUpgradeItem upgrade) upgrade.onRemoved(bot, removed);

        return removed;
    }

    @Override public @NotNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = super.removeItemNoUpdate(slot);
        if (!removed.isEmpty() && removed.getItem() instanceof BotUpgradeItem upgrade) upgrade.onRemoved(bot, removed);

        return removed;
    }
}
