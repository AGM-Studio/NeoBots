package xyz.agmstudio.neobots.modules;

import com.simibubi.create.AllSoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.modules.abstracts.ModuleItem;
import xyz.agmstudio.neobots.modules.abstracts.ModuleTask;
import xyz.agmstudio.neobots.modules.abstracts.data.ModuleBlockPosData;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

import java.util.List;

public class MoveToModule extends ModuleItem<MoveToModule.Data, MoveToModule.Task> {
    public static DeferredHolder<Item, MoveToModule> ITEM = NeoBots.registerItem("move_to_module", MoveToModule::new, 1);
    public static void register() {}


    public MoveToModule(Properties props) {
        super("move_to", props, Task::new, Data::new);
    }

    @Override public @NotNull InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getLevel().isClientSide)
            return InteractionResult.SUCCESS;

        BlockPos pos = ctx.getClickedPos().above();
        Data data = getData(ctx.getLevel(), ctx.getItemInHand());
        data.setTarget(pos, ctx.getLevel().dimension());
        data.save();

        if (ctx.getPlayer() instanceof ServerPlayer player) player.displayClientMessage(Component.literal("§aMove target set to: §f" + pos.toShortString()), true);

        return InteractionResult.CONSUME;
    }

    public static class Task extends ModuleTask<Data> {
        private final Vec3 target;
        public Task(NeoBotEntity bot, Data data) {
            super(bot, data);
            if (data.getTarget() == null) target = null;
            else target = Vec3.atCenterOf(data.getTarget()).add(0, -0.5, 0);
        }

        @Override public String getType() {
            return "move_to";
        }

        @Override public void onStart() {
            if (!data.isSameDimension(bot.level().dimension())) return;
            bot.getNavigation().moveTo(target.x, target.y, target.z, 0, 1.0);
        }

        @Override public void onStop() {
            bot.getNavigation().stop();
        }

        @Override public void onFinish() {
            bot.level().playSound(null, bot.blockPosition(), AllSoundEvents.STEAM.getMainEventHolder().value(), SoundSource.NEUTRAL);
        }

        @Override public boolean isDone() {
            if (target == null) return true;
            return bot.position().distanceToSqr(target) < 0.02;
        }

        @Override public void tick() {
            if (target == null) return;
            if (!bot.getNavigation().isDone()) return;
            // Manually move to exact position
            Vec3 delta = target.subtract(bot.position());
            if (delta.lengthSqr() < 0.02) {
                bot.setDeltaMovement(Vec3.ZERO);
                return;
            }

            double speed = bot.getAttributeValue(Attributes.MOVEMENT_SPEED) / 2.0;
            double maxStep = Math.min(speed, Math.sqrt(delta.lengthSqr()));

            Vec3 motion = delta.normalize().scale(maxStep);
            bot.setDeltaMovement(motion.x, bot.getDeltaMovement().y, motion.z);
        }

        @Override public Component getStatus() {
            if (data.getTarget() == null)
                return Component.literal("No target set").withStyle(ChatFormatting.DARK_GRAY);
            if (!data.isSameDimension(bot.level().dimension()))
                return Component.literal("Unable to find the target").withStyle(ChatFormatting.RED);
            if (!bot.getNavigation().isDone() || !isDone())
                return Component.literal("Moving to target").withStyle(ChatFormatting.YELLOW);

            return Component.literal("Arrived").withStyle(ChatFormatting.GREEN);
        }
    }
    public static class Data extends ModuleBlockPosData {
        protected Data(Level level, ItemStack stack) {
            super(level, stack);
        }

        @Override public int getCooldown() {
            return 50;
        }

        @Override public void addTooltip(@NotNull List<Component> tooltip, @NotNull TooltipContext ctx, @NotNull TooltipFlag flags) {
            if (target != null) {
                tooltip.add(Component.literal("Target:").withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal(target.toShortString()).withStyle(ChatFormatting.AQUA));
            } else
                tooltip.add(Component.literal("No target set").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}