package xyz.agmstudio.neobots.upgrades;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

public abstract class BotUpgradeItem extends Item {
    public static boolean isUpgrade(@NotNull ItemStack stack) {
        return stack.getItem() instanceof BotUpgradeItem;
    }

    public BotUpgradeItem(Properties properties) {
        super(properties);
    }

    public void onInstalled(NeoBotEntity bot, ItemStack stack) {}
    public void onRemoved(NeoBotEntity bot, ItemStack stack) {}
    public void onBotTick(NeoBotEntity bot, ItemStack stack) {}
}
