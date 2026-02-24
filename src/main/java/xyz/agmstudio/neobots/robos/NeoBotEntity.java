package xyz.agmstudio.neobots.robos;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.containers.BatteryContainer;
import xyz.agmstudio.neobots.containers.InventoryContainer;
import xyz.agmstudio.neobots.containers.ModuleContainer;
import xyz.agmstudio.neobots.containers.UpgradeContainer;
import xyz.agmstudio.neobots.menus.NeoBotMenu;
import xyz.agmstudio.neobots.modules.abstracts.task.ModuleTask;
import xyz.agmstudio.neobots.robos.roller.NeoBotRollerEntity;
import xyz.agmstudio.neobots.upgrades.InventoryUpgradeItem;
import xyz.agmstudio.neobots.upgrades.MemoryUpgradeItem;
import xyz.agmstudio.neobots.utils.NeoEntityDataAccessor;

import java.util.concurrent.atomic.AtomicInteger;

public class NeoBotEntity extends PathfinderMob implements MenuProvider {
    public static final NeoEntityDataAccessor<Component> TASK_STATUS =
            new NeoEntityDataAccessor<>(NeoBotEntity.class, EntityDataSerializers.COMPONENT);
    public static final NeoEntityDataAccessor<Integer> STATE =
            new NeoEntityDataAccessor<>(NeoBotEntity.class, EntityDataSerializers.INT);

    public enum State {
        LOADING(-2), NO_CHARGE(-1), STOPPED(0), RUNNING(1), CRASHED(2);
        private final int value;

        State(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }

        public static State of(int value) {
            for (State state: State.values())
                if (state.getValue() == value)
                    return state;

            return LOADING;
        }
        public static State of(@NotNull NeoBotEntity bot) {
            return of(NeoBotEntity.STATE.get(bot));
        }
    }

    // Attributes
    protected final static int UPGRADE_SLOTS        = 3;
    protected final static int BATTERY_SLOTS        = 1;
    protected final static int BASE_MODULE_SLOTS    = 6;
    protected final static int MAX_MODULE_SLOTS     = 32;
    protected final static int BASE_INVENTORY_SLOTS = 1;
    protected final static int MAX_INVENTORY_SLOTS  = 9;
    protected final static int BASE_CONSUMPTION     = 10;

    // Execution values
    private int moduleCapacity = BASE_MODULE_SLOTS;
    private int inventoryCapacity = BASE_INVENTORY_SLOTS;

    private int cooldownTicks = 0;
    private boolean onCooldown = false;
    private State state = State.LOADING;
    private NeoBotCrash lastCrash = null;

    private ModuleTask<?> task = null;
    private CompoundTag taskData = null;

    private final BatteryContainer batteryInventory = new BatteryContainer(this, BATTERY_SLOTS);
    private final InventoryContainer inventory = new InventoryContainer(this, MAX_INVENTORY_SLOTS);
    private final ModuleContainer moduleInventory = new ModuleContainer(this, MAX_MODULE_SLOTS);
    private final UpgradeContainer upgradeInventory = new UpgradeContainer(this, UPGRADE_SLOTS);

    public void setChanged() {}

    public BatteryContainer getBatteryInventory() {
        return batteryInventory;
    }
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

    public int getCooldownTicks() {
        return cooldownTicks;
    }
    public int addCooldownTicks(int ticks) {
        return cooldownTicks += ticks;
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
        if (state == State.STOPPED || state == State.CRASHED || state == State.NO_CHARGE) {
            if (task != null) {
                task.onStop();
                task = null;
            }
        } else if (state == State.RUNNING) {
            if (this.state == State.CRASHED) setActiveModule(0);
            lastCrash = null;
        }

        this.state = state;
        STATE.set(this, state.value);
        updateStatus();
    }

