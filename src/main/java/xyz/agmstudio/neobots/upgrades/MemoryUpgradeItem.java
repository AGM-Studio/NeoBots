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

public class MemoryUpgradeItem extends BotUpgradeItem {
    public static void getRecipe(DataGenContext<Item, MemoryUpgradeItem> ctx, RegistrateRecipeProvider prov) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                .pattern(" B ")
                .pattern("BUB")
                .pattern(" B ")
                .define('B', Items.BOOK)
                .define('U', CNBItems.BASE_UPGRADE)
                .unlockedBy("has_upgrade_base", RegistrateRecipeProvider.has(CNBItems.BASE_UPGRADE))
                .save(prov);
    }

    public MemoryUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override public void onInstalled(NeoBotEntity bot, ItemStack stack) {
        bot.recalculateModuleCapacity();
    }
    @Override public void onRemoved(NeoBotEntity bot, ItemStack stack) {
        bot.recalculateModuleCapacity();
    }
}