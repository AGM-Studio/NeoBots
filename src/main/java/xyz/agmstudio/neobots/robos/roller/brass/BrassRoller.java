package xyz.agmstudio.neobots.robos.roller.brass;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.modules.abstracts.item.ModuleItem;
import xyz.agmstudio.neobots.modules.abstracts.item.ModuleTier;
import xyz.agmstudio.neobots.robos.roller.NeoBotRollerEntity;

public class BrassRoller extends NeoBotRollerEntity {
    public BrassRoller(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override public boolean canExecute(@NotNull ModuleItem<?, ?> module) {
        return module.getTier() == ModuleTier.BRASS || module.getTier() == ModuleTier.ANDESITE;
    }
}