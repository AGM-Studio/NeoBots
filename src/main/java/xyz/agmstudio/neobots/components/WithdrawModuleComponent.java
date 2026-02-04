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

public record WithdrawModuleComponent(BlockPos source, ResourceKey<Level> dimension, int count, Optional<ItemStack> filter) {
    public static final Codec<WithdrawModuleComponent> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    BlockPos.CODEC.fieldOf("source").forGetter(WithdrawModuleComponent::source),
                    Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(WithdrawModuleComponent::dimension),
                    Codec.INT.fieldOf("count").forGetter(WithdrawModuleComponent::count),
                    ItemStack.CODEC.optionalFieldOf("filter").forGetter(WithdrawModuleComponent::filter)
            ).apply(instance, WithdrawModuleComponent::new));

    @Contract("_, _ -> new")
    public @NotNull WithdrawModuleComponent withSource(BlockPos source, ResourceKey<Level> dimension) {
        return new WithdrawModuleComponent(source, dimension, this.count, this.filter);
    }
    @Contract("_ -> new")
    public @NotNull WithdrawModuleComponent withDimension(ResourceKey<Level> dimension) {
        return new WithdrawModuleComponent(this.source, dimension, this.count, this.filter);
    }
    @Contract("_ -> new")
    public @NotNull WithdrawModuleComponent withCount(int count) {
        return new WithdrawModuleComponent(this.source, this.dimension, count, this.filter);
    }
    @Contract("_ -> new")
    public @NotNull WithdrawModuleComponent withFilter(@NotNull ItemStack filter) {
        return new WithdrawModuleComponent(this.source, this.dimension, this.count, filter.isEmpty() ? Optional.empty() : Optional.of(filter.copy()));
    }
}

