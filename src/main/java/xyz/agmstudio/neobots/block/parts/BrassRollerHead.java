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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.index.CNBBlocks;
import xyz.agmstudio.neobots.index.CNBEntities;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

import javax.annotation.ParametersAreNonnullByDefault;


@ParametersAreNonnullByDefault
public class BrassRollerHead extends HorizontalDirectionalBlock {
    protected static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 8.0, 12.0);
    public static void getRecipe(DataGenContext<Item, BlockItem> ctx, RegistrateRecipeProvider prov) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                .pattern("PAP")
                .pattern("AGA")
                .pattern("PAP")
                .define('P', AllItems.PRECISION_MECHANISM)
                .define('A', AllBlocks.ANDESITE_CASING)
                .define('G', Items.GLASS)
                .unlockedBy("has_precision", RegistrateRecipeProvider.has(AllItems.PRECISION_MECHANISM))
                .save(prov);
    }

    public static final MapCodec<BrassRollerHead> CODEC = simpleCodec(BrassRollerHead::new);
    @Override protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }
    @Override protected @NotNull VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    public BrassRollerHead(Properties props) {
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

    @Override public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (level.isClientSide) return;

        BlockState b1 = level.getBlockState(pos.below());
        BlockState b2 = level.getBlockState(pos.below(2));

        if (b1.getBlock() == AllBlocks.BRASS_CASING.get() && b2.getBlock() == CNBBlocks.BRASS_WHEEL.get()) {
            NeoBotEntity bot = new NeoBotEntity(CNBEntities.BRASS_ROLLER.get(), level);
            bot.moveTo(pos.getX() + 0.5, pos.getY() - 1, pos.getZ() + 0.5, 0, 0);
            bot.readAdditionalSaveData(new CompoundTag());
            level.addFreshEntity(bot);

            level.removeBlock(pos, false);
            level.removeBlock(pos.below(), false);
            level.removeBlock(pos.below(2), false);
            level.levelEvent(2001, pos, Block.getId(state));
        }
    }
}