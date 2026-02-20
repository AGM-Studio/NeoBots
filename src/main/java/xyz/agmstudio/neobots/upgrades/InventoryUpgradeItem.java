package xyz.agmstudio.neobots.upgrades;

import com.simibubi.create.AllBlocks;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xyz.agmstudio.neobots.index.CNBItems;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

public abstract class InventoryUpgradeItem extends UpgradeItem {
    public InventoryUpgradeItem(Properties properties) {
        super(properties);
    }

    public abstract int getUpgradeSize();

    @Override public void onInstalled(NeoBotEntity bot, ItemStack stack) {
        bot.recalculateInventoryCapacity();
    }
    @Override public void onRemoved(NeoBotEntity bot, ItemStack stack) {
        bot.recalculateInventoryCapacity();
    }

    public static class Andesite extends InventoryUpgradeItem {
        public static void getRecipe(DataGenContext<Item, Andesite> ctx, RegistrateRecipeProvider prov) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                    .pattern(" C ")
                    .pattern("CUC")
                    .pattern(" C ")
                    .define('C', AllBlocks.CARDBOARD_BLOCK)
                    .define('U', CNBItems.ANDESITE_UPGRADE_BASE)
                    .unlockedBy("has_upgrade_base", RegistrateRecipeProvider.has(CNBItems.ANDESITE_UPGRADE_BASE))
                    .save(prov);
        }

        public Andesite(Properties properties) {
            super(properties);
        }

        @Override public int getUpgradeSize() {
            return 1;
        }
    }
    public static class Brass extends InventoryUpgradeItem {
        public static void getRecipe(DataGenContext<Item, Brass> ctx, RegistrateRecipeProvider prov) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                    .pattern(" B ")
                    .pattern("BUB")
                    .pattern(" B ")
                    .define('B', CNBItems.ANDESITE_INVENTORY_UPGRADE)
                    .define('U', CNBItems.BRASS_UPGRADE_BASE)
                    .unlockedBy("has_upgrade_base", RegistrateRecipeProvider.has(CNBItems.BRASS_UPGRADE_BASE))
                    .save(prov);
        }

        public Brass(Properties properties) {
            super(properties);
        }

        @Override public int getUpgradeSize() {
            return 2;
        }
    }
}