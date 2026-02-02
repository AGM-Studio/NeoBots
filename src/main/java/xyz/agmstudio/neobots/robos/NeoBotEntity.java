package xyz.agmstudio.neobots.robos;

import com.simibubi.create.AllSoundEvents;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
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
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.menus.NeoBotMenu;
import xyz.agmstudio.neobots.modules.BotModuleItem;
import xyz.agmstudio.neobots.modules.IBotModule;

public class NeoBotEntity extends PathfinderMob implements MenuProvider {
    private int activeModuleIndex = 0;
    private boolean moduleJustStarted = true;

    // Cooldown
    private int cooldownTicks = 0;
    private static final int MODULE_COOLDOWN = 20;

    private final SimpleContainer moduleInventory = new SimpleContainer(6) {
        @Override public boolean canPlaceItem(int slot, ItemStack stack) {
            return stack.getItem() instanceof BotModuleItem;
        }

        @Override public void setChanged() {
            super.setChanged();
            NeoBotEntity.this.setChanged();
        }
    };

    private void setChanged() {

    }
    public void useUpgrade() {
        level().playSound(null, blockPosition(), AllSoundEvents.DESK_BELL_USE.getMainEventHolder().value(), SoundSource.NEUTRAL);
        NeoBots.LOGGER.debug("Upgrade called");
    }

    public SimpleContainer getModuleInventory() {
        return moduleInventory;
    }
    public int getActiveModuleIndex() {
        return activeModuleIndex;
    }

    public void setActiveModule(int index) {
        activeModuleIndex = index < getModuleInventory().getContainerSize() ? index : 0;
        moduleJustStarted = true;
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

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }

        SimpleContainer inv = getModuleInventory();
        if (inv.isEmpty()) return;

        int size = inv.getContainerSize();
        if (activeModuleIndex >= size) { // Clamp index (in case slots changed)
            activeModuleIndex = 0;
            moduleJustStarted = true;
        }

        ItemStack stack = inv.getItem(activeModuleIndex);
        if (!(stack.getItem() instanceof IBotModule module)) {
            advanceModule(size);
            return;
        }

        if (moduleJustStarted) {
            module.onStart(this, stack);
            moduleJustStarted = false;
        }

        module.tick(this, stack);

        if (module.isFinished(this, stack)) {
            module.onStop(this, stack);
            cooldownTicks = MODULE_COOLDOWN;
            advanceModule(size);
        }
    }

    private void advanceModule(int size) {
        activeModuleIndex = (activeModuleIndex + 1) % size;
        moduleJustStarted = true;
        setChanged();
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
        tag.put("Modules", moduleInventory.createTag(access));
        tag.putInt("ActiveModule", activeModuleIndex);
        tag.putInt("Cooldown", cooldownTicks);
    }

    @Override public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        RegistryAccess access = level().registryAccess();
        moduleInventory.fromTag(tag.getList("Modules", 10), access);
        activeModuleIndex = tag.getInt("ActiveModule");
        cooldownTicks = tag.getInt("Cooldown");

        moduleJustStarted = true;
    }
}
