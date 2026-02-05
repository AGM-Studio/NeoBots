package xyz.agmstudio.neobots.modules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllSoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
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
import xyz.agmstudio.neobots.robos.NeoBotEntity;

import java.util.List;

public interface MoveToModule {
    DeferredHolder<DataComponentType<?>, DataComponentType<DataComponent>> COMPONENT = NeoBots.registerDataComponent("move_to_data", DataComponent.CODEC);
    DeferredHolder<Item, ModuleItem> ITEM = NeoBots.registerItem("move_to_module", ModuleItem::new, 1);

    static void register() {}
    
    record DataComponent(BlockPos pos, ResourceKey<Level> dimension) {
        public static final Codec<DataComponent> CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        BlockPos.CODEC.fieldOf("pos").forGetter(DataComponent::pos),
                        Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(DataComponent::dimension)
                ).apply(instance, DataComponent::new));
    }
    class ModuleItem extends BotModuleItem<ModuleTask> {
        public ModuleItem(Properties props) {
            super(props, ModuleTask::new);
        }

        @Override public @NotNull InteractionResult useOn(UseOnContext ctx) {
            if (ctx.getLevel().isClientSide)
                return InteractionResult.SUCCESS;
    
            BlockPos pos = ctx.getClickedPos().above();
            DataComponent target = new DataComponent(pos, ctx.getLevel().dimension());
    
            ctx.getItemInHand().set(COMPONENT.get(), target);
            if (ctx.getPlayer() instanceof ServerPlayer player) player.displayClientMessage(Component.literal("§aMove target set to: §f" + pos.toShortString()), true);
    
            return InteractionResult.CONSUME;
        }
    
        @Override public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext ctx, @NotNull List<Component> components, @NotNull TooltipFlag flags) {
            DataComponent target = stack.get(COMPONENT.get());
    
            if (target != null) {
                components.add(Component.literal("Target:").withStyle(ChatFormatting.GRAY));
                components.add(Component.literal(target.pos.toShortString()).withStyle(ChatFormatting.AQUA));
            } else
                components.add(Component.literal("No target set").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
    class ModuleTask extends BotTask {
        private final Vec3 target;
        private final DataComponent data;
        public ModuleTask(NeoBotEntity bot, ItemStack module) {
            super(bot, module);

            this.data = module.get(COMPONENT.get());
            if (this.data == null) target = null;
            else target = Vec3.atCenterOf(data.pos()).add(0, -0.5, 0);
        }

        @Override public String getType() {
            return "move_to";
        }

        @Override public void onStart() {
            if (data == null || data.dimension() != bot.level().dimension()) return;
            bot.getNavigation().moveTo(target.x, target.y, target.z, 0, 1.0);
        }

        @Override public void onStop() {
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
            DataComponent data = stack.get(COMPONENT.get());
            if (data == null)
                return Component.literal("No target set").withStyle(ChatFormatting.DARK_GRAY);
            if (data.dimension() != bot.level().dimension())
                return Component.literal("Unable to find the target").withStyle(ChatFormatting.RED);
            if (!bot.getNavigation().isDone() || !isDone())
                return Component.literal("Moving to target").withStyle(ChatFormatting.YELLOW);

            return Component.literal("Arrived").withStyle(ChatFormatting.GREEN);
        }
    }
}