package xyz.agmstudio.neobots.utils;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;


@ParametersAreNonnullByDefault
public class NeoEntityDataAccessor<T> {
    private final EntityDataAccessor<T> accessor;
    private T lastValue;
    public NeoEntityDataAccessor(Class<? extends SyncedDataHolder> clazz, EntityDataSerializer<T> serializer) {
        this.accessor = SynchedEntityData.defineId(clazz, serializer);
    }
    public void build(SynchedEntityData.@NotNull Builder builder, T def) {
        builder.define(accessor, def);
        this.lastValue = def;
    }
    public void set(Entity entity, T value) {
        if (Objects.equals(lastValue, value)) return;
        entity.getEntityData().set(accessor, value);
        this.lastValue = value;
    }
    public T get(Entity entity) {
        return entity.getEntityData().get(accessor);
    }
}