    public int getEnergy() {
        AtomicInteger value = new AtomicInteger();
        for (ItemStack battery: batteryInventory.getItems()) {
            IEnergyStorage storage = battery.getCapability(Capabilities.EnergyStorage.ITEM);
            if (storage != null) value.addAndGet(storage.getEnergyStored());
        }
        return value.get();
    }
    public int getTotalEnergy() {
        AtomicInteger value = new AtomicInteger();
        for (ItemStack battery: batteryInventory.getItems()) {
            IEnergyStorage storage = battery.getCapability(Capabilities.EnergyStorage.ITEM);
            if (storage != null) value.addAndGet(storage.getMaxEnergyStored());
        }
        return value.get();
    }
    public void consumeEnergy(int amount) {
        for (ItemStack battery: batteryInventory.getItems().reversed()) {
            IEnergyStorage storage = battery.getCapability(Capabilities.EnergyStorage.ITEM);
            if (storage != null) amount -= storage.extractEnergy(amount, false);
            if (amount <= 0) break;
        }
        if (amount > 0) throw NeoBotCrash.OUT_OF_CHARGE;
    }
    public void chargeEnergy(int amount) {
        for (ItemStack battery: batteryInventory.getItems()) {
            IEnergyStorage storage = battery.getCapability(Capabilities.EnergyStorage.ITEM);
            if (storage != null) amount -= storage.receiveEnergy(amount, false);
            if (amount <= 0) break;
        }
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
            consumeEnergy(BASE_CONSUMPTION);
            tickModules();
        } catch (NeoBotCrash crash) {
            if (crash == NeoBotCrash.OUT_OF_CHARGE) setState(State.NO_CHARGE);
            else {
                this.lastCrash = crash;
                setState(State.CRASHED);
            }
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

    @Override protected void dropCustomDeathLoot(@NotNull ServerLevel level, @NotNull DamageSource source, boolean flag) {
        Containers.dropContents(level, this, batteryInventory);
        Containers.dropContents(level, this, upgradeInventory);
        Containers.dropContents(level, this, moduleInventory);
        Containers.dropContents(level, this, inventory);
    }
    @Override protected boolean shouldDropLoot() {
        return true;
    }

    // Attributes
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.FOLLOW_RANGE, 128.0D);
    }

    // Menu
    @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return NeoBotMenu.create(id, inv, this);
    }

    @Override protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        TASK_STATUS.build(builder, Component.translatable("state.create_neobots.loading"));
        STATE.build(builder, State.LOADING.getValue());
    }

    @Override public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (STATE.getAccessor().equals(key) && this instanceof NeoBotRollerEntity roller)
            roller.handleStateAnimation(State.of(this));
    }

    private void updateStatus() {
        Component status;
        if (state == State.LOADING) status = Component.translatable("state.create_neobots.loading");
        else if (state == State.NO_CHARGE) status = Component.translatable("state.create_neobots.no_charge");
        else if (state == State.STOPPED) status = Component.translatable("state.create_neobots.stopped");
        else if (state == State.CRASHED) {
            MutableComponent crash = Component.translatable("state.create_neobots.crashed").withColor(0xff0000);
            if (lastCrash != null) status = crash.append(lastCrash.message);
            else status = crash;
        } else if (task == null) status = Component.translatable("state.create_neobots.idle");
        else status = task.getStatus();
        TASK_STATUS.set(this, status);
    }

    // Data management
    @Override public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        RegistryAccess access = level().registryAccess();
        inventory.saveTag(tag, "Inventory", access);
        moduleInventory.saveTag(tag, "Modules", access);
        upgradeInventory.saveTag(tag, "Upgrades", access);
        batteryInventory.saveTag(tag, "Battery", access);

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
        inventory.loadTag(tag, "Inventory", access);
        moduleInventory.loadTag(tag, "Modules", access);
        upgradeInventory.loadTag(tag, "Upgrades", access);
        batteryInventory.loadTag(tag, "Battery", access);

        cooldownTicks = tag.getInt("Cooldown");
        onCooldown = tag.getBoolean("OnCooldown");
        int state = tag.getInt("State");
        switch (state) {
            case -1: setState(State.NO_CHARGE); break;
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
        for (ItemStack stack: upgradeInventory.getItems())
            if (stack.getItem() instanceof MemoryUpgradeItem upgrade) upgrades += upgrade.getUpgradeSize();

        moduleCapacity = Math.min(BASE_MODULE_SLOTS + upgrades, MAX_MODULE_SLOTS);
    }
    public void recalculateInventoryCapacity() {
        int upgrades = 0;
        for (ItemStack stack: upgradeInventory.getItems())
            if (stack.getItem() instanceof InventoryUpgradeItem upgrade) upgrades += upgrade.getUpgradeSize();

        inventoryCapacity = Math.min(BASE_INVENTORY_SLOTS + upgrades, MAX_INVENTORY_SLOTS);
    }
}