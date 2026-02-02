package xyz.agmstudio.neobots;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import org.slf4j.LoggerFactory;
import xyz.agmstudio.neobots.components.MoveTarget;
import xyz.agmstudio.neobots.menus.NeoBotMenu;
import xyz.agmstudio.neobots.modules.MoveToModuleItem;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

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

    // Components
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MoveTarget>> MOVE_TARGET =
            COMPONENTS.register("move_target", () ->
                    DataComponentType.<MoveTarget>builder()
                            .persistent(MoveTarget.CODEC)
                            .build()
            );

    // Bots
    public static final DeferredHolder<EntityType<?>, EntityType<NeoBotEntity>> BOT_V0 =
            ENTITIES.register("neobot", () ->
                    EntityType.Builder
                            .of(NeoBotEntity::new, MobCategory.MISC)
                            .sized(0.6f, 1.95f) // villager size
                            .build("neobot_v0")
            );

    // Items
    public static final DeferredHolder<Item, MoveToModuleItem> MOVE_TO_MODULE =
            ITEMS.register("move_to_module", () ->
                    new MoveToModuleItem(
                            new Item.Properties().stacksTo(1)
                    )
            );

    // Menus
    public static final DeferredHolder<MenuType<?>, MenuType<NeoBotMenu>> NEOBOT_INVENTORY =
            MENUS.register("neobot", () ->
                    IMenuTypeExtension.create(NeoBotMenu::new)
            );


    public NeoBots(IEventBus bus, ModContainer container) {
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
