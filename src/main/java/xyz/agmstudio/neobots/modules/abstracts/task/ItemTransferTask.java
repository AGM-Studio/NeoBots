package xyz.agmstudio.neobots.modules.abstracts.task;

import com.simibubi.create.AllBlocks;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.modules.abstracts.data.ModuleTransferData;
import xyz.agmstudio.neobots.robos.NeoBotCrash;
import xyz.agmstudio.neobots.robos.NeoBotEntity;
import xyz.agmstudio.neobots.utils.NeoBotsHelper;

public abstract class ItemTransferTask<D extends ModuleTransferData> extends ModuleTask<D> {
    protected final double reach;
    protected final int cooldown;
    protected boolean skipped = false;
    protected int transferred = 0;
    protected int tick = 0;

    public ItemTransferTask(NeoBotEntity bot, D data, double reach, int cooldown) {
        super(bot, data);
        this.reach = reach;
        this.cooldown = cooldown;
    }

    public IItemHandler getInventory() {
        return new InvWrapper(bot.getInventory());
    }
    public IItemHandler getTarget() {
        IItemHandler handler = data.getHandler(bot, reach);
        if (handler == null) throw NeoBotCrash.INVENTORY_INACCESSIBLE;
        return handler;
    }
    public boolean isTargetDepot() {
        return bot.level().getBlockState(data.getTarget()).getBlock() == AllBlocks.DEPOT.get();
    }

    public abstract IItemHandler from();
    public abstract IItemHandler to();

    @Override public void load(@NotNull CompoundTag tag) {
        this.transferred = tag.getInt("transferred");
        this.tick = tag.getInt("tick");
    }

    @Override public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("transferred", transferred);
        tag.putInt("tick", tick);
        return tag;
    }

    private void setTransferred(int value) {
        this.transferred = value;
        this.setDirty();
    }

    @Override public void onStart() {
        if (getTarget() != null)
            bot.lookAt(EntityAnchorArgument.Anchor.EYES, data.getTarget().getCenter());
    }

    @Override public void onStop() {}

    @Override public void onFinish() {}

    @Override public boolean isDone() {
        if (data.getTarget() == null || skipped) return true;
        return transferred >= data.getCount();
    }

    @Override public void tick() {
        IItemHandler handler = data.getHandler(bot, reach);
        if (handler == null) throw NeoBotCrash.INVENTORY_INACCESSIBLE;
        bot.lookAt(EntityAnchorArgument.Anchor.EYES, data.getTarget().getCenter());
        if (transferred >= data.getCount() || tick++ < cooldown) return;

        int toMove = isTargetDepot() ? data.getCount() - transferred : 1;
        int moved = NeoBotsHelper.moveItems(bot.level(), from(), to(), data.getFilter(), toMove);
        if (moved > 0) setTransferred(transferred + moved);
        else skipped = data.getSkip();
        tick = 0;
    }

    @Override protected Object @NotNull [] getTranslateArgs() {
        return new Object[]{transferred, data.getCount()};
    }
}