package xyz.agmstudio.neobots.modules;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.block.charging_pad.ChargingPadBlockEntity;
import xyz.agmstudio.neobots.index.CNBItems;
import xyz.agmstudio.neobots.modules.abstracts.ModuleTask;
import xyz.agmstudio.neobots.modules.abstracts.data.ModuleData;
import xyz.agmstudio.neobots.modules.abstracts.item.ModuleItem;
import xyz.agmstudio.neobots.robos.NeoBotCrash;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

import java.util.List;

public class ChargingModule extends ModuleItem<ChargingModule.Data, ChargingModule.Task> {
    public static void getRecipe(DataGenContext<Item, ChargingModule> ctx, RegistrateRecipeProvider prov) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                .pattern("CRC")
                .pattern("RBR")
                .pattern("CRC")
                .define('B', CNBItems.BASE_MODULE)
                .define('R', Items.REDSTONE)
                .define('C', Items.COPPER_INGOT)
                .unlockedBy("has_base", RegistrateRecipeProvider.has(CNBItems.BASE_MODULE))
                .save(prov);
    }
    public ChargingModule(Properties props) {
        super("charging", props, Task::new, Data::new);
    }

    public static class Task extends ModuleTask<Data> {
        private ChargingPadBlockEntity pad = null;
        public Task(NeoBotEntity bot, Data data) {
            super(bot, data);
        }

        @Override public String getType() {
            return "charging";
        }

        @Override public void onStart() {
            if (bot.level().getBlockEntity(bot.blockPosition().below()) instanceof ChargingPadBlockEntity cbe)
                this.pad = cbe;
            else throw NeoBotCrash.CHARGER_NOT_FOUND;
        }

        @Override public void onStop() {
            if (pad != null) pad.setOwner(null);
        }

        @Override public void onFinish() {
            if (pad != null) pad.setOwner(null);
        }

        @Override public boolean isDone() {
            return ((double) bot.getEnergy()) / bot.getTotalEnergy() > 0.95;
        }

        @Override public void tick() {
            if (pad == null) throw NeoBotCrash.CHARGER_NOT_FOUND;
            if (pad.getOwner() == null) pad.setOwner(bot);
            else if (pad.getOwner() != bot) return;
            bot.chargeEnergy((int) pad.getSpeed());
        }
    }
    public static class Data extends ModuleData {
        protected Data(Level level, ItemStack stack) {
            super(level, stack);
        }

        @Override public int getCooldown() {
            return 10;
        }

        @Override public void addTooltip(@NotNull List<Component> tooltip, @NotNull TooltipContext ctx, @NotNull TooltipFlag flags) {

        }
    }
}