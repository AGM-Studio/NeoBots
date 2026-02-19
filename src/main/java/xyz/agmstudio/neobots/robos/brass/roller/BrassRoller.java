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
    public final AnimationState turnonAnimationState = new AnimationState();

    protected double wheelRot = 0;
    protected int animTick = 0;
    protected State state = null;
    protected State oldState = null;
    protected boolean turningOn = false;

    public BrassRoller(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) {
            wheelRot += getDeltaMovement().horizontalDistance() * 4;
            if (wheelRot > 2 * Math.PI) wheelRot -= 2 * Math.PI;
            if (animTick > 0) animTick--;
            if (turningOn && animTick <= 0) turningOn = false;
            if (state == State.RUNNING && animTick <= 0) {
                idleAnimationState.start(this.tickCount);
                animTick = 40;
            }
        }
    }

    public void handleStateAnimation(State state) {
        if (state == this.state) return;
        this.oldState = this.state;
        this.state = state;
        idleAnimationState.stop();
        crashAnimationState.stop();
        shutdownAnimationState.stop();
        turnonAnimationState.stop();

        NeoBots.LOGGER.info("BrassRoller state changed to {}", this.state);
        switch (state) {
            case NO_CHARGE, STOPPED, LOADING -> {
                animTick = 20;
                shutdownAnimationState.start(this.tickCount);
            }
            case CRASHED -> {
                animTick = 50;
                crashAnimationState.start(this.tickCount);
            }
            case RUNNING -> {
                animTick = 20;
                turningOn = true;
                turnonAnimationState.start(this.tickCount);
                if (!level().isClientSide) addCooldownTicks(20);
            }
        }
    }
}