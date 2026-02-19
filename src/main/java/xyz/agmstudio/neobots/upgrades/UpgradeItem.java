package xyz.agmstudio.neobots.upgrades;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xyz.agmstudio.neobots.index.CNBItems;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

public abstract class UpgradeItem extends Item {
    public UpgradeItem(Properties properties) {
        super(properties);
    }

    public static void getAndesiteBaseRecipe(DataGenContext<Item, Item> ctx, RegistrateRecipeProvider prov) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get(), 4)
                .pattern("PPP")
                .pattern("PCP")
                .pattern("PPP")
                .define('P', AllItems.PRECISION_MECHANISM)
                .define('C', AllBlocks.ANDESITE_CASING)
                .unlockedBy("has_precision", RegistrateRecipeProvider.has(AllItems.PRECISION_MECHANISM))
                .save(prov);
    }
    public static void getBrassBaseRecipe(DataGenContext<Item, Item> ctx, RegistrateRecipeProvider prov) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get(), 4)
                .pattern("PUP")
                .pattern("UCU")
                .pattern("PUP")
                .define('P', AllItems.PRECISION_MECHANISM)
                .define('C', AllBlocks.BRASS_CASING)
                .define('U', CNBItems.ANDESITE_UPGRADE_BASE)
                .unlockedBy("has_precision", RegistrateRecipeProvider.has(AllItems.PRECISION_MECHANISM))
                .save(prov);
    }

    public void onInstalled(NeoBotEntity bot, ItemStack stack) {}
    public void onRemoved(NeoBotEntity bot, ItemStack stack) {}
    public void onBotTick(NeoBotEntity bot, ItemStack stack) {}
}