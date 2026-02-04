package xyz.agmstudio.neobots;

import com.mojang.serialization.Codec;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.agmstudio.neobots.menus.AbstractNeoMenu;
import xyz.agmstudio.neobots.menus.NeoBotMenu;
import xyz.agmstudio.neobots.modules.DepositModule;
import xyz.agmstudio.neobots.modules.MoveToModule;
import xyz.agmstudio.neobots.modules.WithdrawModule;
import xyz.agmstudio.neobots.robos.NeoBotEntity;
import xyz.agmstudio.neobots.upgrades.MemoryUpgradeItem;

import java.util.function.Function;
import java.util.function.Supplier;

@Mod(NeoBots.MOD_ID)
public class NeoBots {
    public static final String MOD_ID = "create_neobots";
    public static final String MOD_NAME = "NeoBots";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    // Deferred Registry
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, MOD_ID);

    public static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> registerDataComponent(String name, Codec<T> codec) {
        return COMPONENTS.register(name, () -> DataComponentType.<T>builder().persistent(codec).build());
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
            ITEMS.register("memory_upgrade", () ->
                    new MemoryUpgradeItem(
                            new Item.Properties().stacksTo(1)
                    )
            );

    public static <T extends Item> @NotNull DeferredHolder<Item, T> registerItem(String name, Function<Item.Properties, T> init, int stacksTo) {
        return ITEMS.register(name, () -> init.apply(new Item.Properties().stacksTo(stacksTo)));
    }
    public static <T extends Item> @NotNull DeferredHolder<Item, T> registerItem(String name, Function<Item.Properties, T> init, Supplier<Item.Properties> properties) {
        return ITEMS.register(name, () -> init.apply(properties.get()));
    }

    // Menus
    public static final DeferredHolder<MenuType<?>, MenuType<NeoBotMenu>> NEOBOT_INVENTORY =
            MENUS.register("neobot", () ->
                    IMenuTypeExtension.create(NeoBotMenu::new)
            );

    public static <T extends AbstractNeoMenu, S extends AbstractContainerScreen<T>> @NotNull DeferredHolder<MenuType<?>, MenuType<T>> registerMenu(String name, IContainerFactory<T> factory, MenuScreens.ScreenConstructor<T, S> constructor) {
        DeferredHolder<MenuType<?>, MenuType<T>> menu = MENUS.register(name, () -> IMenuTypeExtension.create(factory));
        ClientSetup.registerScreen(menu, constructor);
        return menu;
    }

    public NeoBots(IEventBus bus, ModContainer container) {
        MoveToModule.register();
        WithdrawModule.register();
        DepositModule.register();

        COMPONENTS.register(bus);
        ENTITIES.register(bus);
        ITEMS.register(bus);
        MENUS.register(bus);

        bus.addListener(this::registerAttributes);
        bus.register(ClientSetup.class);
    }

    public void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(BOT_V0.get(), NeoBotEntity.createAttributes().build());
    }
}