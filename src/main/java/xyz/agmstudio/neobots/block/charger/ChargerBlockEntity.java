package xyz.agmstudio.neobots.block.charger;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.agmstudio.neobots.block.charging_pad.ChargingPadBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;


@ParametersAreNonnullByDefault
public class ChargerBlockEntity extends SmartBlockEntity implements MenuProvider {
    @SuppressWarnings("FieldCanBeLocal")
    private final float efficiency = 1.0f;
    protected final SimpleContainer inventory = new SimpleContainer(1) {
        @Override public int getMaxStackSize() {
            return 1;
        }

        @Override public boolean canPlaceItem(int slot, ItemStack stack) {
            @Nullable IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
            return energy != null && energy.getMaxEnergyStored() > 0;
        }

        @Override public void setItem(int slot, ItemStack stack) {
            super.setItem(slot, stack);
            ChargerBlockEntity.this.setChanged();
        }

        @Override public void setChanged() {
            super.setChanged();
            ChargerBlockEntity.this.setChanged();
        }
    };

    public ChargerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    public void tick() {
        super.tick();

        if (level == null || level.isClientSide) return;

        ItemStack battery = inventory.getItem(0);
        IEnergyStorage energyStorage = battery.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energyStorage == null) return;
        if (!(level.getBlockEntity(getBlockPos().below()) instanceof ChargingPadBlockEntity pad)) return;
        float speed = pad.getSpeed() * efficiency;
        int received = energyStorage.receiveEnergy((int) speed, false);
        if (received > 0) inventory.setChanged();
    }

    @Override protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        ItemStack battery = inventory.getItem(0);
        if (!battery.isEmpty()) tag.put("item", battery.save(registries));
        super.write(tag, registries, clientPacket);
    }

    @Override protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        if (tag.contains("item")) inventory.setItem(0, ItemStack.parseOptional(registries, tag.getCompound("item")));
        else inventory.setItem(0, ItemStack.EMPTY);
        super.read(tag, registries, clientPacket);
    }

    @Override public @NotNull Component getDisplayName() {
        return Component.literal("Charger");
    }

    @Override public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ChargerMenu(id, inv, this);
    }
}