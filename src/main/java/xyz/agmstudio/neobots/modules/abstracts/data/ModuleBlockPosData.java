package xyz.agmstudio.neobots.modules.abstracts.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class ModuleBlockPosData extends ModuleData {
    protected BlockPos target = null;
    protected ResourceLocation dimension = null;
    protected Direction side = null;
    protected ModuleBlockPosData(Level level, ItemStack stack) {
        super(level, stack);
        if (tag.contains("target")) {
            this.target = BlockPos.of(tag.getLong("target"));
            this.dimension = ResourceLocation.tryParse(tag.getString("dimension"));
        }
        if (tag.contains("side")) {
            this.side = Direction.byName(tag.getString("side"));
        }
    }
    public BlockPos getTarget() {
        return target;
    }
    public Direction getSide() {
        return side;
    }
    public boolean isSameDimension(ResourceKey<Level> dimension) {
        return dimension != null && Objects.equals(this.dimension, dimension.location());
    }
    public ResourceLocation getDimension() {
        return dimension;
    }
    public void setTarget(@Nullable BlockPos target, ResourceKey<Level> dimension) {
        this.setTarget(target, dimension, null);
    }
    public void setTarget(@Nullable BlockPos target, ResourceKey<Level> dimension, Direction side) {
        this.target = target;
        if (this.target == null) {
            tag.remove("target");
            tag.remove("dimension");
            tag.remove("side");
        } else {
            this.dimension = dimension.location();
            tag.putLong("target", this.target.asLong());
            tag.putString("dimension", this.dimension.toString());
            if (side != null) tag.putString("side", side.getName());
            else tag.remove("side");
        }
    }
}