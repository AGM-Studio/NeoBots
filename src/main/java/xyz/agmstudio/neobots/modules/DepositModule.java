package xyz.agmstudio.neobots.modules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.menus.DepositModuleMenu;
import xyz.agmstudio.neobots.menus.DepositModuleScreen;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

import java.util.List;
import java.util.Optional;

public interface DepositModule {
    DeferredHolder<DataComponentType<?>, DataComponentType<DataComponent>> COMPONENT = NeoBots.registerDataComponent("deposit_data", DataComponent.CODEC);
    DeferredHolder<Item, ModuleItem> ITEM = NeoBots.registerItem("deposit_module", ModuleItem::new, 1);
    DeferredHolder<MenuType<?>, MenuType<DepositModuleMenu>> MENU = NeoBots.registerMenu("deposit_menu", DepositModuleMenu::new, DepositModuleScreen::new);
    static void register() {
    }

    record DataComponent(Optional<BlockPos> target, ResourceKey<Level> dimension, int count, Optional<ItemStack> filter) {
        public static final Codec<DataComponent> CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        BlockPos.CODEC.optionalFieldOf("target").forGetter(DataComponent::target),
                        Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(DataComponent::dimension),
                        Codec.INT.fieldOf("count").forGetter(DataComponent::count),
                        ItemStack.CODEC.optionalFieldOf("filter").forGetter(DataComponent::filter)
                ).apply(instance, DataComponent::new));

        @Contract("_, _ -> new")
        public @NotNull DepositModule.DataComponent withTarget(BlockPos target, ResourceKey<Level> dimension) {
            return new DataComponent(Optional.ofNullable(target), dimension, this.count, this.filter);
        }
        @Contract("_ -> new")
        public @NotNull DepositModule.DataComponent withCount(int count) {
            return new DataComponent(this.target, this.dimension, count, this.filter);
        }
        @Contract("_ -> new")
        public @NotNull DepositModule.DataComponent withFilter(@NotNull ItemStack filter) {
            return new DataComponent(this.target, this.dimension, this.count, filter.isEmpty() ? Optional.empty() : Optional.of(filter.copy()));
        }
    }
    class ModuleItem extends BotModuleItem implements MenuProvider {
        private static final int REACH_SQR = 4;

        public ModuleItem(Properties props) {
            super(props);
        }

        /* ---------------- BOT STATE ---------------- */
        private int getProgress(NeoBotEntity bot) {
            return bot.getPersistentData().getInt("DepositProgress");
        }
        private void setProgress(NeoBotEntity bot, int value) {
            bot.getPersistentData().putInt("DepositProgress", value);
        }
        private void resetProgress(NeoBotEntity bot) {
            bot.getPersistentData().remove("DepositProgress");
        }

        /* ---------------- FILTER ---------------- */
        private boolean matchesFilter(ItemStack stack, ItemStack filter) {
            if (stack.isEmpty()) return false;
            if (filter.isEmpty()) return true;

            if (!ItemStack.isSameItem(stack, filter)) return false;
            return stack.getItem() == filter.getItem(); // TODO: Create filters
        }

        /* ---------------- MODULE LIFECYCLE ---------------- */
        @Override public void onStart(NeoBotEntity bot, ItemStack stack) {
            resetProgress(bot);
        }

        @Override public void tick(NeoBotEntity bot, ItemStack stack) {
            DataComponent cfg = stack.get(COMPONENT.get());
            if (cfg == null) return;
            if (bot.level().isClientSide) return;

            if (cfg.dimension() != bot.level().dimension()) return;
            if (cfg.target().isEmpty() || bot.blockPosition().distSqr(cfg.target().get()) > REACH_SQR) return;

            int deposited = getProgress(bot);
            if (deposited >= cfg.count()) return;

            BlockEntity be = bot.level().getBlockEntity(cfg.target().get());
            if (!(be instanceof Container container)) return;

            int remaining = cfg.count() - deposited;

            // Iterate bot inventory
            for (int i = 0; i < bot.getInventory().getContainerSize(); i++) {
                if (remaining <= 0) break;

                ItemStack botStack = bot.getInventory().getItem(i);
                if (botStack.isEmpty()) continue;
                if (!matchesFilter(botStack, cfg.filter().orElse(ItemStack.EMPTY))) continue;

                // Try merge into existing stacks first
                for (int s = 0; s < container.getContainerSize() && remaining > 0; s++) {
                    ItemStack target = container.getItem(s);
                    if (target.isEmpty()) continue;
                    if (!ItemStack.isSameItemSameComponents(botStack, target)) continue;

                    int space = target.getMaxStackSize() - target.getCount();
                    if (space <= 0) continue;

                    int move = Math.min(space, Math.min(botStack.getCount(), remaining));
                    target.grow(move);
                    botStack.shrink(move);

                    remaining -= move;
                    deposited += move;
                }

                // Then try empty slots
                for (int s = 0; s < container.getContainerSize() && remaining > 0; s++) {
                    ItemStack target = container.getItem(s);
                    if (!target.isEmpty()) continue;

                    int move = Math.min(botStack.getCount(), remaining);
                    ItemStack placed = botStack.split(move);
                    container.setItem(s, placed);

                    remaining -= move;
                    deposited += move;
                }

                if (botStack.isEmpty())
                    bot.getInventory().setItem(i, ItemStack.EMPTY);
            }

            if (deposited > getProgress(bot)) {
                setProgress(bot, deposited);
                container.setChanged();
            }
        }

        @Override public boolean isFinished(NeoBotEntity bot, ItemStack stack) {
            DataComponent cfg = stack.get(COMPONENT.get());
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

            DataComponent old = ctx.getItemInHand().get(COMPONENT.get());
            if (old == null) old = new DataComponent(null, null, 1, Optional.empty());

            DataComponent component =
                    old.withTarget(pos, level.dimension());

            ctx.getItemInHand().set(COMPONENT.get(), component);

            if (ctx.getPlayer() instanceof ServerPlayer player) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("Target container set")
                                .withStyle(ChatFormatting.GREEN),
                        true
                );
            }

            return InteractionResult.CONSUME;
        }

        @Override public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);

            if (!level.isClientSide && player instanceof ServerPlayer sp)
                sp.openMenu(this, buf -> buf.writeEnum(hand));

            return InteractionResultHolder.consume(stack);
        }

        /* ---------------- TOOLTIP ---------------- */

        @Override
        public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext ctx, @NotNull List<net.minecraft.network.chat.Component> tooltip, @NotNull TooltipFlag flags) {
            DataComponent cfg = stack.get(COMPONENT.get());
            if (cfg == null) {
                tooltip.add(net.minecraft.network.chat.Component.literal("No target set").withStyle(ChatFormatting.DARK_GRAY));
                return;
            }

            tooltip.add(net.minecraft.network.chat.Component.literal("Deposit:").withStyle(ChatFormatting.GRAY));
            tooltip.add(net.minecraft.network.chat.Component.literal("• Count: " + cfg.count()).withStyle(ChatFormatting.AQUA));
            if (cfg.target().isPresent()) tooltip.add(net.minecraft.network.chat.Component.literal("• To: " + cfg.target().get().toShortString()).withStyle(ChatFormatting.AQUA));
            else tooltip.add(net.minecraft.network.chat.Component.literal("• To: Not set (Right click on container to set)").withStyle(ChatFormatting.RED));
            if (cfg.filter().isPresent()) {
                tooltip.add(net.minecraft.network.chat.Component.literal("• Filter:").withStyle(ChatFormatting.GRAY));
                tooltip.add(
                        cfg.filter().orElse(ItemStack.EMPTY)
                                .getHoverName()
                                .copy()
                                .withStyle(ChatFormatting.YELLOW)
                );
            } else {
                tooltip.add(net.minecraft.network.chat.Component.literal("• Filter: Any").withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        @Override public @NotNull net.minecraft.network.chat.Component getDisplayName() {
            return net.minecraft.network.chat.Component.literal("Deposit Module");
        }

        @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
            return new DepositModuleMenu(id, inv);
        }
    }
}