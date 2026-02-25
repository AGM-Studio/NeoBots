package xyz.agmstudio.neobots.modules.abstracts.task;

import com.simibubi.create.AllBlocks;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.modules.abstracts.data.ModuleTransferData;
import xyz.agmstudio.neobots.robos.NeoBotCrash;
import xyz.agmstudio.neobots.robos.NeoBotEntity;
import xyz.agmstudio.neobots.utils.NeoBotsHelper;

public abstract class ItemTransferTask<D extends ModuleTransferData> extends ModuleTask<D> {
    protected final double reach;
    protected final int cooldown;
    protected boolean skipped = false;
    protected int transferred = 0;
    protected int tick = 0;

    public ItemTransferTask(NeoBotEntity bot, D data, double reach, int cooldown) {
        super(bot, data);
        this.reach = reach;
        this.cooldown = cooldown;
    }

    public IItemHandler getInventory() {
        return new InvWrapper(bot.getInventory());
    }
    public IItemHandler getTarget() {
        IItemHandler handler = data.getHandler(bot, reach);
        if (handler == null) throw NeoBotCrash.INVENTORY_INACCESSIBLE;
        return handler;
    }
    public boolean isTargetDepot() {
        return bot.level().getBlockState(data.getTarget()).getBlock() == AllBlocks.DEPOT.get();
    }

    public abstract IItemHandler from();
    public abstract IItemHandler to();

    @Override public void load(@NotNull CompoundTag tag) {
        this.transferred = tag.getInt("transferred");
        this.tick = tag.getInt("tick");
    }

    @Override public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("transferred", transferred);
        tag.putInt("tick", tick);
        return tag;
    }

    private void setTransferred(int value) {
        this.transferred = value;
        this.setDirty();
    }

    @Override public void onStart() {
        if (getTarget() != null)
            bot.lookAt(EntityAnchorArgument.Anchor.EYES, data.getTarget().getCenter());
    }

    @Override public void onStop() {}

    @Override public void onFinish() {}

    @Override public boolean isDone() {
        if (data.getTarget() == null || skipped) return true;
        return transferred >= data.getCount();
    }

    @Override public void tick() {
        IItemHandler handler = data.getHandler(bot, reach);
        if (handler == null) throw NeoBotCrash.INVENTORY_INACCESSIBLE;
        bot.lookAt(EntityAnchorArgument.Anchor.EYES, data.getTarget().getCenter());
        if (transferred >= data.getCount() || tick++ < cooldown) return;

        int toMove = isTargetDepot() ? data.getCount() - transferred : 1;
        int moved = NeoBotsHelper.moveItems(bot.level(), from(), to(), data.getFilter(), toMove);
        if (moved > 0) setTransferred(transferred + moved);
        else skipped = data.getSkip();
        tick = 0;
    }

    @Override protected Object @NotNull [] getTranslateArgs() {
        return new Object[]{transferred, data.getCount()};
    }

    public static abstract class Advanced<D extends ModuleTransferData> extends ItemTransferTask<D> {
        private final Vec3 target;
        public Advanced(NeoBotEntity bot, D data, double reach, int cooldown) {
            super(bot, data, reach, cooldown);
            if (data.getTarget() == null) target = null;
            else {
                BlockPos pos = data.getTarget().relative(data.getSide());
                Level level = bot.level();
                BlockState state = level.getBlockState(pos);
                VoxelShape shape = state.getCollisionShape(level, pos);

                double height = shape.isEmpty() ? 0.0 : shape.max(Direction.Axis.Y);
                target = new Vec3(pos.getX() + 0.5, pos.getY() + height, pos.getZ() + 0.5);
            }
        }

        @Override public void onStart() {
            if (!data.isSameDimension(bot.level().dimension())) throw NeoBotCrash.TARGET_INACCESSIBLE;
            if (bot.position().distanceTo(target) > 0.02) bot.getNavigation().moveTo(target.x, target.y, target.z, 0, 1.0);
            else super.onStart();
        }

        @Override public void tick() {
            if (target == null) return;
            if (!bot.getNavigation().isDone()) return;
            Vec3 delta = target.subtract(bot.position());
            if (delta.lengthSqr() < 0.02) {
                bot.setDeltaMovement(Vec3.ZERO);
                super.tick();
            } else {
                double speed = bot.getAttributeValue(Attributes.MOVEMENT_SPEED) / 2.0;
                double maxStep = Math.min(speed, Math.sqrt(delta.lengthSqr()));

                Vec3 motion = delta.normalize().scale(maxStep);
                bot.setDeltaMovement(motion.x, bot.getDeltaMovement().y, motion.z);
            }
        }

        @Override protected @NotNull Component getOnGoingStatus() {
            if (!bot.getNavigation().isDone())
                return Component.translatable("module.create_neobots." + getType() + ".status.on_the_way", data.getTarget().relative(data.getSide()).toShortString()).withStyle(getStatusStyle());
            return super.getOnGoingStatus();
        }
    }
}