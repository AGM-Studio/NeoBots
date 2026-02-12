package xyz.agmstudio.neobots.block.charging_pad;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import xyz.agmstudio.neobots.index.CNBBlockEntities;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ChargingPadBlock extends KineticBlock implements IBE<ChargingPadBlockEntity> {
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