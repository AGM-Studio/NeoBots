package xyz.agmstudio.neobots.upgrades;

import net.minecraft.world.item.ItemStack;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

public class MemoryUpgradeItem extends BotUpgradeItem {
    public MemoryUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override public void onInstalled(NeoBotEntity bot, ItemStack stack) {
        bot.recalculateModuleCapacity();
    }
    @Override public void onRemoved(NeoBotEntity bot, ItemStack stack) {
        bot.recalculateModuleCapacity();
    }
}
