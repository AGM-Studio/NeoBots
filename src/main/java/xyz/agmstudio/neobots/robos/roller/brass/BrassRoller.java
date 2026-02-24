package xyz.agmstudio.neobots.robos.roller.brass;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import xyz.agmstudio.neobots.robos.roller.NeoBotRollerEntity;

public class BrassRoller extends NeoBotRollerEntity {
    public BrassRoller(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }
}