package xyz.agmstudio.neobots.modules.abstracts.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.modules.abstracts.task.ModuleTask;
import xyz.agmstudio.neobots.modules.abstracts.data.ModuleBlockPosData;
import xyz.agmstudio.neobots.modules.abstracts.data.ModuleData;

public abstract class TargetedModuleItem<D extends ModuleBlockPosData, T extends ModuleTask<D>> extends ModuleItem<D, T> {
    public TargetedModuleItem(String key, Properties properties, ModuleTask.Gen<D, T> taskGenerator, ModuleData.Gen<D> dataGenerator) {
        this(key, properties, taskGenerator, dataGenerator, 1);
    }
    public TargetedModuleItem(String key, Properties properties, ModuleTask.Gen<D, T> taskGenerator, ModuleData.Gen<D> dataGenerator, int lines) {
        super(key, properties, taskGenerator, dataGenerator, lines);
    }

    public boolean isValidTarget(@NotNull UseOnContext ctx,@NotNull BlockPos pos) {
        return true;
    }
    public BlockPos adjustBlockPos(@NotNull UseOnContext ctx,@NotNull  BlockPos pos) {
        return pos;
    }
    protected Component getTargetSetMessage() {
        return Component.translatable("module.create_neobots.abstract.target_set").withStyle(ChatFormatting.GREEN);
    }
    protected Component getInvalidTargetMessage() {
        return Component.translatable("module.create_neobots.abstract.invalid_target").withStyle(ChatFormatting.RED);
    }

    @Override public @NotNull InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide || !(ctx.getPlayer() instanceof ServerPlayer player))
            return InteractionResult.SUCCESS;

        BlockPos pos = adjustBlockPos(ctx, ctx.getClickedPos());
        if (!isValidTarget(ctx, pos)) {
            player.displayClientMessage(getInvalidTargetMessage(), true);
            return InteractionResult.PASS;
        }

        D data = getData(level, ctx.getItemInHand());
        data.setTarget(pos, level.dimension(), ctx.getClickedFace());
        data.save();

        player.displayClientMessage(getTargetSetMessage(), true);
        return InteractionResult.CONSUME;
    }
}