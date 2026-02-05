package xyz.agmstudio.neobots.modules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.menus.modules.DepositModuleMenu;
import xyz.agmstudio.neobots.robos.NeoBotEntity;
import xyz.agmstudio.neobots.utils.NeoBotsHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

public interface DepositModule {
    DeferredHolder<DataComponentType<?>, DataComponentType<DataComponent>> COMPONENT = NeoBots.registerDataComponent("deposit_data", DataComponent.CODEC);
    DeferredHolder<Item, ModuleItem> ITEM = NeoBots.registerItem("deposit_module", ModuleItem::new, 1);
    DeferredHolder<MenuType<?>, MenuType<DepositModuleMenu>> MENU = NeoBots.registerMenu("deposit_menu", DepositModuleMenu::new);
    static void register() {
    }

    @ParametersAreNonnullByDefault
    record DataComponent(Optional<BlockPos> target, Optional<ResourceKey<Level>> dimension, int count, Optional<ItemStack> filter) {
        public static DataComponent getDefault() {
            return new DataComponent(Optional.empty(), Optional.empty(), 1, Optional.empty());
        }
        public static final Codec<DataComponent> CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        BlockPos.CODEC.optionalFieldOf("target").forGetter(DataComponent::target),
                        Level.RESOURCE_KEY_CODEC.optionalFieldOf("dimension").forGetter(DataComponent::dimension),
                        Codec.INT.fieldOf("count").forGetter(DataComponent::count),
                        ItemStack.CODEC.optionalFieldOf("filter").forGetter(DataComponent::filter)
                ).apply(instance, DataComponent::new));

        @Contract("_, _ -> new")
        public @NotNull DepositModule.DataComponent withTarget(@Nullable BlockPos target, @Nullable ResourceKey<Level> dimension) {
            return new DataComponent(Optional.ofNullable(target), Optional.ofNullable(dimension), this.count, this.filter);
        }
        @Contract("_ -> new")
        public @NotNull DepositModule.DataComponent withCount(int count) {
            return new DataComponent(this.target, this.dimension, count, this.filter);
        }
        @Contract("_ -> new")
        public @NotNull DepositModule.DataComponent withFilter(@NotNull ItemStack filter) {
            return new DataComponent(this.target, this.dimension, this.count, filter.isEmpty() ? Optional.empty() : Optional.of(filter.copy()));
        }
        
        public @Nullable Container getContainer(NeoBotEntity bot, double distSqr) {
            if (this.target.isEmpty() || this.dimension.isEmpty()) return null;
            if (bot.level().dimension() != this.dimension.get() || this.target.get().distSqr(bot.blockPosition()) > distSqr) return null;
            if (bot.level().getBlockEntity(this.target.get()) instanceof Container container) return container;
            return null;
        }
        
        public static @NotNull DataComponent extract(ItemStack stack) {
            DataComponent component = stack.get(COMPONENT.get());
            return component != null ? component : getDefault();
        }
    }
    class ModuleItem extends BotModuleItem<ModuleTask> implements MenuProvider {
        private static final int REACH_SQR = 4;

        public ModuleItem(Properties props) {
            super(props, (bot, stack) -> new ModuleTask(bot, stack, REACH_SQR));
        }

        @Override public @NotNull InteractionResult useOn(UseOnContext ctx) {
            if (ctx.getLevel().isClientSide)
                return InteractionResult.SUCCESS;

            BlockPos pos = ctx.getClickedPos();
            Level level = ctx.getLevel();

            if (!(level.getBlockEntity(pos) instanceof Container))
                return InteractionResult.PASS;

            DataComponent component = DataComponent.extract(ctx.getItemInHand()).withTarget(pos, level.dimension());
            ctx.getItemInHand().set(COMPONENT.get(), component);

            if (ctx.getPlayer() instanceof ServerPlayer player) {
                player.displayClientMessage(
                        Component.literal("Target container set")
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
        public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext ctx, @NotNull List<Component> tooltip, @NotNull TooltipFlag flags) {
            DataComponent cfg = stack.get(COMPONENT.get());
            if (cfg == null) {
                tooltip.add(Component.literal("No target set").withStyle(ChatFormatting.DARK_GRAY));
                return;
            }

            tooltip.add(Component.literal("Deposit:").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("• Count: " + cfg.count()).withStyle(ChatFormatting.AQUA));
            if (cfg.target().isPresent()) tooltip.add(Component.literal("• To: " + cfg.target().get().toShortString()).withStyle(ChatFormatting.AQUA));
            else tooltip.add(Component.literal("• To: Not set (Right click on container to set)").withStyle(ChatFormatting.RED));
            if (cfg.filter().isPresent()) {
                tooltip.add(Component.literal("• Filter:").withStyle(ChatFormatting.GRAY));
                tooltip.add(
                        cfg.filter().orElse(ItemStack.EMPTY)
                                .getHoverName()
                                .copy()
                                .withStyle(ChatFormatting.YELLOW)
                );
            } else {
                tooltip.add(Component.literal("• Filter: Any").withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        @Override public @NotNull Component getDisplayName() {
            return Component.literal("Deposit Module");
        }

        @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
            return new DepositModuleMenu(id, inv);
        }
    }

    class ModuleTask extends BotTask {
        private final DataComponent data;
        private final double reach;
        private int deposited = 0;

        public ModuleTask(NeoBotEntity bot, ItemStack module, double reach) {
            super(bot, module);
            data = DataComponent.extract(module);
            this.reach = reach;
        }

        @Override public String getType() {
            return "deposit";
        }

        @Override public void load(@NotNull CompoundTag tag) {
            this.deposited = tag.getInt("deposited");
        }

        @Override public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("deposited", deposited);
            return tag;
        }

        private void setDeposited(int value) {
            this.deposited = value;
            this.setDirty();
        }

        @Override public void onStart() {}

        @Override public void onStop() {}

        @Override public boolean isDone() {
            if (data.target.isEmpty()) return true;
            return deposited >= data.count();
        }

        @Override public void tick() {
            if (bot.level().isClientSide) return;
            Container container = data.getContainer(bot, reach);
            if (container == null || deposited >= data.count()) return;

            int remaining = data.count() - deposited;
            int moved = NeoBotsHelper.moveItems(bot.level(), bot.getInventory(), container, data.filter.orElse(ItemStack.EMPTY), remaining);
            if (moved > 0) setDeposited(deposited + moved);
        }

        @Override public Component getStatus() {
            return Component.literal("Depositing (" + deposited + "/" + data.count + ")").withStyle(ChatFormatting.YELLOW);
        }
    }
}