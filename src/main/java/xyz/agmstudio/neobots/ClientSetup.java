package xyz.agmstudio.neobots;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import xyz.agmstudio.neobots.block.charger.ChargerRenderer;
import xyz.agmstudio.neobots.index.CNBBlockEntities;
import xyz.agmstudio.neobots.menus.AbstractMenu;

import java.util.ArrayList;
import java.util.List;

public class ClientSetup {
    private static final List<ScreenEntry<?, ?>> SCREENS = new ArrayList<>();
    public static <M extends AbstractMenu, S extends AbstractContainerScreen<M>> void registerScreen(DeferredHolder<MenuType<?>, MenuType<M>> menu, MenuScreens.ScreenConstructor<M, S> constructor) {
        SCREENS.add(new ScreenEntry<>(menu, constructor));
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> BlockEntityRenderers.register(CNBBlockEntities.CHARGER.get(), ChargerRenderer::new));
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        for (ScreenEntry<?, ?> entry: SCREENS) entry.register(event);
    }

    private record ScreenEntry<M extends AbstractMenu, S extends AbstractContainerScreen<M>>(DeferredHolder<MenuType<?>, MenuType<M>> menu, MenuScreens.ScreenConstructor<M, S> constructor) {
        void register(RegisterMenuScreensEvent event) {
            event.register(menu.get(), constructor);
        }
    }
}