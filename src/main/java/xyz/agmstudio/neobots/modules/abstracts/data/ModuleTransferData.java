package xyz.agmstudio.neobots.modules.abstracts.data;

import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.logistics.vault.ItemVaultBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public abstract class ModuleTransferData extends ModuleBlockPosData {
    protected int count;
    protected ItemStack filter = ItemStack.EMPTY;
    protected boolean skip;

    protected ModuleTransferData(Level level, ItemStack stack) {
        super(level, stack);
        this.count = Math.max(1, tag.getInt("count"));
        this.skip = tag.getBoolean("skip");
        if (tag.contains("filter"))
            this.filter = ItemStack.parseOptional(level.registryAccess(), tag.getCompound("filter"));
    }

    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = Math.max(1, count);
        tag.putInt("count", this.count);
    }

    public ItemStack getFilter() {
        return filter;
    }
    public void setFilter(@Nullable ItemStack filter) {
        this.filter = filter != null ? filter : ItemStack.EMPTY;
        if (this.filter.isEmpty()) tag.remove("filter");
        else tag.put("filter", this.filter.save(level.registryAccess()));
    }

    public boolean getSkip() {
        return skip;
    }
    public void setSkip(boolean skip) {
        this.skip = skip;
        tag.putBoolean("skip", this.skip);
    }

    public @Nullable IItemHandler getHandler(NeoBotEntity bot, double reach) {
        if (target == null || !isSameDimension(bot.level().dimension())) return null;
        if (target.distSqr(bot.blockPosition()) > reach) return null;
        if (bot.level().getBlockEntity(target) instanceof ItemVaultBlockEntity) return null;
        return bot.level().getCapability(Capabilities.ItemHandler.BLOCK, target, side);
    }

    @Override public int getCooldown() {
        return 50;
    }
    @Override public void addTooltip(List<Component> tooltip, Item.@NotNull TooltipContext ctx, @NotNull TooltipFlag flags) {
        String trans = "module.create_neobots." + getTranslateKey() + ".tooltip.";
        tooltip.add(Component.translatable(trans + "count", count).withStyle(ChatFormatting.AQUA));
        if (target != null) tooltip.add(Component.translatable(trans + "target", target.toShortString()).withStyle(ChatFormatting.AQUA));
        else tooltip.add(Component.translatable(trans + "no_target").withStyle(ChatFormatting.RED));
        if (skip) tooltip.add(Component.translatable(trans + "skip").withStyle(ChatFormatting.GRAY));
        if (!filter.isEmpty()) {
            tooltip.add(Component.translatable(trans + "filter").withStyle(ChatFormatting.GRAY).append(filter.getHoverName().copy().withStyle(ChatFormatting.YELLOW)));
            if (filter.getItem() instanceof FilterItem filterItem) {
                List<Component> filterTooltip = new ArrayList<>();
                filterItem.appendHoverText(filter, ctx, filterTooltip, flags);
                if (!filterTooltip.isEmpty()) filterTooltip.removeFirst();
                for (Component line: filterTooltip)
                    tooltip.add(Component.literal("  ").append(line.copy()));
            }
        } else tooltip.add(Component.translatable(trans + "no_filter").withStyle(ChatFormatting.DARK_GRAY));
    }

    protected abstract String getTranslateKey();
}