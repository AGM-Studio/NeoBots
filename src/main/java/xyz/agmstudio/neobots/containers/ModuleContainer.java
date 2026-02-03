package xyz.agmstudio.neobots.containers;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.menus.NeoBotMenu;
import xyz.agmstudio.neobots.modules.BotModuleItem;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

public class ModuleContainer extends BotFilteredContainer<BotModuleItem> {
    public ModuleContainer(NeoBotEntity bot, int maxSize) {
        super(bot, maxSize, BotModuleItem.class);
    }

    @Override public int getActiveSlots() {
        return bot.getModuleCapacity();
    }

    public static class Slot extends net.minecraft.world.inventory.Slot {
        private final NeoBotEntity bot;
        private final int index;

        public static NeoBotMenu.MenuSlotCreator builder(NeoBotEntity bot) {
            return (c, i, x, y) -> new Slot(bot, c, i, x, y);
        }
        public Slot(NeoBotEntity bot, Container inv, int index, int x, int y) {
            super(inv, index, x, y);
            this.index = index;
            this.bot = bot;
        }

        @Override public boolean mayPlace(@NotNull ItemStack stack) {
            return BotModuleItem.isModule(stack) && index < bot.getModuleCapacity();
        }
        @Override public boolean mayPickup(@NotNull Player player) {
            return index < bot.getModuleCapacity();
        }
        @Override public boolean isActive() {
            return index < bot.getModuleCapacity();
        }
    }
}
