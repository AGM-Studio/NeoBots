package xyz.agmstudio.neobots.index;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.menus.abstracts.AbstractMenu;
import xyz.agmstudio.neobots.menus.abstracts.AbstractScreen;

import java.util.List;

@JeiPlugin
public class CNBJeiPlugin implements IModPlugin {
    @Override public @NotNull ResourceLocation getPluginUid() {
        return NeoBots.rl("jei_plugin");
    }

    @Override public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        //noinspection unchecked,RedundantCast
        registration.addGuiContainerHandler(
                (Class<? extends AbstractScreen<? extends AbstractMenu>>) (Class<?>) AbstractScreen.class,
            new IGuiContainerHandler<AbstractScreen<? extends AbstractMenu>>() {
                @Override public @NotNull List<Rect2i> getGuiExtraAreas(@NotNull AbstractScreen<?> screen) {
                    return List.of(
                        new Rect2i(screen.getGuiLeft(), screen.getGuiTop(), screen.getXSize(), screen.getYSize())
                    );
                }
            }
        );
    }
}