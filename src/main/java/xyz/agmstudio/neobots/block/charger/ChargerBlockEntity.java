package xyz.agmstudio.neobots.block.charger;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.agmstudio.neobots.block.charging_pad.ChargingPadBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;


@ParametersAreNonnullByDefault
public class ChargerBlockEntity extends BlockEntity implements MenuProvider {
    @SuppressWarnings("FieldCanBeLocal")
    private final float efficiency = 1.0f;
    protected final SimpleContainer inventory = new SimpleContainer(1) {
        @Override public int getMaxStackSize() {
            return 1;
        }

        @Override public boolean canPlaceItem(int i, ItemStack stack) {
            return stack.getCapability(Capabilities.EnergyStorage.ITEM) != null;
        }
    };

    public ChargerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        ItemStack battery = inventory.getItem(0);
        IEnergyStorage energyStorage = battery.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energyStorage == null) return;
        if (!(level.getBlockEntity(getBlockPos().below()) instanceof ChargingPadBlockEntity pad)) return;
        float speed = pad.getSpeed() * efficiency;
        int received = energyStorage.receiveEnergy((int) speed, false);
        if (received > 0) inventory.setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntity be) {
        if (be instanceof ChargerBlockEntity cbe) cbe.tick();
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ItemStack battery = inventory.getItem(0);
        if (battery.isEmpty()) tag.remove("battery");
        else tag.put("item", battery.save(registries));
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.setItem(0, ItemStack.parseOptional(registries, tag.getCompound("item")));
    }

    @Override public @NotNull Component getDisplayName() {
        return Component.literal("Charger");
    }

    @Override public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new ChargerMenu(id, inv, this);
    }
}