package xyz.agmstudio.neobots.modules.brass;

import com.simibubi.create.content.logistics.vault.ItemVaultBlockEntity;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.index.CNBItems;
import xyz.agmstudio.neobots.menus.TransferModuleMenu;
import xyz.agmstudio.neobots.modules.abstracts.data.ModuleTransferData;
import xyz.agmstudio.neobots.modules.abstracts.item.ModuleTier;
import xyz.agmstudio.neobots.modules.abstracts.item.TargetedModuleItem;
import xyz.agmstudio.neobots.modules.abstracts.task.ItemTransferTask;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

public class BrassDepositModule extends TargetedModuleItem<BrassDepositModule.Data, BrassDepositModule.Task> implements MenuProvider {
    public static void getRecipe(DataGenContext<Item, BrassDepositModule> ctx, RegistrateRecipeProvider prov) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                .pattern("RMR")
                .pattern("IBI")
                .pattern("RHR")
                .define('B', CNBItems.BRASS_MODULE_BASE)
                .define('M', CNBItems.ANDESITE_MOVE_TO_MODULE)
                .define('R', Items.REDSTONE)
                .define('I', Items.IRON_INGOT)
                .define('H', Items.HOPPER)
                .unlockedBy("has_base", RegistrateRecipeProvider.has(CNBItems.BRASS_MODULE_BASE))
                .save(prov);
    }

    private static final int REACH_SQR = 4;
    private static final int COOLDOWN = 4;

    public BrassDepositModule(Properties props) {
        super("brass_deposit", props, (bot, stack) -> new Task(bot, stack, REACH_SQR, COOLDOWN), Data::new, 2);
    }

    @Override public ModuleTier getTier() {
        return ModuleTier.BRASS;
    }

    @Override public boolean isValidTarget(@NotNull UseOnContext ctx, @NotNull BlockPos pos) {
        return !(ctx.getLevel().getBlockEntity(pos) instanceof ItemVaultBlockEntity) &&
                ctx.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, pos, ctx.getClickedFace()) != null;
    }

    @Override public @NotNull Component getDisplayName() {
        return Component.translatable("item.create_neobots.deposit_module");
    }

    @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return TransferModuleMenu.create(id, inv);
    }

    public static class Task extends ItemTransferTask.Advanced<Data> {
        public Task(NeoBotEntity bot, Data data, double reach, int cooldown) {
            super(bot, data, reach, cooldown);
        }

        @Override public String getType() {
            return "brass_deposit";
        }

        @Override public IItemHandler from() {
            return getInventory();
        }

        @Override public IItemHandler to() {
            return getTarget();
        }
    }
    public static class Data extends ModuleTransferData {
        public Data(Level level, @NotNull ItemStack stack) {
            super(level, stack);
        }
        @Override protected String getTranslateKey() {
            return "brass_deposit";
        }
    }
}