package xyz.agmstudio.neobots.upgrades;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import xyz.agmstudio.neobots.index.CNBItems;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

public abstract class MemoryUpgradeItem extends UpgradeItem {
    public MemoryUpgradeItem(Properties properties) {
        super(properties);
    }

    public abstract int getUpgradeSize();

    @Override public void onInstalled(NeoBotEntity bot, ItemStack stack) {
        bot.recalculateModuleCapacity();
    }
    @Override public void onRemoved(NeoBotEntity bot, ItemStack stack) {
        bot.recalculateModuleCapacity();
    }

    public static class Andesite extends MemoryUpgradeItem {
        public static void getRecipe(DataGenContext<Item, Andesite> ctx, RegistrateRecipeProvider prov) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                    .pattern(" B ")
                    .pattern("BUB")
                    .pattern(" B ")
                    .define('B', Items.BOOK)
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
    public static class Brass extends MemoryUpgradeItem {
        public static void getRecipe(DataGenContext<Item, Brass> ctx, RegistrateRecipeProvider prov) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                    .pattern(" B ")
                    .pattern("BUB")
                    .pattern(" B ")
                    .define('B', CNBItems.MEMORY_UPGRADE)
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