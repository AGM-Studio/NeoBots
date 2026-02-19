package xyz.agmstudio.neobots.modules.abstracts.task;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.modules.abstracts.data.ModuleData;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

public abstract class ModuleTask<D extends ModuleData> {
    public interface Gen<D extends ModuleData, T> {
        T generate(NeoBotEntity bot, D data);
    }

    protected final NeoBotEntity bot;
    protected final String id;
    protected final D data;

    private boolean dirty = false;
    private boolean justStarted = false;

    public ModuleTask(NeoBotEntity bot, D data) {
        this.bot = bot;
        this.data = data;
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
    public abstract void onFinish();
    public abstract boolean isDone();
    public abstract void tick();

    public Component getStatus() {
        if (!isDone())
            return Component.translatable("module.create_neobots." + getType() + ".status", getTranslateArgs()).withStyle(getStatusStyle());
        else
            return Component.translatable("module.create_neobots." + getType() + ".status.is_done", getTranslateArgs()).withStyle(getStatusStyle());
    }
    protected Style getFinishedStyle() {
        return Style.EMPTY.withColor(ChatFormatting.GREEN);
    }
    protected Style getStatusStyle() {
        return Style.EMPTY.withColor(ChatFormatting.YELLOW);
    }
    protected Object @NotNull [] getTranslateArgs() {
        return new Object[]{};
    }

    public int getCooldown() {
        return data.getCooldown();
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