package xyz.agmstudio.neobots.block.battery;

import com.simibubi.create.AllItems;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;


@ParametersAreNonnullByDefault
public class BatteryItem extends BlockItem {
    public static void getRecipe(DataGenContext<Item, BatteryItem> ctx, RegistrateRecipeProvider prov) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                .pattern(" N ")
                .pattern("ZRC")
                .pattern("ZRC")
                .define('Z', AllItems.ZINC_INGOT)
                .define('R', Items.REDSTONE)
                .define('C', Items.COPPER_INGOT)
                .define('N', AllItems.COPPER_NUGGET)
                .unlockedBy("has_copper", RegistrateRecipeProvider.has(Items.COPPER_INGOT))
                .unlockedBy("has_redstone", RegistrateRecipeProvider.has(Items.REDSTONE))
                .unlockedBy("has_zinc", RegistrateRecipeProvider.has(AllItems.ZINC_INGOT))
                .save(prov);
    }

    public static final int CAPACITY = 102_400;

    public BatteryItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override public boolean isBarVisible(ItemStack stack) {
        IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        return energy != null && energy.getEnergyStored() < energy.getMaxEnergyStored();
    }

    @Override public int getBarWidth(ItemStack stack) {
        IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        return energy == null ? 0 : (int) (13.F * energy.getEnergyStored() / energy.getMaxEnergyStored());
    }

    @Override public int getBarColor(ItemStack stack) {
        IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energy == null) return 0x000000;
        float maxEnergy = energy.getMaxEnergyStored();
        float f = Math.max(0.0F, (maxEnergy - (float) energy.getEnergyStored()) / maxEnergy);
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> components, TooltipFlag flags) {
        IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energy == null) return;
        components.add(Component.literal(energy.getEnergyStored() + "/" + energy.getMaxEnergyStored()));
    }
}