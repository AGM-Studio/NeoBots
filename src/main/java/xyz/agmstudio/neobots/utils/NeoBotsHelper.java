package xyz.agmstudio.neobots.utils;

import com.simibubi.create.content.logistics.filter.FilterItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public interface NeoBotsHelper {
    static boolean matchesFilter(Level level, ItemStack stack, ItemStack filter) {
        if (stack.isEmpty()) return false;
        if (filter.isEmpty()) return true;

        Item filterItem = filter.getItem();
        if (filterItem instanceof FilterItem createFilter)
            return createFilter.makeStackWrapper(filter).test(level, stack);

        return FilterItem.testDirect(filter, stack, false);
    }

    Component STACKS_TERM = Component.literal("Stacks");
    Component STACK_TERM = Component.literal("Stack");
    static Component countAsStacks(int count) {
        if (count < 64) return Component.literal(count + "");
        int s = count / 64;
        int r = count % 64;
        Component term = s > 1 ? STACKS_TERM : STACK_TERM;
        if (r > 0) return Component.literal(s + " ").append(term).append(" + " + r);
        return Component.literal(s + " ").append(term);
    }
}