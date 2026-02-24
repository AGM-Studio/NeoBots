package xyz.agmstudio.neobots.robos.roller;

import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

public abstract class NeoBotRollerEntity extends NeoBotEntity {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState crashAnimationState = new AnimationState();
    public final AnimationState shutdownAnimationState = new AnimationState();
    public final AnimationState turnonAnimationState = new AnimationState();
    protected int animTick = 0;

    protected State botState = null;
    protected State oldState = null;
    protected boolean turningOn = false;

    protected double oldWheelRot = 0;
    protected double wheelRot = 0;
    private double prevWheelX = 0;
    private double prevWheelZ = 0;

    public int getAnimTick() {
        return animTick;
    }
    public double getOldWheelRot() {
        return oldWheelRot;
    }
    public double getWheelRot() {
        return wheelRot;
    }
    public State getBotState() {
        return botState;
    }
    public State getOldState() {
        return oldState;
    }
    public boolean isTurningOn() {
        return turningOn;
    }

    public NeoBotRollerEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

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
            if (botState == State.RUNNING && animTick <= 0) {
                idleAnimationState.start(this.tickCount);
                animTick = 40;
            }
        }
    }

    public void handleStateAnimation(State state) {
        if (state == botState) return;
        this.oldState = botState;
        this.botState = state;
        idleAnimationState.stop();
        crashAnimationState.stop();
        shutdownAnimationState.stop();
        turnonAnimationState.stop();

        AnimationState animation = null;
        switch (state) {
            case NO_CHARGE, STOPPED, LOADING -> {
                animTick = 20;
                animation = shutdownAnimationState;
            }
            case CRASHED -> {
                animTick = 50;
                animation = crashAnimationState;
            }
            case RUNNING -> {
                animTick = 20;
                turningOn = true;
                animation = turnonAnimationState;
                if (!level().isClientSide) addCooldownTicks(20);
            }
        }

        if (animation == null) return;
        animation.start(tickCount);
        if (oldState == null || oldState == State.LOADING) {
            animation.fastForward(animTick - 1, 1.0f);
            animTick = 0;
        }
    }
}