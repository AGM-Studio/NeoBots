package xyz.agmstudio.neobots.modules;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.index.CNBItems;
import xyz.agmstudio.neobots.menus.TransferModuleMenu;
import xyz.agmstudio.neobots.modules.abstracts.ModuleTask;
import xyz.agmstudio.neobots.modules.abstracts.data.ModuleTransferData;
import xyz.agmstudio.neobots.modules.abstracts.item.TargetedModuleItem;
import xyz.agmstudio.neobots.robos.NeoBotCrash;
import xyz.agmstudio.neobots.robos.NeoBotEntity;
import xyz.agmstudio.neobots.utils.NeoBotsHelper;

public class DepositModule extends TargetedModuleItem<DepositModule.Data, DepositModule.Task> implements MenuProvider {
    public static void getRecipe(DataGenContext<Item, DepositModule> ctx, RegistrateRecipeProvider prov) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                .pattern("RIR")
                .pattern("IBI")
                .pattern("RHR")
                .define('B', CNBItems.BASE_MODULE)
                .define('R', Items.REDSTONE)
                .define('I', Items.IRON_INGOT)
                .define('H', Items.HOPPER)
                .unlockedBy("has_base", RegistrateRecipeProvider.has(CNBItems.BASE_MODULE))
                .save(prov);
    }

    private static final int REACH_SQR = 4;
    private static final int COOLDOWN = 4;

    public DepositModule(Properties props) {
        super("deposit", props, (bot, stack) -> new Task(bot, stack, REACH_SQR, COOLDOWN), Data::new);
    }

    @Override public boolean isValidTarget(@NotNull UseOnContext ctx, @NotNull BlockPos pos) {
        return ctx.getLevel().getBlockEntity(pos) instanceof Container;
    }

    @Override public @NotNull Component getDisplayName() {
        return Component.translatable("item.create_neobots.deposit_module");
    }

    @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return TransferModuleMenu.create(id, inv);
    }

    public static class Task extends ModuleTask<Data> {
        private final double reach;
        private final int cooldown;
        private boolean skipped = false;
        private int deposited = 0;
        private int tick = 0;


        public Task(NeoBotEntity bot, Data data, double reach, int cooldown) {
            super(bot, data);
            this.reach = reach;
            this.cooldown = cooldown;
        }

        @Override public String getType() {
            return "deposit";
        }

        @Override public void load(@NotNull CompoundTag tag) {
            this.deposited = tag.getInt("deposited");
            this.tick = tag.getInt("tick");
        }

        @Override public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("deposited", deposited);
            tag.putInt("tick", tick);
            return tag;
        }

        private void setDeposited(int value) {
            this.deposited = value;
            this.setDirty();
        }

        @Override public void onStart() {}

        @Override public void onStop() {}

        @Override public void onFinish() {}

        @Override public boolean isDone() {
            if (data.getTarget() == null || skipped) return true;
            return deposited >= data.getCount();
        }

        @Override public void tick() {
            IItemHandler handler = data.getHandler(bot, reach);
            if (handler == null) throw NeoBotCrash.INVENTORY_INACCESSIBLE;
            if (deposited >= data.getCount() || tick++ < cooldown) return;

            int remaining = data.getCount() - deposited;
            int moved = NeoBotsHelper.moveItems(bot.level(), bot.getInventory(), handler, data.getFilter(), remaining);
            if (moved > 0) setDeposited(deposited + moved);
            else skipped = data.getSkip();
            tick = 0;
        }

        @Override protected Object @NotNull [] getTranslateArgs() {
            return new Object[]{deposited, data.getCount()};
        }
    }
    public static class Data extends ModuleTransferData {
        public Data(Level level, @NotNull ItemStack stack) {
            super(level, stack);
        }
        @Override protected String getTranslateKey() {
            return "deposit";
        }
    }
}