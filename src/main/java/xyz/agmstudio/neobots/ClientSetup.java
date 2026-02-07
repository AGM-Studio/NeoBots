package xyz.agmstudio.neobots;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import xyz.agmstudio.neobots.menus.AbstractMenu;
import xyz.agmstudio.neobots.robos.NeoBotRenderer;

import java.util.ArrayList;
import java.util.List;

public class ClientSetup {
    private static final List<ScreenEntry<?, ?>> SCREENS = new ArrayList<>();
    public static <M extends AbstractMenu, S extends AbstractContainerScreen<M>> void registerScreen(DeferredHolder<MenuType<?>, MenuType<M>> menu, MenuScreens.ScreenConstructor<M, S> constructor) {
        SCREENS.add(new ScreenEntry<>(menu, constructor));
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NeoBots.BOT_V0.get(), NeoBotRenderer::new);
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