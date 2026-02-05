package xyz.agmstudio.neobots.modules;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

public abstract class BotTask {
    protected final NeoBotEntity bot;
    protected final ItemStack stack;
    protected final String id;

    private boolean dirty = false;
    private boolean justStarted = false;

    public BotTask(NeoBotEntity bot, ItemStack stack) {
        this.bot = bot;
        this.stack = stack;
        this.id = getType() + "_" + bot.getActiveModuleIndex();
    }
    public CompoundTag save() {
        return new CompoundTag();
    }
    public void load(CompoundTag tag) {}
    public void setDirty() {
        this.dirty = true;
    }
    public boolean isDirty() {
        return dirty;
    }

    public String getId() {
        return id;
    }

    public abstract String getType();

    public abstract void onStart();
    public abstract void onStop();
    public abstract boolean isDone();
    public abstract void tick();
    public abstract Component getStatus();

    public int getCooldown() {
        return stack.getItem() instanceof BotModuleItem<?> module ? module.getCooldown(bot, stack) : 50;
    }

    public void setJustStarted() {
        this.justStarted = true;
    }
    public void setStarted() {
        justStarted = false;
    }
    public boolean hasJustStarted() {
        return justStarted;
    }
}