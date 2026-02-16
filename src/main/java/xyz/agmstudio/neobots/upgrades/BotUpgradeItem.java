package xyz.agmstudio.neobots.upgrades;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

public abstract class BotUpgradeItem extends Item {
    public static boolean isUpgrade(@NotNull ItemStack stack) {
        return stack.getItem() instanceof BotUpgradeItem;
    }
    public static void getBaseRecipe(DataGenContext<Item, Item> ctx, RegistrateRecipeProvider prov) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get(), 4)
                .pattern("PPP")
                .pattern("PAP")
                .pattern("PPP")
                .define('P', AllItems.PRECISION_MECHANISM)
                .define('A', AllBlocks.ANDESITE_CASING)
                .unlockedBy("has_precision", RegistrateRecipeProvider.has(AllItems.PRECISION_MECHANISM))
                .save(prov);
    }

    public BotUpgradeItem(Properties properties) {
        super(properties);
    }

    public void onInstalled(NeoBotEntity bot, ItemStack stack) {}
    public void onRemoved(NeoBotEntity bot, ItemStack stack) {}
    public void onBotTick(NeoBotEntity bot, ItemStack stack) {}
}