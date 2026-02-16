package xyz.agmstudio.neobots.index;

import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.world.item.Item;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.modules.ChargingModule;
import xyz.agmstudio.neobots.modules.DepositModule;
import xyz.agmstudio.neobots.modules.MoveToModule;
import xyz.agmstudio.neobots.modules.WithdrawModule;
import xyz.agmstudio.neobots.upgrades.MemoryUpgradeItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class CNBItems {
    public static void register() {}

    private static final List<ItemEntry<?>> ITEMS = new ArrayList<>();
    private static <T extends Item> ItemEntry<T> register(String name, NonNullFunction<Item.Properties, T> factory, int stack) {
        ItemEntry<T> entry = NeoBots.REGISTRATE.item(name, factory)
                .properties(p -> p.stacksTo(stack))
                .tab(CNBCreativeModeTabs.MAIN.getKey()).register();
        ITEMS.add(entry);
        return entry;
    }
    public static void forEach(Consumer<ItemEntry<?>> consumer) {
        ITEMS.forEach(consumer);
    }

    // Modules
    public static final ItemEntry<DepositModule> DEPOSIT_MODULE = register("deposit_module", DepositModule::new, 1);
    public static final ItemEntry<WithdrawModule> WITHDRAW_MODULE = register("withdraw_module", WithdrawModule::new, 1);
    public static final ItemEntry<MoveToModule> MOVE_TO_MODULE = register("move_to_module", MoveToModule::new, 1);
    public static final ItemEntry<ChargingModule> CHARGING_MODULE = register("charging_module", ChargingModule::new, 1);

    // Upgrades
    public static final ItemEntry<MemoryUpgradeItem> MEMORY_UPGRADE = register("memory_upgrade", MemoryUpgradeItem::new, 16);
}