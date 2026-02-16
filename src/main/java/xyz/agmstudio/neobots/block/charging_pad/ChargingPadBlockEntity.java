package xyz.agmstudio.neobots.block.charging_pad;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

public class ChargingPadBlockEntity extends KineticBlockEntity {
    public NeoBotEntity owner = null;
    public ChargingPadBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override protected Block getStressConfigKey() {
        return AllBlocks.CRUSHING_WHEEL.get();
    }
    public NeoBotEntity getOwner() {
        return owner;
    }
    public void setOwner(NeoBotEntity owner) {
        this.owner = owner;
    }
}