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
import xyz.agmstudio.neobots.modules.abstracts.item.ModuleItem;
import xyz.agmstudio.neobots.modules.andesite.AndesiteChargingModule;
import xyz.agmstudio.neobots.modules.andesite.AndesiteDepositModule;
import xyz.agmstudio.neobots.modules.andesite.AndesiteMoveToModule;
import xyz.agmstudio.neobots.modules.andesite.AndesiteWithdrawModule;
import xyz.agmstudio.neobots.modules.brass.BrassDepositModule;
import xyz.agmstudio.neobots.modules.brass.BrassWithdrawModule;
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
    public static final ItemEntry<AndesiteDepositModule> ANDESITE_DEPOSIT_MODULE    = register("andesite_deposit_module", AndesiteDepositModule::new, 1, AndesiteDepositModule::getRecipe, "deposit_module");
    public static final ItemEntry<AndesiteWithdrawModule> ANDESITE_WITHDRAW_MODULE  = register("andesite_withdraw_module", AndesiteWithdrawModule::new, 1, AndesiteWithdrawModule::getRecipe, "withdraw_module");
    public static final ItemEntry<AndesiteMoveToModule> ANDESITE_MOVE_TO_MODULE     = register("andesite_move_to_module", AndesiteMoveToModule::new, 1, AndesiteMoveToModule::getRecipe, "move_to_module");
    public static final ItemEntry<AndesiteChargingModule> ANDESITE_CHARGING_MODULE  = register("andesite_charging_module", AndesiteChargingModule::new, 1, AndesiteChargingModule::getRecipe, "charging_module");

    public static final ItemEntry<BrassDepositModule> BRASS_DEPOSIT_MODULE    = register("brass_deposit_module", BrassDepositModule::new, 1, BrassDepositModule::getRecipe);
    public static final ItemEntry<BrassWithdrawModule> BRASS_WITHDRAW_MODULE  = register("brass_withdraw_module", BrassWithdrawModule::new, 1, BrassWithdrawModule::getRecipe);

    // Upgrades
    public static final ItemEntry<MemoryUpgradeItem.Andesite>    ANDESITE_MEMORY_UPGRADE     = register("andesite_memory_upgrade", MemoryUpgradeItem.Andesite::new, 4, MemoryUpgradeItem.Andesite::getRecipe, "memory_upgrade");
    public static final ItemEntry<MemoryUpgradeItem.Brass>       BRASS_MEMORY_UPGRADE        = register("brass_memory_upgrade", MemoryUpgradeItem.Brass::new, 4, MemoryUpgradeItem.Brass::getRecipe);
    public static final ItemEntry<InventoryUpgradeItem.Andesite> ANDESITE_INVENTORY_UPGRADE  = register("andesite_inventory_upgrade", InventoryUpgradeItem.Andesite::new, 4, InventoryUpgradeItem.Andesite::getRecipe);
    public static final ItemEntry<InventoryUpgradeItem.Brass>    BRASS_INVENTORY_UPGRADE     = register("brass_inventory_upgrade", InventoryUpgradeItem.Brass::new, 4, InventoryUpgradeItem.Brass::getRecipe);

    // Bases
    public static final ItemEntry<Item> INCOMPLETE_BATTERY = NeoBots.REGISTRATE.item("incomplete_battery", Item::new).model(
            (ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.modLoc("block/battery"))
    ).register();

    public static final ItemEntry<Item> ANDESITE_MODULE_BASE    = register("andesite_module_base", Item::new, 16, ModuleItem::getAndesiteBaseRecipe);
    public static final ItemEntry<Item> BRASS_MODULE_BASE       = register("brass_module_base", Item::new, 16, ModuleItem::getBrassBaseRecipe);
    public static final ItemEntry<Item> ANDESITE_UPGRADE_BASE   = register("andesite_upgrade_base", Item::new, 16, UpgradeItem::getAndesiteBaseRecipe);
    public static final ItemEntry<Item> BRASS_UPGRADE_BASE      = register("brass_upgrade_base", Item::new, 16, UpgradeItem::getBrassBaseRecipe);
}