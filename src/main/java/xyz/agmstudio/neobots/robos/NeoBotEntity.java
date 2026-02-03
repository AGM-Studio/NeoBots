package xyz.agmstudio.neobots.robos;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import xyz.agmstudio.neobots.containers.ModuleContainer;
import xyz.agmstudio.neobots.containers.UpgradeContainer;
import xyz.agmstudio.neobots.menus.NeoBotMenu;
import xyz.agmstudio.neobots.upgrades.MemoryUpgradeItem;

public class NeoBotEntity extends PathfinderMob implements MenuProvider {
    // Attributes
    protected final static int UPGRADE_SLOTS     = 3;
    protected final static int BASE_MODULE_SLOTS = 6;
    protected final static int MAX_MODULE_SLOTS  = 32;

    // Execution values
    private int moduleCapacity = BASE_MODULE_SLOTS;

    private final ModuleContainer moduleInventory = new ModuleContainer(this, MAX_MODULE_SLOTS);
    private final UpgradeContainer upgradeInventory = new UpgradeContainer(this, UPGRADE_SLOTS);

    public void setChanged() {

    }

    public ModuleContainer getModuleInventory() {
        return moduleInventory;
    }
    public UpgradeContainer getUpgradeInventory() {
        return upgradeInventory;
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
    }

    public NeoBotEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override public boolean canBeLeashed() {
        return false;
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (!level().isClientSide && player instanceof ServerPlayer sp)
            sp.openMenu(this, buf -> buf.writeInt(this.getId()));

        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) return;

        moduleInventory.tickModules();
    }

    // Attributes
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 5.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    // Menu
    @Override public @NotNull Component getDisplayName() {
        return Component.literal("NeoBot Menu");
    }

    @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new NeoBotMenu(id, inv, this);
    }

    // Data management
    @Override public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        RegistryAccess access = level().registryAccess();
        moduleInventory.saveTag(tag, "Modules", access);
        upgradeInventory.saveTag(tag, "Upgrades", access);
    }

    @Override public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        RegistryAccess access = level().registryAccess();
        moduleInventory.loadTag(tag, "Modules", access);
        upgradeInventory.loadTag(tag, "Upgrades", access);

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
}
