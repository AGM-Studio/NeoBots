package xyz.agmstudio.neobots.modules;

import net.minecraft.world.item.ItemStack;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

public interface IBotModule {
    /** Called once when this module becomes active */
    void onStart(NeoBotEntity bot, ItemStack stack);

    /** Called every tick while active */
    void tick(NeoBotEntity bot, ItemStack stack);

    /** Return true when the module is finished */
    boolean isFinished(NeoBotEntity bot, ItemStack stack);

    /** Called once when the module finishes */
    default void onStop(NeoBotEntity bot, ItemStack stack) {}
}
