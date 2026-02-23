package xyz.agmstudio.neobots.index;

import com.tterrag.registrate.util.entry.MenuEntry;
import xyz.agmstudio.neobots.menus.NeoBotMenu;
import xyz.agmstudio.neobots.menus.NeoBotScreen;
import xyz.agmstudio.neobots.menus.TransferModuleMenu;
import xyz.agmstudio.neobots.menus.TransferModuleScreen;

import static xyz.agmstudio.neobots.NeoBots.REGISTRATE;

public final class CNBMenus {
    public static void register() {}

    public static final MenuEntry<NeoBotMenu> NEOBOT_INVENTORY =
            REGISTRATE.menu("neobot_menu", NeoBotMenu::new, () -> NeoBotScreen::new).register();

    public static final MenuEntry<TransferModuleMenu> TRANSFER_MODULE =
            REGISTRATE.menu("transfer_menu", TransferModuleMenu::new, () -> TransferModuleScreen::new).register();
}