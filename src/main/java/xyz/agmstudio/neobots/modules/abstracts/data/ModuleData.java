package xyz.agmstudio.neobots.modules.abstracts.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static xyz.agmstudio.neobots.index.CNBDataComponents.MODULE_DATA;

@ParametersAreNonnullByDefault
public abstract class ModuleData {
    public interface Gen<D extends ModuleData> {
        D generate(Level level, ItemStack stack);
    }

    protected final CompoundTag tag;
    protected final ItemStack stack;
    protected final Level level;
    protected ModuleData(Level level, ItemStack stack) {
        CompoundTag data = stack.get(MODULE_DATA);
        if (data == null) this.tag = new CompoundTag();
        else this.tag = data.copy();
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
        stack.set(MODULE_DATA, data.tag.copy());
    }

    protected abstract String getTranslateKey();
    public abstract int getCooldown();
    public abstract void addTooltip(List<Component> tooltip, Item.@NotNull TooltipContext ctx, @NotNull TooltipFlag flags);
}