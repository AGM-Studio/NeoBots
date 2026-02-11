package xyz.agmstudio.neobots.block.charging_pad;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ChargingPadBlockEntity extends KineticBlockEntity {
    public ChargingPadBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override protected Block getStressConfigKey() {
        return AllBlocks.MECHANICAL_MIXER.get();
    }
}