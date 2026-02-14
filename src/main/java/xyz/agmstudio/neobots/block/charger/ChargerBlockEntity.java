package xyz.agmstudio.neobots.block.charger;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.block.battery.BatteryItem;
import xyz.agmstudio.neobots.block.charging_pad.ChargingPadBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;


@ParametersAreNonnullByDefault
public class ChargerBlockEntity extends SmartBlockEntity {
    @SuppressWarnings("FieldCanBeLocal")
    private final float efficiency = 1.0f;
    protected final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.getItem() instanceof BatteryItem;
        }

        @Override protected void onContentsChanged(int slot) {
            notifyUpdate();
        }
    };

    public IItemHandler getInventory() {
        return inventory;
    }

    public ChargerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    public void tick() {
        super.tick();

        if (level == null || level.isClientSide) return;

        ItemStack battery = inventory.getStackInSlot(0);
        IEnergyStorage energyStorage = battery.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energyStorage == null) return;
        if (!(level.getBlockEntity(getBlockPos().below()) instanceof ChargingPadBlockEntity pad)) return;
        float speed = pad.getSpeed() * efficiency;
        int received = energyStorage.receiveEnergy((int) speed, false);
        if (received > 0) notifyUpdate();
    }

    @Override protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        ItemStack battery = inventory.getStackInSlot(0);
        if (!battery.isEmpty()) tag.put("item", battery.save(registries));
        super.write(tag, registries, clientPacket);
    }

    @Override protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        if (tag.contains("item")) inventory.setStackInSlot(0, ItemStack.parseOptional(registries, tag.getCompound("item")));
        else inventory.setStackInSlot(0, ItemStack.EMPTY);
        super.read(tag, registries, clientPacket);
    }
}