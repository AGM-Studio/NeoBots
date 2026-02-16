package xyz.agmstudio.neobots.modules;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.menus.TransferModuleMenu;
import xyz.agmstudio.neobots.modules.abstracts.ModuleTask;
import xyz.agmstudio.neobots.modules.abstracts.data.ModuleTransferData;
import xyz.agmstudio.neobots.modules.abstracts.item.TargetedModuleItem;
import xyz.agmstudio.neobots.robos.NeoBotCrash;
import xyz.agmstudio.neobots.robos.NeoBotEntity;
import xyz.agmstudio.neobots.utils.NeoBotsHelper;

public class WithdrawModule extends TargetedModuleItem<WithdrawModule.Data, WithdrawModule.Task> implements MenuProvider {
    private static final int REACH_SQR = 4;
    private static final int COOLDOWN = 4;

    public WithdrawModule(Properties props) {
        super("withdraw", props, (bot, stack) -> new Task(bot, stack, REACH_SQR, COOLDOWN), Data::new);
    }

    @Override public boolean isValidTarget(@NotNull UseOnContext ctx, @NotNull BlockPos pos) {
        return ctx.getLevel().getBlockEntity(pos) instanceof Container;
    }

    @Override public @NotNull Component getDisplayName() {
        return Component.translatable("item.create_neobots.withdraw_module");
    }

    @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return TransferModuleMenu.create(id, inv);
    }
    public static class Task extends ModuleTask<Data> {
        private final double reach;
        private final int cooldown;
        private boolean skipped = false;
        private int withdrawn = 0;
        private int tick = 0;

        public Task(NeoBotEntity bot, Data data, double reach, int cooldown) {
            super(bot, data);
            this.reach = reach;
            this.cooldown = cooldown;
        }

        @Override public String getType() {
            return "withdraw";
        }

        @Override public void load(@NotNull CompoundTag tag) {
            this.withdrawn = tag.getInt("withdrawn");
            this.tick = tag.getInt("tick");
        }

        @Override public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("withdrawn", withdrawn);
            tag.putInt("tick", tick);
            return tag;
        }

        private void setWithdrawn(int value) {
            this.withdrawn = value;
            this.setDirty();
        }

        @Override public void onStart() {}

        @Override public void onStop() {}

        @Override public void onFinish() {}

        @Override public boolean isDone() {
            if (data.getTarget() == null || skipped) return true;
            return withdrawn >= data.getCount();
        }

        @Override public void tick() {
            Container container = data.getContainer(bot, reach);
            if (container == null) throw NeoBotCrash.INVENTORY_INACCESSIBLE;
            if (withdrawn >= data.getCount() || tick++ < cooldown) return;

            int moved = NeoBotsHelper.moveItems(bot.level(), container, bot.getInventory(), data.getFilter(), 1);
            if (moved > 0) setWithdrawn(withdrawn + moved);
            else skipped = data.getSkip();
            tick = 0;
        }

        @Override protected Object @NotNull [] getTranslateArgs() {
            return new Object[]{withdrawn, data.getCount()};
        }
    }
    public static class Data extends ModuleTransferData {
        public Data(Level level, ItemStack stack) {
            super(level, stack);
        }
        @Override protected String getTranslateKey() {
            return "withdraw";
        }
    }
}