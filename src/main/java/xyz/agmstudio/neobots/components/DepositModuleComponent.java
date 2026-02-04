package xyz.agmstudio.neobots.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record DepositModuleComponent(BlockPos target, ResourceKey<Level> dimension, int count, Optional<ItemStack> filter) {
    public static final Codec<DepositModuleComponent> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    BlockPos.CODEC.fieldOf("target").forGetter(DepositModuleComponent::target),
                    Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(DepositModuleComponent::dimension),
                    Codec.INT.fieldOf("count").forGetter(DepositModuleComponent::count),
                    ItemStack.CODEC.optionalFieldOf("filter").forGetter(DepositModuleComponent::filter)
            ).apply(instance, DepositModuleComponent::new));

    @Contract("_, _ -> new")
    public @NotNull DepositModuleComponent withTarget(BlockPos source, ResourceKey<Level> dimension) {
        return new DepositModuleComponent(source, dimension, this.count, this.filter);
    }
    @Contract("_ -> new")
    public @NotNull DepositModuleComponent withCount(int count) {
        return new DepositModuleComponent(this.target, this.dimension, count, this.filter);
    }
    @Contract("_ -> new")
    public @NotNull DepositModuleComponent withFilter(@NotNull ItemStack filter) {
        return new DepositModuleComponent(this.target, this.dimension, this.count, filter.isEmpty() ? Optional.empty() : Optional.of(filter.copy()));
    }
}