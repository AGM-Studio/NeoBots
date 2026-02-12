package xyz.agmstudio.neobots.block.charger;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.gui.Texture;
import xyz.agmstudio.neobots.menus.AbstractMenu;

import javax.annotation.ParametersAreNonnullByDefault;


@ParametersAreNonnullByDefault
public class ChargerMenu extends AbstractMenu {
    private static final Texture BG = new Texture("textures/gui/one_slot_panel.png", 176, 204);

    private final ChargerBlockEntity blockEntity;

    private static ChargerBlockEntity captureCharger(Level level, FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ChargerBlockEntity charger) return charger;
        throw new IllegalStateException("The provided BlockEntity is not a ChargerBlockEntity!");
    }
    public ChargerMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, captureCharger(inv.player.level(), buf));
    }
    public ChargerMenu(int id, Inventory inv, ChargerBlockEntity be) {
        super(NeoBots.CHARGER_MENU.get(), id, inv);
        this.blockEntity = be;

        this.addSlot(new Slot(blockEntity.inventory, 0, 80, 35));

        addPlayerInventory(8, 84);
    }

    @Override public @NotNull ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
        return ItemStack.EMPTY;
    }

    @Override public boolean stillValid(Player player) {
        return blockEntity.getBlockPos().distSqr(player.blockPosition()) <= 64;
    }

    @Override protected Texture getBackground() {
        return BG;
    }
}