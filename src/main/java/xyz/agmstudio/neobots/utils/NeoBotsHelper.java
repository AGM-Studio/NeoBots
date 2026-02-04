package xyz.agmstudio.neobots.utils;

import com.simibubi.create.content.logistics.filter.FilterItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface NeoBotsHelper {
    static boolean matchesFilter(Level level, ItemStack stack, ItemStack filter) {
        if (stack.isEmpty()) return false;
        if (filter.isEmpty()) return true;

        Item filterItem = filter.getItem();
        if (filterItem instanceof FilterItem createFilter)
            return createFilter.makeStackWrapper(filter).test(level, stack);

        return FilterItem.testDirect(filter, stack, false);
    }
}