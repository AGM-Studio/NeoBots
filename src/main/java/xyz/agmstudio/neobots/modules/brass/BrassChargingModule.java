package xyz.agmstudio.neobots.modules.brass;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.block.charging_pad.ChargingPadBlockEntity;
import xyz.agmstudio.neobots.index.CNBItems;
import xyz.agmstudio.neobots.menus.ChargingModuleMenu;
import xyz.agmstudio.neobots.modules.abstracts.data.ModuleBlockPosData;
import xyz.agmstudio.neobots.modules.abstracts.item.ModuleTier;
import xyz.agmstudio.neobots.modules.abstracts.item.TargetedModuleItem;
import xyz.agmstudio.neobots.modules.abstracts.task.ModuleTask;
import xyz.agmstudio.neobots.robos.NeoBotCrash;
import xyz.agmstudio.neobots.robos.NeoBotEntity;
import xyz.agmstudio.neobots.utils.NeoBotsHelper;

import java.util.List;

public class BrassChargingModule extends TargetedModuleItem<BrassChargingModule.Data, BrassChargingModule.Task> implements MenuProvider {
    public static void getRecipe(DataGenContext<Item, BrassChargingModule> ctx, RegistrateRecipeProvider prov) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                .pattern("CRC")
                .pattern("RBR")
                .pattern("CMC")
                .define('B', CNBItems.BRASS_MODULE_BASE)
                .define('M', CNBItems.ANDESITE_MOVE_TO_MODULE)
                .define('C', Items.COPPER_INGOT)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_base", RegistrateRecipeProvider.has(CNBItems.ANDESITE_MODULE_BASE))
                .save(prov);
    }
    public BrassChargingModule(Properties props) {
        super("brass_charging", props, Task::new, Data::new);
    }

    @Override public ModuleTier getTier() {
        return ModuleTier.BRASS;
    }

    @Override public boolean isValidTarget(@NotNull UseOnContext ctx, @NotNull BlockPos pos) {
        return ctx.getLevel().getBlockEntity(pos) instanceof ChargingPadBlockEntity;
    }

    @Override public @NotNull Component getDisplayName() {
        return Component.translatable("item.create_neobots.brass_charging_module");
    }

    @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return ChargingModuleMenu.create(id, inv);
    }

    public static class Task extends ModuleTask<Data> {
        private final Vec3 target;
        private ChargingPadBlockEntity pad = null;
        private int chargeTick = 0;
        private boolean skip = false;
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
            return "brass_charging";
        }

        @Override public void onStart() {
            if (data.skip && 100.0 * bot.getEnergy() / bot.getTotalEnergy() > data.getSkipValue()) {
                skip = true;
                return;
            }
            if (!data.isSameDimension(bot.level().dimension())) throw NeoBotCrash.TARGET_INACCESSIBLE;
            if (bot.position().distanceTo(target) > 0.02) bot.getNavigation().moveTo(target.x, target.y, target.z, 0, 1.0);
        }

        @Override public void onStop() {
            if (pad != null) pad.setOwner(null);
        }

        @Override public void onFinish() {
            if (pad != null) pad.setOwner(null);
        }

        @Override public boolean isDone() {
            if (skip) return true;
            if (data.getMode() == 0)
                return 100.0 * bot.getEnergy() / bot.getTotalEnergy() > data.getValue();

            return chargeTick > data.getValue() * 20;
        }

        @Override public void tick() {
            if (target == null || skip) return;
            if (!bot.getNavigation().isDone()) return;
            Vec3 delta = target.subtract(bot.position());
            if (delta.lengthSqr() < 0.02) {
                bot.setDeltaMovement(Vec3.ZERO);
                if (pad == null) {
                    if (bot.level().getBlockEntity(bot.blockPosition().below()) instanceof ChargingPadBlockEntity cbe) this.pad = cbe;
                    else throw NeoBotCrash.CHARGER_NOT_FOUND;
                }
                if (pad.getOwner() == null) pad.setOwner(bot);
                else if (pad.getOwner() != bot) return;
                bot.chargeEnergy((int) pad.getSpeed());
                chargeTick++;
            } else {
                double speed = bot.getAttributeValue(Attributes.MOVEMENT_SPEED) / 2.0;
                double maxStep = Math.min(speed, Math.sqrt(delta.lengthSqr()));

                Vec3 motion = delta.normalize().scale(maxStep);
                bot.setDeltaMovement(motion.x, bot.getDeltaMovement().y, motion.z);
            }
        }

        @Override protected Object @NotNull [] getTranslateArgs() {
            if (data.getMode() == 0)
                return new Object[]{
                        (100 * bot.getEnergy() / bot.getTotalEnergy()) + "%",
                        data.getValue() + "%"
                };

            return new Object[]{
                    NeoBotsHelper.formatSeconds(chargeTick / 20),
                    NeoBotsHelper.formatSeconds(data.getValue())
            };
        }

        @Override protected @NotNull Component getOnGoingStatus() {
            if (!bot.getNavigation().isDone())
                return Component.translatable("module.create_neobots." + getType() + ".status.on_the_way", data.getTarget().toShortString()).withStyle(getStatusStyle());
            return super.getOnGoingStatus();
        }
    }
    public static class Data extends ModuleBlockPosData {
        private int value;
        private int mode;  // 0: Percent, 1: Seconds
        private boolean skip;
        private int skipValue;
        protected Data(Level level, ItemStack stack) {
            super(level, stack);
            this.mode = tag.getInt("mode");
            this.value = tag.getInt("value");
            if (mode == 0) this.value = Mth.clamp(this.value, 0, 100);
            else this.value = Math.max(this.value, 0);
            this.skip = tag.getBoolean("skip");
            this.skipValue = Mth.clamp(tag.getInt("skip_value"), 0, 100);
        }

        public int getValue() {
            return value;
        }
        public void setValue(int value) {
            if (mode == 0) this.value = Mth.clamp(value, 0, 100);
            else this.value = Math.max(value, 0);
            tag.putInt("value", this.value);
        }

        public int getMode() {
            return mode;
        }
        public void setMode(int mode) {
            this.mode = mode == 1 ? 1 : 0;
            tag.putInt("mode", this.mode);
            setValue(this.value);
        }

        public boolean getSkip() {
            return skip;
        }
        public void setSkip(boolean skip) {
            this.skip = skip;
            tag.putBoolean("skip", this.skip);
        }

        public int getSkipValue() {
            return skipValue;
        }
        public void setSkipValue(int skipValue) {
            this.skipValue = Mth.clamp(skipValue, 0, 100);
            tag.putInt("skip_value", this.skipValue);
        }

        @Override public int getCooldown() {
            return 10;
        }

        @Override public void addTooltip(@NotNull List<Component> tooltip, @NotNull TooltipContext ctx, @NotNull TooltipFlag flags) {
            String trans = "module.create_neobots." + getTranslateKey() + ".tooltip.";
            if (target != null) tooltip.add(Component.translatable(trans + "target", target.toShortString()).withStyle(ChatFormatting.AQUA));
            else tooltip.add(Component.translatable(trans + "no_target").withStyle(ChatFormatting.RED));
            tooltip.add(Component.translatable(trans + "mode." + mode, mode == 0 ? value + "%" : NeoBotsHelper.formatSeconds(value)));
            if (skip) tooltip.add(Component.translatable(trans + "skip", skipValue + "%"));
        }

        @Override protected String getTranslateKey() {
            return "brass_charging";
        }
    }
}