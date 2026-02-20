package xyz.agmstudio.neobots.robos.brass.roller;

import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

public class BrassRoller extends NeoBotEntity {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState crashAnimationState = new AnimationState();
    public final AnimationState shutdownAnimationState = new AnimationState();
    public final AnimationState turnonAnimationState = new AnimationState();

    protected int animTick = 0;
    protected State state = null;
    protected State oldState = null;
    protected boolean turningOn = false;

    public BrassRoller(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    protected double oldWheelRot = 0;
    protected double wheelRot = 0;
    private double prevWheelX = 0;
    private double prevWheelZ = 0;
    private void calculateWheelRotation() {
        oldWheelRot = wheelRot;
        float yawRad = (float) Math.toRadians(getYRot());
        double dx = (getX() - prevWheelX) * (-Math.sin(yawRad));
        double dz = (getZ() - prevWheelZ) * ( Math.cos(yawRad));

        wheelRot += (dx + dz) * 2;
        wheelRot %= Math.PI * 2;

        prevWheelX = getX();
        prevWheelZ = getZ();
    }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) {
            calculateWheelRotation();
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