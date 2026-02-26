package xyz.agmstudio.neobots.modules.andesite;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.index.CNBItems;
import xyz.agmstudio.neobots.modules.abstracts.item.ModuleTier;
import xyz.agmstudio.neobots.modules.abstracts.task.ModuleTask;
import xyz.agmstudio.neobots.modules.abstracts.data.ModuleBlockPosData;
import xyz.agmstudio.neobots.modules.abstracts.item.TargetedModuleItem;
import xyz.agmstudio.neobots.robos.NeoBotCrash;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

import java.util.List;

public class AndesiteMoveToModule extends TargetedModuleItem<AndesiteMoveToModule.Data, AndesiteMoveToModule.Task> {
    public static void getRecipe(DataGenContext<Item, AndesiteMoveToModule> ctx, RegistrateRecipeProvider prov) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                .pattern("AAA")
                .pattern("CBC")
                .pattern("AAA")
                .define('B', CNBItems.ANDESITE_MODULE_BASE)
                .define('A', AllItems.ANDESITE_ALLOY)
                .define('C', AllBlocks.COGWHEEL)
                .unlockedBy("has_base", RegistrateRecipeProvider.has(CNBItems.ANDESITE_MODULE_BASE))
                .save(prov);
    }

    public AndesiteMoveToModule(Properties props) {
        super("move_to", props, Task::new, Data::new);
    }

    @Override public ModuleTier getTier() {
        return ModuleTier.ANDESITE;
    }

    @Override public boolean isValidTarget(@NotNull UseOnContext ctx, @NotNull BlockPos pos) {
        Level level = ctx.getLevel();
        BlockState state = level.getBlockState(pos);

        BlockPos standPos = pos.above();
        return level.getBlockState(standPos).isAir() && level.getBlockState(standPos.above()).isAir();
    }

    public static class Task extends ModuleTask<Data> {
        private final Vec3 target;
        public Task(NeoBotEntity bot, Data data) {
            super(bot, data);
            BlockPos pos = data.getTarget();
            if (pos == null) target = null;
            else {
                Level level = bot.level();
                BlockState state = level.getBlockState(pos);
                VoxelShape shape = state.getCollisionShape(level, pos);

                double height = shape.isEmpty() ? 0.0 : shape.max(Direction.Axis.Y);
                target = new Vec3(pos.getX() + 0.5, pos.getY() + height, pos.getZ() + 0.5);
            }
        }

        @Override public String getType() {
            return "move_to";
        }

        @Override public void onStart() {
            if (!data.isSameDimension(bot.level().dimension())) throw NeoBotCrash.TARGET_INACCESSIBLE;
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

        @Override
        protected Object @NotNull [] getTranslateArgs() {
            return new Object[]{data.getTarget().toShortString()};
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
            if (target != null)
                tooltip.add(Component.translatable("module.create_neobots.move_to.tooltip.target", target.toShortString()).withStyle(ChatFormatting.AQUA));
            else
                tooltip.add(Component.translatable("module.create_neobots.move_to.tooltip.no_target").withStyle(ChatFormatting.DARK_GRAY));
        }

        @Override protected String getTranslateKey() {
            return "move_to";
        }
    }
}