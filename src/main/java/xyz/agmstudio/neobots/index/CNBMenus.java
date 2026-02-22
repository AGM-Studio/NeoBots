package xyz.agmstudio.neobots.index;

import com.tterrag.registrate.util.entry.MenuEntry;
import xyz.agmstudio.neobots.menus.NeoBotMenu;
import xyz.agmstudio.neobots.menus.TransferModuleMenu;
import xyz.agmstudio.neobots.menus.abstracts.AbstractScreen;

import static xyz.agmstudio.neobots.NeoBots.REGISTRATE;

public final class CNBMenus {
    public static void register() {}

    public static final MenuEntry<NeoBotMenu> NEOBOT_INVENTORY =
            REGISTRATE.menu("neobot_menu", NeoBotMenu::new, () -> AbstractScreen<NeoBotMenu>::new).register();

    public static final MenuEntry<TransferModuleMenu> TRANSFER_MODULE =
            REGISTRATE.menu("transfer_menu", TransferModuleMenu::new, () -> AbstractScreen<TransferModuleMenu>::new).register();
}