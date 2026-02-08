package xyz.agmstudio.neobots.modules.abstracts;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class ModuleBlockPosData extends ModuleData {
    protected BlockPos target = null;
    protected ResourceLocation dimension = null;
    protected ModuleBlockPosData(Level level, ItemStack stack) {
        super(level, stack);
        if (tag.contains("target")) {
            this.target = BlockPos.of(tag.getLong("target"));
            this.dimension = ResourceLocation.tryParse(tag.getString("dimension"));
        }
    }
    public BlockPos getTarget() {
        return target;
    }
    public boolean isSameDimension(ResourceKey<Level> dimension) {
        return dimension != null && Objects.equals(this.dimension, dimension.location());
    }
    public ResourceLocation getDimension() {
        return dimension;
    }
    public void setTarget(@Nullable BlockPos target, ResourceKey<Level> dimension) {
        this.target = target;
        if (this.target == null) {
            tag.remove("target");
            tag.remove("dimension");
        } else {
            this.dimension = dimension.location();
            tag.putLong("target", this.target.asLong());
            tag.putString("dimension", this.dimension.toString());
        }
    }
}