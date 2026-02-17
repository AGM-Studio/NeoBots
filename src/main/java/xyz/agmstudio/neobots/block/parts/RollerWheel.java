package xyz.agmstudio.neobots.block.parts;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;


@ParametersAreNonnullByDefault
public class RollerWheel extends Block {
    protected static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 4.0, 13.0);
    public static void getRecipe(DataGenContext<Item, BlockItem> ctx, RegistrateRecipeProvider prov) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                .pattern("KBK")
                .pattern("BSB")
                .pattern("KBK")
                .define('B', AllItems.BELT_CONNECTOR)
                .define('S', AllBlocks.SHAFT)
                .define('K', Items.DRIED_KELP)
                .unlockedBy("has_shaft", RegistrateRecipeProvider.has(AllBlocks.SHAFT))
                .save(prov);
    }

    @Override protected @NotNull VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    public RollerWheel(Properties props) {
        super(props);
    }
}