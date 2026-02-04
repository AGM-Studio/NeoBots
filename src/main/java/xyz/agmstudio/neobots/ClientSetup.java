package xyz.agmstudio.neobots;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import xyz.agmstudio.neobots.menus.DepositModuleScreen;
import xyz.agmstudio.neobots.menus.NeoBotScreen;
import xyz.agmstudio.neobots.menus.WithdrawModuleScreen;
import xyz.agmstudio.neobots.robos.NeoBotRenderer;

public class ClientSetup {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NeoBots.BOT_V0.get(), NeoBotRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(NeoBots.NEOBOT_INVENTORY.get(), NeoBotScreen::new);
        event.register(NeoBots.WITHDRAW_MENU.get(), WithdrawModuleScreen::new);
        event.register(NeoBots.DEPOSIT_MENU.get(), DepositModuleScreen::new);
    }
}