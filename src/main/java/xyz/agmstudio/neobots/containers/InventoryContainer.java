package xyz.agmstudio.neobots.containers;

import net.minecraft.world.item.ItemStack;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

public class InventoryContainer extends BotFilteredContainer {
    public InventoryContainer(NeoBotEntity bot, int size) {
        super(bot, size);
    }

    @Override public boolean isItemValid(ItemStack stack) {
        return true;
    }

    @Override
    public int getActiveSlots() {
        return bot.getInventoryCapacity();
    }
}