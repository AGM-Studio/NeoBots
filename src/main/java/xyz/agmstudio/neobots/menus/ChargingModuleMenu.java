package xyz.agmstudio.neobots.menus;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.containers.slots.PreviewSlot;
import xyz.agmstudio.neobots.index.CNBMenus;
import xyz.agmstudio.neobots.menus.abstracts.AbstractMenu;
import xyz.agmstudio.neobots.modules.brass.BrassChargingModule;

public class ChargingModuleMenu extends AbstractMenu {
    protected final BrassChargingModule.Data data;
    protected final ItemStack module;

    public static @NotNull ChargingModuleMenu create(int id, Inventory inv) {
        return new ChargingModuleMenu(CNBMenus.TRANSFER_MODULE.get(), id, inv);
    }
    public ChargingModuleMenu(MenuType<?> menu, int id, Inventory inv) {
        super(menu, id, inv);
        this.module = inv.player.getMainHandItem();
        NeoBots.LOGGER.debug("Module: {}", module);
        if (module.getItem() instanceof BrassChargingModule m)
            data = m.getData(inv.player.level(), this.module.copy());
        else throw new IllegalArgumentException("Invalid module type for menu");

        addPlayerInventory(8, 122, this.data.getStack());
        addSlot(new PreviewSlot(data.getStack(), 18, 80));
    }

    @Override public void handlePacket(int id, boolean value) {
        if (id == 0) {
            data.setSkip(value);
            data.save();
        } else if (id == 1) {
            data.save(inventory.player.getMainHandItem());
        }
    }
    @Override public void handlePacket(int id, int value) {
        if (id == 0) { // mode
            if (value != 0 && value != 1) return;
            data.setMode(value);
            data.save();
        } else if (id == 1) { // value
            if (value < 0) return;
            if (data.getMode() == 0 && value > 100) return;
            data.setValue(value);
            data.save();
        } else if (id == 2) { // skip value
            if (value < 0 || value > 100) return;
            data.setSkipValue(value);
            data.save();
        }
    }

    @Override public boolean stillValid(@NotNull Player player) {
        return player.getMainHandItem() == module;
    }

    @Override public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;  // HAS NOTHING TO MOVE!
    }
}