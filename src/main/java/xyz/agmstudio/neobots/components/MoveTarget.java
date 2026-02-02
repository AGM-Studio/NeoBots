package xyz.agmstudio.neobots.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record MoveTarget(BlockPos pos, ResourceKey<Level> dimension) {
    public static final Codec<MoveTarget> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    BlockPos.CODEC.fieldOf("pos").forGetter(MoveTarget::pos),
                    Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(MoveTarget::dimension)
            ).apply(instance, MoveTarget::new));
}
