package xyz.agmstudio.neobots.block.battery;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;


@ParametersAreNonnullByDefault
public class BatteryItem extends BlockItem {
    public static final int CAPACITY = 131_072;

    public BatteryItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override public boolean isBarVisible(ItemStack stack) {
        IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        return energy != null && energy.getEnergyStored() < energy.getMaxEnergyStored();
    }

    @Override public int getBarWidth(ItemStack stack) {
        IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        return energy == null ? 0 : (int) (13.F * energy.getEnergyStored() / energy.getMaxEnergyStored());
    }

    @Override public int getBarColor(ItemStack stack) {
        IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energy == null) return 0x000000;
        float maxEnergy = energy.getMaxEnergyStored();
        float f = Math.max(0.0F, (maxEnergy - (float) energy.getEnergyStored()) / maxEnergy);
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> components, TooltipFlag flags) {
        IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energy == null) return;
        components.add(Component.literal(energy.getEnergyStored() + "/" + energy.getMaxEnergyStored()));
    }
}