package xyz.agmstudio.neobots.block.charging_pad;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import xyz.agmstudio.neobots.index.CNBBlockEntities;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ChargingPadBlock extends KineticBlock implements IBE<ChargingPadBlockEntity> {
    public static void getRecipe(DataGenContext<Item, BlockItem> ctx, RegistrateRecipeProvider prov) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                .pattern("C C")
                .pattern(" A ")
                .pattern(" B ")
                .define('B', AllItems.BRASS_SHEET)
                .define('A', AllBlocks.ANDESITE_CASING)
                .define('C', Items.COPPER_INGOT)
                .unlockedBy("has_case", RegistrateRecipeProvider.has(AllBlocks.ANDESITE_CASING))
                .save(prov);
    }

    public ChargingPadBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == Direction.DOWN;
    }

    @Override
    public Class<ChargingPadBlockEntity> getBlockEntityClass() {
        return ChargingPadBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ChargingPadBlockEntity> getBlockEntityType() {
        return CNBBlockEntities.CHARGING_PAD.get();
    }
}