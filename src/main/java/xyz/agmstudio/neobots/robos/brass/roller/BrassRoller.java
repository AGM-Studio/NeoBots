package xyz.agmstudio.neobots.robos.brass.roller;

import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

public class BrassRoller extends NeoBotEntity {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState crashAnimationState = new AnimationState();
    public final AnimationState shutdownAnimationState = new AnimationState();

    protected int animTick = 0;
    private NeoBotEntity.State state = null;

    public BrassRoller(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (animTick > 0) animTick--;
            if (state == State.RUNNING && animTick <= 0) {
                idleAnimationState.start(this.tickCount);
                animTick = 40;
            }
        }
    }

    public void handleStateAnimation(State state) {
        if (state == this.state) return;

        this.state = state;
        idleAnimationState.stop();
        crashAnimationState.stop();
        shutdownAnimationState.stop();

        NeoBots.LOGGER.info("BrassRoller state changed to {}", this.state);
        switch (state) {
            case NO_CHARGE, STOPPED -> {
                animTick = 20;
                shutdownAnimationState.start(this.tickCount);
            }
            case CRASHED -> {
                animTick = 50;
                crashAnimationState.start(this.tickCount);
            }
            default -> {
                animTick = 40;
                idleAnimationState.start(this.tickCount);
            }
        }
    }
}