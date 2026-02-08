package xyz.agmstudio.neobots.modules;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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
import xyz.agmstudio.neobots.menus.TransferModuleMenu;
import xyz.agmstudio.neobots.modules.abstracts.ModuleItem;
import xyz.agmstudio.neobots.modules.abstracts.ModuleTask;
import xyz.agmstudio.neobots.modules.abstracts.data.ModuleTransferData;
import xyz.agmstudio.neobots.robos.NeoBotEntity;
import xyz.agmstudio.neobots.utils.NeoBotsHelper;

public class DepositModule extends ModuleItem<DepositModule.Data, DepositModule.Task> implements MenuProvider {
    public static DeferredHolder<Item, DepositModule> ITEM = NeoBots.registerItem("deposit_module", DepositModule::new, 1);
    public static DeferredHolder<MenuType<?>, MenuType<Menu>> MENU = NeoBots.registerMenu("deposit_menu", Menu::new);
    public static void register() {}

    private static final int REACH_SQR = 4;

    public DepositModule(Properties props) {
        super("deposit", props, (bot, stack) -> new Task(bot, stack, REACH_SQR), Data::new);
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

        if (!level.isClientSide && player instanceof ServerPlayer sp)
            sp.openMenu(this, buf -> buf.writeEnum(hand));

        return InteractionResultHolder.consume(stack);
    }

    @Override public @NotNull Component getDisplayName() {
        return Component.literal("Deposit Module");
    }

    @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new Menu(id, inv);
    }

    public static class Task extends ModuleTask<Data> {
        private final double reach;
        private int deposited = 0;

        public Task(NeoBotEntity bot, Data data, double reach) {
            super(bot, data);
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

        @Override public void onFinish() {}

        @Override public boolean isDone() {
            if (data.getTarget() == null) return true;
            return deposited >= data.getCount();
        }

        @Override public void tick() {
            if (bot.level().isClientSide) return;
            Container container = data.getContainer(bot, reach);
            if (container == null || deposited >= data.getCount()) return;

            int remaining = data.getCount() - deposited;
            int moved = NeoBotsHelper.moveItems(bot.level(), bot.getInventory(), container, data.getFilter(), remaining);
            if (moved > 0) setDeposited(deposited + moved);
        }

        @Override public Component getStatus() {
            return Component.literal("Depositing (" + deposited + "/" + data.getCount() + ")").withStyle(ChatFormatting.YELLOW);
        }
    }
    public static class Data extends ModuleTransferData {
        public Data(Level level, @NotNull ItemStack stack) {
            super(level, stack);
        }
        @Override protected String getTranslateKey() {
            return "deposit";
        }
    }
    public static class Menu extends TransferModuleMenu<Data> {
        public Menu(int id, Inventory inv, FriendlyByteBuf buf) {
            this(id, inv);
        }
        public Menu(int id, Inventory inv) {
            super(MENU.get(), id, inv, new Data(inv.player.level(), inv.player.getMainHandItem().copy()));
        }
    }
}