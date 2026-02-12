package xyz.agmstudio.neobots;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.energy.ComponentEnergyStorage;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.agmstudio.neobots.index.CNBBlockEntities;
import xyz.agmstudio.neobots.index.CNBBlocks;
import xyz.agmstudio.neobots.index.CNBDataComponents;
import xyz.agmstudio.neobots.item.BatteryItem;
import xyz.agmstudio.neobots.menus.AbstractMenu;
import xyz.agmstudio.neobots.block.charger.ChargerMenu;
import xyz.agmstudio.neobots.menus.NeoBotMenu;
import xyz.agmstudio.neobots.modules.DepositModule;
import xyz.agmstudio.neobots.modules.MoveToModule;
import xyz.agmstudio.neobots.modules.WithdrawModule;
import xyz.agmstudio.neobots.network.NetworkHandler;
import xyz.agmstudio.neobots.robos.NeoBotEntity;
import xyz.agmstudio.neobots.upgrades.MemoryUpgradeItem;

import java.util.function.Function;
import java.util.function.Supplier;

import static xyz.agmstudio.neobots.index.CNBDataComponents.BATTERY_DATA;

@Mod(NeoBots.MOD_ID)
public class NeoBots {
    public static final String MOD_ID = "create_neobots";
    public static final String MOD_NAME = "NeoBots";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID)
            .setTooltipModifierFactory(item ->
                    new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                            .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
            );;

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final Supplier<CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register("neobot_items", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + MOD_ID + ".example"))
            .icon(() -> NeoBots.MEMORY_UPGRADE.get().getDefaultInstance())
            .displayItems((params, output) -> ITEMS.getEntries().forEach(entry -> output.accept(entry.get())))
            .build()
    );

    public static <T extends Item> @NotNull DeferredHolder<Item, T> registerItem(String name, Function<Item.Properties, T> init, int stacksTo) {
        return ITEMS.register(name, () -> init.apply(new Item.Properties().stacksTo(stacksTo)));
    }
    public static <T extends Item> @NotNull DeferredHolder<Item, T> registerItem(String name, Function<Item.Properties, T> init, Supplier<Item.Properties> properties) {
        return ITEMS.register(name, () -> init.apply(properties.get()));
    }

    public static <T extends AbstractMenu> @NotNull DeferredHolder<MenuType<?>, MenuType<T>> registerMenu(String name, IContainerFactory<T> factory) {
        return registerMenu(name, factory, AbstractMenu.Screen<T>::new);
    }
    public static <T extends AbstractMenu, S extends AbstractContainerScreen<T>> @NotNull DeferredHolder<MenuType<?>, MenuType<T>> registerMenu(String name, IContainerFactory<T> factory, MenuScreens.ScreenConstructor<T, S> constructor) {
        DeferredHolder<MenuType<?>, MenuType<T>> menu = MENUS.register(name, () -> IMenuTypeExtension.create(factory));
        ClientSetup.registerScreen(menu, constructor);
        return menu;
    }

    // Bots
    public static final DeferredHolder<EntityType<?>, EntityType<NeoBotEntity>> BOT_V0 =
            ENTITIES.register("neobot", () ->
                    EntityType.Builder
                            .of(NeoBotEntity::new, MobCategory.MISC)
                            .sized(0.6f, 1.95f) // villager size
                            .build("neobot_v0")
            );

    // Items
    public static final DeferredHolder<Item, MemoryUpgradeItem> MEMORY_UPGRADE =
            registerItem("memory_upgrade", MemoryUpgradeItem::new, 16);
    public static final DeferredHolder<Item, BatteryItem> BATTERY =
            registerItem("battery", BatteryItem::new, 16);

    // Menus
    public static final DeferredHolder<MenuType<?>, MenuType<NeoBotMenu>> NEOBOT_INVENTORY =
            registerMenu("neobot_menu", NeoBotMenu::new);
    public static final DeferredHolder<MenuType<?>, MenuType<ChargerMenu>> CHARGER_MENU =
            registerMenu("charger_menu", ChargerMenu::new);

    @Contract("_ -> new")
    public static @NotNull ResourceLocation rl(String value) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, value);
    }
    public NeoBots(IEventBus bus, ModContainer container) {
        MoveToModule.register();
        WithdrawModule.register();
        DepositModule.register();

        CNBDataComponents.register(bus);
        ENTITIES.register(bus);
        ITEMS.register(bus);
        CNBBlocks.register();
        CNBBlockEntities.register();
        MENUS.register(bus);
        CREATIVE_MODE_TABS.register(bus);

        NetworkHandler.registerPackets(bus);
        bus.addListener(this::registerAttributes);
        bus.addListener(this::registerCapabilities);

        bus.register(ClientSetup.class);

        REGISTRATE.registerEventListeners(bus);
    }

    public void registerAttributes(@NotNull EntityAttributeCreationEvent event) {
        event.put(BOT_V0.get(), NeoBotEntity.createAttributes().build());
    }
    public void registerCapabilities(@NotNull RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.EnergyStorage.ITEM,
                (stack, ctx) -> new ComponentEnergyStorage(stack, BATTERY_DATA.get(), 131_072, 256, 256),
                BATTERY.get()
        );
    }
}