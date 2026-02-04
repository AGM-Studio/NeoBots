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
    class ModuleItem extends BotModuleItem {
        public ModuleItem(Properties props) {
            super(props);
        }
    
        @Override public void onStart(NeoBotEntity bot, ItemStack stack) {
            DataComponent target = stack.get(COMPONENT.get());
            if (target == null || target.dimension() != bot.level().dimension()) return;
    
            Vec3 pos = Vec3.atCenterOf(target.pos()).add(0, -0.5, 0);
            bot.getNavigation().moveTo(pos.x, pos.y, pos.z, 0, 1.0);
        }
    
        @Override
        public void tick(NeoBotEntity bot, ItemStack stack) {
            DataComponent target = stack.get(COMPONENT.get());
            if (target == null) return;
            if (!bot.getNavigation().isDone()) return;
            // Manually move to exact position
            Vec3 center = Vec3.atCenterOf(target.pos()).add(0, -0.5, 0);
            Vec3 delta = center.subtract(bot.position());
            double distance = delta.lengthSqr();
            if (delta.lengthSqr() < 0.02) {
                bot.setDeltaMovement(Vec3.ZERO);
                return;
            }
    
            Vec3 dir = delta.normalize();
            double speed = bot.getAttributeValue(Attributes.MOVEMENT_SPEED) / 2.0;
            double maxStep = Math.min(speed, Math.sqrt(distance));
    
            Vec3 motion = dir.scale(maxStep);
            bot.setDeltaMovement(motion.x, bot.getDeltaMovement().y, motion.z);
        }
    
        @Override public boolean isFinished(NeoBotEntity bot, ItemStack stack) {
            DataComponent target = stack.get(COMPONENT.get());
            if (target == null) return true;
    
            Vec3 center = Vec3.atCenterOf(target.pos()).add(0, -0.5, 0);
            return bot.position().distanceToSqr(center) < 0.02;
        }
    
        @Override public void onStop(NeoBotEntity bot, ItemStack stack) {
            bot.level().playSound(null, bot.blockPosition(), AllSoundEvents.STEAM.getMainEventHolder().value(), SoundSource.NEUTRAL);
        }
    
        // Configura
        @Override public @NotNull InteractionResult useOn(UseOnContext ctx) {
            if (ctx.getLevel().isClientSide)
                return InteractionResult.SUCCESS;
    
            BlockPos pos = ctx.getClickedPos().above();
            DataComponent target = new DataComponent(pos, ctx.getLevel().dimension());
    
            ctx.getItemInHand().set(COMPONENT.get(), target);
            if (ctx.getPlayer() instanceof ServerPlayer player) player.displayClientMessage(
                    Component.literal("§aMove target set to: §f" +
                            pos.getX() + ", " + pos.getY() + ", " + pos.getZ()),
                    true
            );
    
            return InteractionResult.CONSUME;
        }
    
        @Override public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext ctx, @NotNull List<Component> components, @NotNull TooltipFlag flags) {
            DataComponent target = stack.get(COMPONENT.get());
    
            if (target != null) {
                components.add(Component.literal("Target:")
                        .withStyle(ChatFormatting.GRAY));
                components.add(Component.literal(
                        target.pos().getX() + ", " +
                                target.pos().getY() + ", " +
                                target.pos().getZ()
                ).withStyle(ChatFormatting.AQUA));
            } else {
                components.add(Component.literal("No target set")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }
}