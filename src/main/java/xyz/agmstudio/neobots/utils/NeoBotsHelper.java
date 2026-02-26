package xyz.agmstudio.neobots.utils;

import com.simibubi.create.content.logistics.filter.FilterItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

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

    Component STACKS_TERM = Component.translatable("gui_term.create_neobots.stacks");
    Component STACK_TERM = Component.translatable("gui_term.create_neobots.stack");
    static String leadingZeros(int number, int length) {
        return String.format("%0" + length + "d", number);
    }
    static Component countAsStacks(int count) {
        if (count < 64) return Component.literal(count + "");
        int s = count / 64;
        int r = count % 64;
        Component term = s > 1 ? STACKS_TERM : STACK_TERM;
        if (r > 0) return Component.literal(s + " ").append(term).append(" + " + r);
        return Component.literal(s + " ").append(term);
    }
    static @NotNull Component formatSeconds(int value) {
        if (value < 60) return Component.literal(value + "s");
        if (value < 3600) return Component.literal(value / 60 + ":" + leadingZeros(value % 60, 2));
        return Component.literal(value / 3600 + ":" + leadingZeros(value % 3600 / 60, 2) + ":" + leadingZeros(value % 60, 2));
    }

    static int moveItems(Level level, Container from, IItemHandler to, ItemStack filter, int count) {
        return moveItems(level, new InvWrapper(from), to, filter, count);
    }
    static int moveItems(Level level, IItemHandler from, Container to, ItemStack filter, int count) {
        return moveItems(level, from, new InvWrapper(to), filter, count);
    }
    static int moveItems(Level level, Container from, Container to, ItemStack filter, int count) {
        return moveItems(level, new InvWrapper(from), new InvWrapper(to), filter, count);
    }
    static int moveItems(Level level, IItemHandler from, IItemHandler to, ItemStack filter, int count) {
        if (count <= 0) return 0;
        int moved = 0;

        for (int i = 0; i < from.getSlots() && moved < count; i++) {
            ItemStack stackInSlot = from.getStackInSlot(i);
            if (stackInSlot.isEmpty()) continue;
            if (!NeoBotsHelper.matchesFilter(level, stackInSlot, filter)) continue;

            int extractAmount = Math.min(stackInSlot.getCount(), count - moved);

            ItemStack extracted = from.extractItem(i, extractAmount, false);
            if (extracted.isEmpty()) continue;

            ItemStack remainder = ItemHandlerHelper.insertItem(to, extracted, false);

            int inserted = extracted.getCount() - remainder.getCount();
            moved += inserted;

            if (!remainder.isEmpty()) from.insertItem(i, remainder, false);
        }

        return moved;
    }
}