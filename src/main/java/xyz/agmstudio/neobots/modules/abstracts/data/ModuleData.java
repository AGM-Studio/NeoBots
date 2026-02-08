package xyz.agmstudio.neobots.modules.abstracts.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.NeoBots;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public abstract class ModuleData {
    public record DataComponent(CompoundTag data) {
        public static void register() {}
        public static final Codec<DataComponent> CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        CompoundTag.CODEC.fieldOf("data").forGetter(DataComponent::data)
                ).apply(instance, DataComponent::new));

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<DataComponent>> COMPONENT = NeoBots.registerDataComponent("module_data", CODEC);
    }

    public interface Gen<D extends ModuleData> {
        D generate(Level level, ItemStack stack);
    }

    protected final CompoundTag tag;
    protected final ItemStack stack;
    protected final Level level;
    protected ModuleData(Level level, ItemStack stack) {
        DataComponent data = stack.get(DataComponent.COMPONENT);
        if (data == null) this.tag = new CompoundTag();
        else this.tag = data.data().copy();
        this.stack = stack;
        this.level = level;
    }
    public void save() {
        ModuleData.save(this, stack);
    }
    public void save(ItemStack stack) {
        ModuleData.save(this, stack);
    }

    public ItemStack getStack() {
        return stack;
    }
    public Item getItem() {
        return stack.getItem();
    }

    public static void save(ModuleData data, ItemStack stack) {
        DataComponent component = new DataComponent(data.tag.copy());
        stack.set(DataComponent.COMPONENT, component);
    }

    public abstract int getCooldown();
    public abstract void addTooltip(List<Component> tooltip, Item.@NotNull TooltipContext ctx, @NotNull TooltipFlag flags);
}