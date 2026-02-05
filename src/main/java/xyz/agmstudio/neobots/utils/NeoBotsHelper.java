package xyz.agmstudio.neobots.utils;

import com.simibubi.create.content.logistics.filter.FilterItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public interface NeoBotsHelper {
    static boolean matchesFilter(Level level, ItemStack stack, ItemStack filter) {
        if (filter.isEmpty()) return true;
        if (stack.isEmpty()) return false;

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

    static int moveItems(Level level, Container from, Container to, ItemStack filter, int count) {
        if (count <= 0) return 0;
        int moved = 0;

        for (int i = 0; i < from.getContainerSize(); i++) {
            ItemStack sourceStack = from.getItem(i);
            if (sourceStack.isEmpty()) continue;
            if (!NeoBotsHelper.matchesFilter(level, sourceStack, filter)) continue;

            moved += moveFromStack(sourceStack, to, count);
            if (sourceStack.isEmpty()) from.setItem(i, ItemStack.EMPTY);
            if (moved >= count) break;
        }

        if (moved > 0) from.setChanged();
        return moved;
    }

    static int moveFromStack(ItemStack source, Container to, int limit) {
        int moved = 0;

        for (int s = 0; s < to.getContainerSize() && moved < limit; s++) {
            ItemStack target = to.getItem(s);
            if (target.isEmpty()) continue;
            if (!ItemStack.isSameItemSameComponents(source, target)) continue;

            int space = target.getMaxStackSize() - target.getCount();
            if (space <= 0) continue;
            int move = Math.min(space, Math.min(source.getCount(), limit - moved));

            target.grow(move);
            source.shrink(move);
            moved += move;
        }

        for (int s = 0; s < to.getContainerSize() && moved < limit; s++) {
            ItemStack target = to.getItem(s);
            if (!target.isEmpty()) continue;

            int move = Math.min(source.getCount(), limit - moved);
            ItemStack placed = source.split(move);

            to.setItem(s, placed);
            moved += move;
        }

        if (moved > 0) to.setChanged();
        return moved;
    }
}