package xyz.agmstudio.neobots.modules.abstracts;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.modules.abstracts.data.ModuleData;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

import java.util.List;
import java.util.Objects;

public abstract class ModuleItem<D extends ModuleData, T extends ModuleTask<D>> extends Item {
    private final ModuleTask.Gen<D, T> taskGenerator;
    private final ModuleData.Gen<D> dataGenerator;

    public static boolean isModule(@NotNull ItemStack stack) {
        return stack.getItem() instanceof ModuleItem;
    }

    public ModuleItem(Properties properties, ModuleTask.Gen<D, T> taskGenerator, ModuleData.Gen<D> dataGenerator) {
        super(properties);
        this.taskGenerator = taskGenerator;
        this.dataGenerator = dataGenerator;
    }

    /** Generates a task for the bot to be executed (Uses load to automatically load the data)*/
    public T getTask(NeoBotEntity bot, ItemStack stack) {
        T task = taskGenerator.generate(bot, getData(bot.level(), stack));
        CompoundTag tag = bot.getTaskData();
        if (tag != null && Objects.equals(task.id, tag.getString("id"))) task.load(tag);
        return task;
    }

    /** Generates a data class for this module */
    public D getData(Level level, ItemStack stack) {
        return dataGenerator.generate(level, stack);
    }

    @Override public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext ctx, @NotNull List<Component> tooltip, @NotNull TooltipFlag flags) {
        getData(ctx.level(), stack).addTooltip(tooltip);
    }
}