package xyz.agmstudio.neobots.menus;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.modules.BotModuleItem;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

public class NeoBotMenu extends AbstractContainerMenu {
    private final NeoBotEntity bot;
    protected final DataSlot activeModule = DataSlot.standalone();
    protected final DataSlot upgradeChance = DataSlot.standalone();

    private static NeoBotEntity captureBot(Level level, FriendlyByteBuf buf) {
        Entity entity = level.getEntity(buf.readInt());
        if (entity instanceof NeoBotEntity bot) return bot;
        throw new IllegalStateException("The provided entity is not a NeoBotEntity!");
    }
    public NeoBotMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, captureBot(inv.player.level(), buf));
    }

    public NeoBotMenu(int id, Inventory inv, NeoBotEntity bot) {
        super(NeoBots.NEOBOT_INVENTORY.get(), id);
        this.bot = bot;

        addDataSlot(activeModule);
        addDataSlot(upgradeChance);

        SimpleContainer modules = bot.getModuleInventory();
        addRectangleShapedSlots(modules, 4, (int) (modules.getContainerSize() / 4.0) + 1, 6, 19, 0, -1, (c, index, x, y) ->
            this.addSlot(new Slot(c, index, x, y) {
                @Override public boolean mayPlace(@NotNull ItemStack stack) {
                    return stack.getItem() instanceof BotModuleItem;
                }
            })
        );

        // Player inventory
        addRectangleShapedSlots(inv, 9, 3, 87, 84, 9);
        addRectangleShapedSlots(inv, 9, 1, 87, 142, 0, 9);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player p_38941_, int p_38942_) {
        return null;
    }

    @Override public boolean stillValid(@NotNull Player player) {
        return bot.isAlive() && player.distanceTo(bot) < 8.0F;
    }
    @Override public void broadcastChanges() {
        super.broadcastChanges();
        if (bot == null || bot.level().isClientSide) return;

        activeModule.set(bot.getActiveModuleIndex());
        upgradeChance.set(5000);
    }

    public NeoBotEntity getBot() {
        return bot;
    }

    public void addRectangleShapedSlots(Container inv, int w, int h, int x, int y, int offset) {
        NeoBotMenu.this.addRectangleShapedSlots(inv, w, h, x, y, offset, -1, (c, index, px, py) ->
            this.addSlot(new Slot(c, index, px, py))
        );
    }
    public void addRectangleShapedSlots(Container inv, int w, int h, int x, int y, int offset, int limit) {
        NeoBotMenu.this.addRectangleShapedSlots(inv, w, h, x, y, offset, limit, (c, index, px, py) ->
            this.addSlot(new Slot(c, index, px, py))
        );
    }
    public void addRectangleShapedSlots(Container inv, int w, int h, int x, int y, int offset, int limit, MenuSlotCreator creator) {
        int size = inv.getContainerSize();
        for (int j = 0; j < h; ++j) for (int i = 0; i < w; i++) {
            int index = j * w + i + offset;
            if (index >= size || (limit > -1 && index >= limit)) break;
            creator.create(inv, index, x + i * 18, y + j * 18);
        }
    }

    public interface MenuSlotCreator {
        void create(Container inv, int index, int x, int y);
    }
}
