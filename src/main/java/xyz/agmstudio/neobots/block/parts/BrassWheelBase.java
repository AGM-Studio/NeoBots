package xyz.agmstudio.neobots.block.parts;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllBlocks;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;


@ParametersAreNonnullByDefault
public class BrassWheelBase extends HorizontalDirectionalBlock {
    protected static final VoxelShape SHAPE_N = Block.box(3.0, 0.0, 3.0, 13.0, 6.0, 14.0);
    protected static final VoxelShape SHAPE_E = Block.box(2.0, 0.0, 3.0, 13.0, 6.0, 13.0);
    protected static final VoxelShape SHAPE_S = Block.box(3.0, 0.0, 2.0, 13.0, 6.0, 13.0);
    protected static final VoxelShape SHAPE_W = Block.box(3.0, 0.0, 3.0, 14.0, 6.0, 13.0);
    public static void getRecipe(DataGenContext<Item, BlockItem> ctx, RegistrateRecipeProvider prov) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                .pattern("CCC")
                .pattern("B B")
                .define('C', AllBlocks.BRASS_CASING)
                .define('B', AllBlocks.METAL_BRACKET)
                .unlockedBy("has_brass_casing", RegistrateRecipeProvider.has(AllBlocks.BRASS_CASING))
                .save(prov);
    }

    public static final MapCodec<BrassWheelBase> CODEC = simpleCodec(BrassWheelBase::new);
    @Override protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }
    @Override protected @NotNull VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext ctx) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SHAPE_S;
            case WEST -> SHAPE_W;
            case EAST -> SHAPE_E;
            default -> SHAPE_N;
        };
    }

    public BrassWheelBase(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        Direction nearest = context.getHorizontalDirection();
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown())
            return defaultBlockState().setValue(FACING, nearest);

        return defaultBlockState().setValue(FACING, nearest.getOpposite());
    }
}