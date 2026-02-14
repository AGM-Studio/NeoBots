package xyz.agmstudio.neobots.robos;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.containers.InventoryContainer;
import xyz.agmstudio.neobots.containers.ModuleContainer;
import xyz.agmstudio.neobots.containers.UpgradeContainer;
import xyz.agmstudio.neobots.menus.NeoBotMenu;
import xyz.agmstudio.neobots.modules.abstracts.ModuleTask;
import xyz.agmstudio.neobots.upgrades.MemoryUpgradeItem;
import xyz.agmstudio.neobots.utils.NeoEntityDataAccessor;

public class NeoBotEntity extends PathfinderMob implements MenuProvider {
    public static final NeoEntityDataAccessor<Component> TASK_STATUS =
            new NeoEntityDataAccessor<>(NeoBotEntity.class, EntityDataSerializers.COMPONENT);

    public enum State {
        LOADING(-1), STOPPED(0), RUNNING(1), CRASHED(2);
        private final int value;

        State(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }

    // Attributes
    protected final static int UPGRADE_SLOTS        = 3;
    protected final static int BASE_MODULE_SLOTS    = 6;
    protected final static int MAX_MODULE_SLOTS     = 32;
    protected final static int BASE_INVENTORY_SLOTS = 1;
    protected final static int MAX_INVENTORY_SLOTS  = 9;

    // Execution values
    private int moduleCapacity = BASE_MODULE_SLOTS;
    private int inventoryCapacity = BASE_INVENTORY_SLOTS;

    private int cooldownTicks = 0;
    private boolean onCooldown = false;
    private State state = State.LOADING;
    private NeoBotCrash lastCrash = null;

    private ModuleTask<?> task = null;
    private CompoundTag taskData = null;

    private final InventoryContainer inventory = new InventoryContainer(this, MAX_INVENTORY_SLOTS);
    private final ModuleContainer moduleInventory = new ModuleContainer(this, MAX_MODULE_SLOTS);
    private final UpgradeContainer upgradeInventory = new UpgradeContainer(this, UPGRADE_SLOTS);

    public void setChanged() {}

    public InventoryContainer getInventory() {
        return inventory;
    }
    public ModuleContainer getModuleInventory() {
        return moduleInventory;
    }
    public UpgradeContainer getUpgradeInventory() {
        return upgradeInventory;
    }

    public int getInventoryCapacity() {
        return inventoryCapacity;
    }
    public int getModuleCapacity() {
        return moduleCapacity;
    }
    public int getUpgradeCapacity() {
        return UPGRADE_SLOTS;
    }

    public int getActiveModuleIndex() {
        return moduleInventory.getActiveModuleIndex();
    }
    public void setActiveModule(int index) {
        moduleInventory.setActiveModuleIndex(index);
        task = moduleInventory.getTask();
    }

    public ModuleTask<?> getTask() {
        return task;
    }
    public void reloadTask() {
        task = moduleInventory.getTask();
    }
    public CompoundTag getTaskData() {
        return taskData;
    }

    public State getState() {
        return state;
    }
    public void setState(State state) {
        if (state == State.STOPPED) {
            task.onStop();
            task = null;
        } else if (state == State.RUNNING) {
            if (this.state == State.CRASHED) setActiveModule(0);
            lastCrash = null;
        }

        this.state = state;
        updateStatus();
    }

    public NeoBotEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override public boolean canBeLeashed() {
        return false;
    }
    @Override public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (!level().isClientSide && player instanceof ServerPlayer sp)
            sp.openMenu(this, buf -> buf.writeInt(this.getId()));

        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide || state != State.RUNNING) return;
        try {
            tickModules();
        } catch (NeoBotCrash crash) {
            this.lastCrash = crash;
            setState(State.CRASHED);
        }
        updateStatus();
    }

    private void tickModules() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }
        if (onCooldown) {
            onCooldown = false;
            task = moduleInventory.nextTask();
        }
        if (task == null) {
            task = moduleInventory.getTask();
            if (task == null) {
                cooldownTicks += 20;
                return;
            }
        }
        if (task.hasJustStarted()) {
            task.onStart();
            task.setStarted();
        }

        task.tick();
        if (task.isDone()) {
            task.onFinish();
            cooldownTicks = task.getCooldown();
            onCooldown = true;
        }
    }

    // Attributes
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 5.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.FOLLOW_RANGE, 128.0D);
    }

    // Menu
    @Override public @NotNull Component getDisplayName() {
        return Component.literal("NeoBot Menu");
    }

    @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new NeoBotMenu(id, inv, this);
    }

    @Override protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        TASK_STATUS.build(builder, Component.translatable("state.neobots.loading"));
    }

    private void updateStatus() {
        Component status;
        if (state == State.LOADING) status = Component.translatable("state.neobots.loading");
        else if (state == State.STOPPED) status = Component.translatable("state.neobots.stopped");
        else if (state == State.CRASHED) {
            MutableComponent crash = Component.translatable("state.neobots.crashed").withColor(0xff0000);
            if (lastCrash != null) status = crash.append(lastCrash.message);
            else status = crash;
        } else if (cooldownTicks > 0) status =  Component.translatable("state.neobots.cooldown");
        else if (task == null) status = Component.translatable("state.neobots.idle");
        else status = task.getStatus();
        TASK_STATUS.set(this, status);
    }

    // Data management
    @Override public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        RegistryAccess access = level().registryAccess();
        tag.put("Inventory", inventory.createTag(access));
        moduleInventory.saveTag(tag, "Modules", access);
        upgradeInventory.saveTag(tag, "Upgrades", access);

        tag.putInt("Cooldown", cooldownTicks);
        tag.putBoolean("OnCooldown", onCooldown);
        tag.putInt("State", state == State.CRASHED ? 2 + lastCrash.id : state.getValue());

        if (task == null) return;
        CompoundTag taskTag = task.save();
        taskTag.putString("id", task.getId());
        tag.put("Task", taskTag);
    }

    @Override public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        RegistryAccess access = level().registryAccess();
        inventory.fromTag(tag.getList("Inventory", 10), access);
        moduleInventory.loadTag(tag, "Modules", access);
        upgradeInventory.loadTag(tag, "Upgrades", access);

        cooldownTicks = tag.getInt("Cooldown");
        onCooldown = tag.getBoolean("OnCooldown");
        int state = tag.getInt("State");
        switch (state) {
            case 0: setState(State.STOPPED); break;
            case 1: setState(State.RUNNING); break;
            default: {
                lastCrash = NeoBotCrash.findById(state - 2);
                setState(State.CRASHED);
            }
        }

        taskData = tag.getCompound("Task");

        recalculateInventoryCapacity();
        recalculateModuleCapacity();
    }

    public void recalculateModuleCapacity() {
        int upgrades = 0;
        for (ItemStack stack: upgradeInventory.getItems()) {
            if (stack.getItem() instanceof MemoryUpgradeItem upgrade) {
                upgrades += 1;
            }
        }

        moduleCapacity = Math.min(BASE_MODULE_SLOTS + upgrades, MAX_MODULE_SLOTS);
    }
    public void recalculateInventoryCapacity() {
        int upgrades = 0;
        for (ItemStack stack: upgradeInventory.getItems()) {
            // TODO: Inventory Upgrades
        }
        inventoryCapacity = Math.min(BASE_INVENTORY_SLOTS + upgrades, MAX_INVENTORY_SLOTS);
    }
}