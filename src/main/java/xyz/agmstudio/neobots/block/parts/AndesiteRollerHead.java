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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.index.CNBBlocks;
import xyz.agmstudio.neobots.index.CNBEntities;
import xyz.agmstudio.neobots.robos.roller.andesite.AndesiteRoller;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class AndesiteRollerHead extends HorizontalDirectionalBlock {
    protected static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 8.0, 12.0);
    public static void getRecipe(DataGenContext<Item, BlockItem> ctx, RegistrateRecipeProvider prov) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                .pattern(" N ")
                .pattern("AAA")
                .pattern(" A ")
                .define('A', AllBlocks.ANDESITE_CASING)
                .define('N', AllBlocks.ORANGE_NIXIE_TUBE)
                .unlockedBy("has_andesite", RegistrateRecipeProvider.has(AllBlocks.ANDESITE_CASING))
                .save(prov);
    }

    public static final MapCodec<BrassRollerHead> CODEC = simpleCodec(BrassRollerHead::new);
    @Override protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }
    @Override protected @NotNull VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    public AndesiteRollerHead(Properties props) {
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

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (level.isClientSide) return;

        Direction facing = state.getValue(FACING);
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();

        BlockPos casingPos = pos.below();
        BlockPos leftWheelPos = casingPos.relative(left);
        BlockPos rightWheelPos = casingPos.relative(right);

        BlockState casing = level.getBlockState(casingPos);
        BlockState leftWheel = level.getBlockState(leftWheelPos);
        BlockState rightWheel = level.getBlockState(rightWheelPos);

        if (
                casing.getBlock() == AllBlocks.ANDESITE_CASING.get()
                && leftWheel.getBlock() == CNBBlocks.ROLLER_WHEEL.get()
                && rightWheel.getBlock() == CNBBlocks.ROLLER_WHEEL.get()
                && leftWheel.getValue(DirectionalBlock.FACING) == left
                && rightWheel.getValue(DirectionalBlock.FACING) == right
        ) {
            AndesiteRoller bot = new AndesiteRoller(CNBEntities.ANDESITE_ROLLER.get(), level);
            bot.moveTo(casingPos.getX() + 0.5, casingPos.getY(), casingPos.getZ() + 0.5, facing.toYRot(), 0);
            bot.setYRot(facing.toYRot());
            bot.yBodyRot = facing.toYRot();
            bot.yHeadRot = facing.toYRot();
            level.addFreshEntity(bot);

            level.removeBlock(pos, false);
            level.removeBlock(casingPos, false);
            level.removeBlock(leftWheelPos, false);
            level.removeBlock(rightWheelPos, false);
            level.levelEvent(2001, pos, Block.getId(state));
        }
    }
}