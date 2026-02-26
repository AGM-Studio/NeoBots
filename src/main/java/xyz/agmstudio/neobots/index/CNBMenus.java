package xyz.agmstudio.neobots.index;

import com.tterrag.registrate.util.entry.MenuEntry;
import xyz.agmstudio.neobots.menus.*;

import static xyz.agmstudio.neobots.NeoBots.REGISTRATE;

public final class CNBMenus {
    public static void register() {}

    public static final MenuEntry<NeoBotMenu> NEOBOT_INVENTORY =
            REGISTRATE.menu("neobot_menu", NeoBotMenu::new, () -> NeoBotScreen::new).register();

    public static final MenuEntry<TransferModuleMenu> TRANSFER_MODULE =
            REGISTRATE.menu("transfer_menu", TransferModuleMenu::new, () -> TransferModuleScreen::new).register();
    public static final MenuEntry<ChargingModuleMenu> CHARGING_MODULE =
            REGISTRATE.menu("transfer_menu", ChargingModuleMenu::new, () -> ChargingModuleScreen::new).register();
}