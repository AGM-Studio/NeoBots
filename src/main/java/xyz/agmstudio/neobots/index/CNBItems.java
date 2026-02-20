package xyz.agmstudio.neobots.index;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.world.item.Item;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.modules.ChargingModule;
import xyz.agmstudio.neobots.modules.DepositModule;
import xyz.agmstudio.neobots.modules.MoveToModule;
import xyz.agmstudio.neobots.modules.WithdrawModule;
import xyz.agmstudio.neobots.modules.abstracts.item.ModuleItem;
import xyz.agmstudio.neobots.upgrades.InventoryUpgradeItem;
import xyz.agmstudio.neobots.upgrades.MemoryUpgradeItem;
import xyz.agmstudio.neobots.upgrades.UpgradeItem;

public final class CNBItems {
    public static void register() {}

    private static <T extends Item> ItemEntry<T> register(String name, NonNullFunction<Item.Properties, T> factory, int stack, String... aliases) {
        return register(name, factory, stack, null, aliases);
    }
    private static <T extends Item> ItemEntry<T> register(String name, NonNullFunction<Item.Properties, T> factory, int stack, NonNullBiConsumer<DataGenContext<Item, T>, RegistrateRecipeProvider> cons, String... aliases) {
        ItemBuilder<T, CreateRegistrate> builder = NeoBots.REGISTRATE.item(name, factory)
                .properties(p -> p.stacksTo(stack))
                .tab(CNBCreativeModeTabs.MAIN.getKey());
        if (cons != null) builder.recipe(cons);
        for (String alias: aliases) NeoBots.addItemAlias(alias, name);
        return builder.register();
    }

    // Modules
    public static final ItemEntry<DepositModule>    DEPOSIT_MODULE      = register("deposit_module", DepositModule::new, 1, DepositModule::getRecipe);
    public static final ItemEntry<WithdrawModule>   WITHDRAW_MODULE     = register("withdraw_module", WithdrawModule::new, 1, WithdrawModule::getRecipe);
    public static final ItemEntry<MoveToModule>     MOVE_TO_MODULE      = register("move_to_module", MoveToModule::new, 1, MoveToModule::getRecipe);
    public static final ItemEntry<ChargingModule>   CHARGING_MODULE     = register("charging_module", ChargingModule::new, 1, ChargingModule::getRecipe);

    // Upgrades
    public static final ItemEntry<MemoryUpgradeItem.Andesite>    ANDESITE_MEMORY_UPGRADE     = register("andesite_memory_upgrade", MemoryUpgradeItem.Andesite::new, 4, MemoryUpgradeItem.Andesite::getRecipe, "memory_upgrade");
    public static final ItemEntry<MemoryUpgradeItem.Brass>       BRASS_MEMORY_UPGRADE        = register("brass_memory_upgrade", MemoryUpgradeItem.Brass::new, 4, MemoryUpgradeItem.Brass::getRecipe);
    public static final ItemEntry<InventoryUpgradeItem.Andesite> ANDESITE_INVENTORY_UPGRADE  = register("andesite_inventory_upgrade", InventoryUpgradeItem.Andesite::new, 4, InventoryUpgradeItem.Andesite::getRecipe);
    public static final ItemEntry<InventoryUpgradeItem.Brass>    BRASS_INVENTORY_UPGRADE     = register("brass_inventory_upgrade", InventoryUpgradeItem.Brass::new, 4, InventoryUpgradeItem.Brass::getRecipe);

    // Bases
    public static final ItemEntry<Item> INCOMPLETE_BATTERY = NeoBots.REGISTRATE.item("incomplete_battery", Item::new).model(
            (ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.modLoc("block/battery"))
    ).register();

    public static final ItemEntry<Item> BASE_MODULE             = register("andesite_module_base", Item::new, 16, ModuleItem::getBaseRecipe);
    public static final ItemEntry<Item> ANDESITE_UPGRADE_BASE   = register("andesite_upgrade_base", Item::new, 16, UpgradeItem::getAndesiteBaseRecipe);
    public static final ItemEntry<Item> BRASS_UPGRADE_BASE      = register("brass_upgrade_base", Item::new, 16, UpgradeItem::getBrassBaseRecipe);
}