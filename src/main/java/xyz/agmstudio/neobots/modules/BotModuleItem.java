package xyz.agmstudio.neobots.modules;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

import java.util.Objects;
import java.util.function.BiFunction;

public abstract class BotModuleItem<T extends BotTask> extends Item {
    private final BiFunction<NeoBotEntity, ItemStack, T> taskGenerator;

    public static boolean isModule(@NotNull ItemStack stack) {
        return stack.getItem() instanceof BotModuleItem;
    }

    public BotModuleItem(Properties properties, BiFunction<NeoBotEntity, ItemStack, T> taskGenerator) {
        super(properties);
        this.taskGenerator = taskGenerator;
    }

    /** Generates a task for the bot to be executed (Uses load to automatically load the data)*/
    public T getTask(NeoBotEntity bot, ItemStack stack) {
        T task = taskGenerator.apply(bot, stack);
        CompoundTag tag = bot.getTaskData();
        if (tag != null && Objects.equals(task.id, tag.getString("id"))) task.load(tag);
        return task;
    }

    /** The module cooldown after it's finished */
    public int getCooldown(NeoBotEntity bot, ItemStack stack) {
        return 50;
    }
}