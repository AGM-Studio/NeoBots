package xyz.agmstudio.neobots.block.battery;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.List;

public class BatteryBlockEntity extends SmartBlockEntity {
    private final EnergyStorage energy = new EnergyStorage(BatteryItem.CAPACITY);

    public BatteryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public IEnergyStorage getEnergy() {
        return energy;
    }

    @Override public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    @Override protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.putInt("energy", energy.getEnergyStored());
        super.write(tag, registries, clientPacket);
    }

    @Override protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        energy.receiveEnergy(tag.getInt("energy"), false);
        super.read(tag, registries, clientPacket);
    }
}