package xyz.agmstudio.neobots.modules;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.components.WithdrawModuleComponent;
import xyz.agmstudio.neobots.menus.WithdrawModuleMenu;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

import java.util.List;
import java.util.Optional;

public class WithdrawModuleItem extends BotModuleItem implements MenuProvider {
    private static final int REACH_SQR = 4;

    public WithdrawModuleItem(Properties props) {
        super(props);
    }

    /* ---------------- BOT STATE ---------------- */
    private int getProgress(NeoBotEntity bot) {
        return bot.getPersistentData().getInt("WithdrawProgress");
    }
    private void setProgress(NeoBotEntity bot, int value) {
        bot.getPersistentData().putInt("WithdrawProgress", value);
    }
    private void resetProgress(NeoBotEntity bot) {
        bot.getPersistentData().remove("WithdrawProgress");
    }

    /* ---------------- FILTER ---------------- */
    private boolean matchesFilter(ItemStack stack, ItemStack filter) {
        if (stack.isEmpty()) return false;
        if (filter.isEmpty()) return true;

        if (!ItemStack.isSameItem(stack, filter)) return false;
        return stack.getItem() == filter.getItem();  // TODO: Create filters
    }

    /* ---------------- MODULE LIFECYCLE ---------------- */

    @Override public void onStart(NeoBotEntity bot, ItemStack stack) {
        resetProgress(bot);
    }

    @Override public void tick(NeoBotEntity bot, ItemStack stack) {
        WithdrawModuleComponent cfg = stack.get(NeoBots.WITHDRAW.get());
        if (cfg == null) return;
        if (bot.level().isClientSide) return;

        // Wrong dimension
        if (cfg.dimension() != bot.level().dimension()) return;

        // Not in reach → silently wait
        if (bot.blockPosition().distSqr(cfg.source()) > REACH_SQR) return;

        int taken = getProgress(bot);
        if (taken >= cfg.count()) return;

        BlockEntity be = bot.level().getBlockEntity(cfg.source());
        if (!(be instanceof Container container)) return;

        int remaining = cfg.count() - taken;

        for (int i = 0; i < container.getContainerSize(); i++) {
            if (remaining <= 0) break;

            ItemStack slotStack = container.getItem(i);
            if (!matchesFilter(slotStack, cfg.filter().orElse(ItemStack.EMPTY))) continue;

            int toTake = Math.min(slotStack.getCount(), remaining);
            ItemStack extracted = slotStack.split(toTake);

            if (!extracted.isEmpty()) {
                container.setChanged();
                bot.getInventory().addItem(extracted);

                remaining -= extracted.getCount();
                taken += extracted.getCount();
                setProgress(bot, taken);
            }
        }
    }

    @Override public boolean isFinished(NeoBotEntity bot, ItemStack stack) {
        WithdrawModuleComponent cfg = stack.get(NeoBots.WITHDRAW.get());
        if (cfg == null) return true;
        return getProgress(bot) >= cfg.count();
    }

    @Override public void onStop(NeoBotEntity bot, ItemStack stack) {
        resetProgress(bot);
    }

    @Override public @NotNull InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getLevel().isClientSide)
            return InteractionResult.SUCCESS;

        BlockPos pos = ctx.getClickedPos();
        Level level = ctx.getLevel();
        if (!(level.getBlockEntity(pos) instanceof Container))
            return InteractionResult.PASS;

        WithdrawModuleComponent old = ctx.getItemInHand().get(NeoBots.WITHDRAW.get());
        if (old == null) old = new WithdrawModuleComponent(null, null, 1, Optional.empty());
        WithdrawModuleComponent component = old.withSource(pos, level.dimension());

        ctx.getItemInHand().set(NeoBots.WITHDRAW.get(), component);
        if (ctx.getPlayer() instanceof ServerPlayer player) {
            player.displayClientMessage(
                    Component.literal("Source container set")
                            .withStyle(ChatFormatting.GREEN),
                    true
            );
        }

        return InteractionResult.CONSUME;
    }

    @Override public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) sp.openMenu(this, buf -> buf.writeEnum(hand));

        return InteractionResultHolder.consume(stack);
    }

    @Override public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext ctx, @NotNull List<Component> tooltip, @NotNull TooltipFlag flags) {
        WithdrawModuleComponent cfg = stack.get(NeoBots.WITHDRAW.get());
        if (cfg == null) {
            tooltip.add(Component.literal("No source set").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        tooltip.add(Component.literal("Withdraw:").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("• Count: " + cfg.count()).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal(
                "• From: " + cfg.source().getX() + ", "
                        + cfg.source().getY() + ", "
                        + cfg.source().getZ()
        ).withStyle(ChatFormatting.AQUA));

        if (cfg.filter().isPresent()) {
            tooltip.add(Component.literal("• Filter:").withStyle(ChatFormatting.GRAY));
            tooltip.add(cfg.filter().orElse(ItemStack.EMPTY).getHoverName().copy().withStyle(ChatFormatting.YELLOW));
        } else {
            tooltip.add(Component.literal("• Filter: Any").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override public @NotNull Component getDisplayName() {
        return Component.literal("Withdraw Module");
    }

    @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new WithdrawModuleMenu(id, inv);
    }
}
