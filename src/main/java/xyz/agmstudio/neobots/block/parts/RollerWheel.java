package xyz.agmstudio.neobots.block.parts;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;


@ParametersAreNonnullByDefault
public class RollerWheel extends DirectionalBlock {
    private final MapCodec<RollerWheel> CODEC = simpleCodec(RollerWheel::new);

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

    @Override protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case DOWN -> Block.box(3, 12, 3, 13, 16, 13);
            case NORTH -> Block.box(3, 3, 12, 13, 13, 16);
            case SOUTH -> Block.box(3, 3, 0, 13, 13, 4);
            case WEST -> Block.box(12, 3, 3, 16, 13, 13);
            case EAST -> Block.box(0, 3, 3, 4, 13, 13);
            default -> Block.box(3.0, 0.0, 3.0, 13.0, 4.0, 13.0);
        };
    }

    public RollerWheel(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override protected @NotNull MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getClickedFace());
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}