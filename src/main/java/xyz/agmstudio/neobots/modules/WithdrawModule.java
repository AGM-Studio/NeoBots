package xyz.agmstudio.neobots.modules;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.menus.modules.WithdrawModuleMenu;
import xyz.agmstudio.neobots.modules.abstracts.ModuleItem;
import xyz.agmstudio.neobots.modules.abstracts.ModuleTask;
import xyz.agmstudio.neobots.modules.abstracts.ModuleBlockPosData;
import xyz.agmstudio.neobots.robos.NeoBotEntity;
import xyz.agmstudio.neobots.utils.NeoBotsHelper;

import java.util.List;

public class WithdrawModule extends ModuleItem<WithdrawModule.Data, WithdrawModule.Task> implements MenuProvider {
    public static DeferredHolder<Item, WithdrawModule> ITEM = NeoBots.registerItem("withdraw_module", WithdrawModule::new, 1);
    public static DeferredHolder<MenuType<?>, MenuType<WithdrawModuleMenu>> MENU = NeoBots.registerMenu("withdraw_menu", WithdrawModuleMenu::new);
    public static void register() {}

    private static final int REACH_SQR = 4;

    public WithdrawModule(Properties props) {
        super(props, (bot, stack) -> new Task(bot, stack, REACH_SQR), Data::new);
    }

    @Override public @NotNull InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide)
            return InteractionResult.SUCCESS;

        BlockPos pos = ctx.getClickedPos();
        if (!(level.getBlockEntity(pos) instanceof Container))
            return InteractionResult.PASS;

        Data data = getData(level, ctx.getItemInHand());
        data.setTarget(pos, level.dimension());
        data.save();

        if (ctx.getPlayer() instanceof ServerPlayer player)
            player.displayClientMessage(Component.literal("Target container set").withStyle(ChatFormatting.GREEN), true);

        return InteractionResult.CONSUME;
    }

    @Override public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) sp.openMenu(this, buf -> buf.writeEnum(hand));

        return InteractionResultHolder.consume(stack);
    }

    @Override public @NotNull Component getDisplayName() {
        return Component.literal("Withdraw Module");
    }

    @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new WithdrawModuleMenu(id, inv);
    }
    public static class Task extends ModuleTask<Data> {
        private final double reach;
        private int withdrawn = 0;

        public Task(NeoBotEntity bot, Data data, double reach) {
            super(bot, data);
            this.reach = reach;
        }

        @Override public String getType() {
            return "withdraw";
        }

        @Override public void load(@NotNull CompoundTag tag) {
            this.withdrawn = tag.getInt("withdrawn");
        }

        @Override public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("withdrawn", withdrawn);
            return tag;
        }

        private void setWithdrawn(int value) {
            this.withdrawn = value;
            this.setDirty();
        }

        @Override public void onStart() {}

        @Override public void onStop() {}

        @Override public boolean isDone() {
            if (data.getTarget() == null) return true;
            return withdrawn >= data.count;
        }

        @Override
        public void tick() {
            if (bot.level().isClientSide) return;
            Container container = data.getContainer(bot, reach);
            if (container == null || withdrawn >= data.count) return;

            int remaining = data.count - withdrawn;
            int moved = NeoBotsHelper.moveItems(bot.level(), container, bot.getInventory(), data.filter, remaining);
            if (moved > 0) setWithdrawn(withdrawn + moved);
        }

        @Override public Component getStatus() {
            return Component.literal("Withdrawing (" + withdrawn + "/" + data.count + ")").withStyle(ChatFormatting.YELLOW);
        }
    }
    public static class Data extends ModuleBlockPosData {
        private int count;
        private ItemStack filter = ItemStack.EMPTY;
        private boolean skip;

        public Data(Level level, @NotNull ItemStack stack) {
            super(level, stack);
            this.count = Math.max(1, tag.getInt("count"));
            this.skip = tag.getBoolean("skip");
            if (tag.contains("filter"))
                this.filter = ItemStack.parseOptional(level.registryAccess(), tag.getCompound("filter"));
        }

        public int getCount() {
            return count;
        }
        public void setCount(int count) {
            this.count = Math.max(1, count);
            tag.putInt("count", this.count);
        }

        public ItemStack getFilter() {
            return filter;
        }
        public void setFilter(ItemStack filter) {
            this.filter = filter != null ? filter : ItemStack.EMPTY;
            if (this.filter.isEmpty()) tag.remove("filter");
            else tag.put("filter", this.filter.save(level.registryAccess()));
        }

        public boolean getSkip() {
            return skip;
        }
        public void setSkip(boolean skip) {
            this.skip = skip;
            tag.putBoolean("skip", this.skip);
        }

        public Container getContainer(NeoBotEntity bot, double reach) {
            if (target == null || !isSameDimension(bot.level().dimension())) return null;
            if (target.distSqr(bot.blockPosition()) > reach) return null;
            if (bot.level().getBlockEntity(target) instanceof Container container) return container;
            return null;
        }

        @Override public int getCooldown() {
            return 50;
        }

        @Override public void addTooltip(@NotNull List<Component> tooltip) {
            tooltip.add(Component.literal("Withdraw:").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("• Count: " + count).withStyle(ChatFormatting.AQUA));
            if (target != null) tooltip.add(Component.literal("• From: " + target.toShortString()).withStyle(ChatFormatting.AQUA));
            else tooltip.add(Component.literal("• From: Right click on a container to set").withStyle(ChatFormatting.RED));
            if (!filter.isEmpty()) {
                tooltip.add(Component.literal("• Filter:").withStyle(ChatFormatting.GRAY));
                tooltip.add(filter.getHoverName().copy().withStyle(ChatFormatting.YELLOW));
            } else
                tooltip.add(Component.literal("• Filter: Any").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}