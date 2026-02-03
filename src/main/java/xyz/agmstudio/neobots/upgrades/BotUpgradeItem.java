package xyz.agmstudio.neobots.upgrades;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class BotUpgradeItem extends Item {
    public static boolean isUpgrade(@NotNull ItemStack stack) {
        return stack.getItem() instanceof BotUpgradeItem;
    }

    public BotUpgradeItem(Properties properties) {
        super(properties);
    }
}
