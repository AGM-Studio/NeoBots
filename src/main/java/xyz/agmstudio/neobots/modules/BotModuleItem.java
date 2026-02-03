package xyz.agmstudio.neobots.modules;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

public abstract class BotModuleItem extends Item {
    public static boolean isModule(@NotNull ItemStack stack) {
        return stack.getItem() instanceof BotModuleItem;
    }

    public BotModuleItem(Properties properties) {
        super(properties);
    }

    /** Called once when this module becomes active */
    public abstract void onStart(NeoBotEntity bot, ItemStack stack);

    /** Called every tick while active */
    public abstract void tick(NeoBotEntity bot, ItemStack stack);

    /** Return true when the module is finished */
    public abstract boolean isFinished(NeoBotEntity bot, ItemStack stack);

    /** Called once when the module finishes */
    public void onStop(NeoBotEntity bot, ItemStack stack) {}

    /** The module cooldown after it's finished */
    public int getCooldown(NeoBotEntity bot, ItemStack stack) {
        return 50;
    }
}
