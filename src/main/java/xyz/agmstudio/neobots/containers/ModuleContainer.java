package xyz.agmstudio.neobots.containers;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.modules.abstracts.item.ModuleItem;
import xyz.agmstudio.neobots.modules.abstracts.task.ModuleTask;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

public class ModuleContainer extends BotFilteredContainer {
    private int activeModuleIndex = -1;
    private boolean moduleJustStarted = true;

    private boolean hasModules = false;

    public ModuleContainer(NeoBotEntity bot, int maxSize) {
        super(bot, maxSize);
    }

    public int getActiveModuleIndex() {
        return activeModuleIndex;
    }

    public void setActiveModuleIndex(int index) {
        if (index < 0 || index >= getContainerSize()) index = 0;
        activeModuleIndex = index;
        moduleJustStarted = true;
    }

    public ModuleTask<?> nextTask() {
        advance();
        return getTask();
    }
    public ModuleTask<?> getTask() {
        if (activeModuleIndex < 0) return null;
        ItemStack stack = getItem(activeModuleIndex);
        if (!(stack.getItem() instanceof ModuleItem)) {
            if (!advance()) return null;
            stack = getItem(activeModuleIndex);
        }
        ModuleItem<?, ?> module = (ModuleItem<?, ?>) stack.getItem();
        ModuleTask<?> task = module.getTask(bot, stack);
        if (moduleJustStarted) task.setJustStarted();
        return task;
    }
    public ItemStack getModuleStack() {
        return getItem(activeModuleIndex);
    }

    public boolean advance() {
        int size = bot.getModuleCapacity();
        int index = -1;
        for (int i = 0; i < size; i++) {
            int idx = (activeModuleIndex + i + 1) % size;
            if (getItem(idx).getItem() instanceof ModuleItem) {
                index = idx;
                break;
            }
        }
        if (index == -1) {
            hasModules = false;
            return false;
        }

        setActiveModuleIndex(index);
        return true;
    }

    @Override public void setChanged() {
        super.setChanged();
        hasModules = false;
        for (ItemStack stack: getItems()) {
            if (stack.getItem() instanceof ModuleItem) {
                hasModules = true;
                break;
            }
        }

        bot.reloadTask();
    }

    @Override public void loadTag(@NotNull CompoundTag tag, String key, HolderLookup.@NotNull Provider access) {
        CompoundTag data = tag.getCompound(key);
        this.fromTag(data.getList("inv", 10), access);
        this.activeModuleIndex = data.getInt("current");

        moduleJustStarted = true;
    }
    @Override public void saveTag(@NotNull CompoundTag tag, String key, HolderLookup.@NotNull Provider access) {
        CompoundTag data = new CompoundTag();
        data.put("inv", this.createTag(access));
        data.putInt("current", activeModuleIndex);

        tag.put(key, data);
    }

    @Override public boolean isItemValid(ItemStack stack) {
        return stack.getItem() instanceof ModuleItem;
    }

    @Override public int getActiveSlots() {
        return bot.getModuleCapacity();
    }

    @Override public int getMaxStackSize() {
        return 1;
    }
}