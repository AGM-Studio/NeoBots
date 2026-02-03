package xyz.agmstudio.neobots.containers;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.modules.BotModuleItem;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

public class ModuleContainer extends BotFilteredContainer {
    private int activeModuleIndex = 0;
    private boolean moduleJustStarted = true;

    private int cooldownTicks = 0;
    private boolean onCooldown = false;

    private boolean hasModules = false;

    public ModuleContainer(NeoBotEntity bot, int maxSize) {
        super(bot, maxSize);
    }

    public int getActiveModuleIndex() {
        return activeModuleIndex;
    }
    public boolean isModuleJustStarted() {
        return moduleJustStarted;
    }

    public void setActiveModuleIndex(int index) {
        if (index < 0 || index >= getContainerSize()) index = 0;
        activeModuleIndex = index;
        moduleJustStarted = true;
    }

    private boolean advance() {
        int size = bot.getModuleCapacity();
        int index = -1;
        for (int i = 0; i < size; i++) {
            int idx = (activeModuleIndex + i + 1) % size;
            if (getItem(idx).getItem() instanceof BotModuleItem) {
                index = idx;
                break;
            }
        }
        if (index == -1) {
            hasModules = false;
            return false;
        }

        activeModuleIndex = index;
        moduleJustStarted = true;
        return true;
    }

    public void tickModules() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }
        if (onCooldown) {
            onCooldown = false;
            advance();
        }

        if (!hasModules) return;
        if (activeModuleIndex >= bot.getModuleCapacity())
            activeModuleIndex = 0;

        ItemStack stack = getItem(activeModuleIndex);
        if (!(stack.getItem() instanceof BotModuleItem)) {
            if (!advance()) return;
            stack = getItem(activeModuleIndex);
        }

        BotModuleItem module = (BotModuleItem) stack.getItem();
        if (moduleJustStarted) {
            module.onStart(bot, stack);
            moduleJustStarted = false;
        }

        module.tick(bot, stack);
        if (module.isFinished(bot, stack)) {
            module.onStop(bot, stack);
            cooldownTicks = module.getCooldown(bot, stack);
            onCooldown = true;
        }
    }

    @Override public void setChanged() {
        super.setChanged();
        hasModules = false;
        for (ItemStack stack: getItems()) {
            if (stack.getItem() instanceof BotModuleItem) {
                hasModules = true;
                break;
            }
        }
    }

    @Override public void loadTag(@NotNull CompoundTag tag, String key, HolderLookup.@NotNull Provider access) {
        CompoundTag data = tag.getCompound(key);
        this.fromTag(data.getList("inv", 10), access);
        this.activeModuleIndex = data.getInt("current");
        this.cooldownTicks = data.getInt("cooldown");
        this.onCooldown = data.getBoolean("on_cooldown");

        moduleJustStarted = true;
    }
    @Override public void saveTag(@NotNull CompoundTag tag, String key, HolderLookup.@NotNull Provider access) {
        CompoundTag data = new CompoundTag();
        data.put("inv", this.createTag(access));
        data.putInt("current", activeModuleIndex);
        data.putInt("cooldown", cooldownTicks);
        data.putBoolean("on_cooldown", onCooldown);

        tag.put(key, data);
    }

    @Override public boolean isItemValid(ItemStack stack) {
        return BotModuleItem.isModule(stack);
    }

    @Override public int getActiveSlots() {
        return bot.getModuleCapacity();
    }
}
