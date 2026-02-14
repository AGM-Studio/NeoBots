package xyz.agmstudio.neobots.index;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.NeoBots;

public final class CNBDataComponents {
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, NeoBots.MOD_ID);

    private static <T> @NotNull DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, Codec<T> codec) {
        return COMPONENTS.register(name, () -> DataComponentType.<T>builder().persistent(codec).build());
    }
    private static <T> @NotNull DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return COMPONENTS.register(name, () -> DataComponentType.<T>builder().persistent(codec).networkSynchronized(streamCodec).build());
    }

    public static void register(IEventBus bus) {
        COMPONENTS.register(bus);
    }

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> MODULE_DATA = register("module_data",CompoundTag.CODEC, ByteBufCodecs.COMPOUND_TAG);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> BATTERY_DATA = register("energy", Codec.INT, ByteBufCodecs.INT);
}