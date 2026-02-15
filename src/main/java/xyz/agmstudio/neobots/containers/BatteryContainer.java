package xyz.agmstudio.neobots.containers;

import net.minecraft.world.item.ItemStack;
import xyz.agmstudio.neobots.block.battery.BatteryItem;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

public class BatteryContainer extends BotFilteredContainer {
    public BatteryContainer(NeoBotEntity bot, int size) {
        super(bot, size);
    }

    @Override public boolean isItemValid(ItemStack stack) {
        return stack.getItem() instanceof BatteryItem;
    }

    @Override public int getMaxStackSize() {
        return 1;
    }
}