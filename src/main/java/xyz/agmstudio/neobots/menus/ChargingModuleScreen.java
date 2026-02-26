package xyz.agmstudio.neobots.menus;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import xyz.agmstudio.neobots.menus.abstracts.AbstractScreen;
import xyz.agmstudio.neobots.menus.gui.Texture;
import xyz.agmstudio.neobots.utils.NeoBotsHelper;

import java.util.List;

public class ChargingModuleScreen extends AbstractScreen<ChargingModuleMenu> {
    private static final Texture BG = new Texture("textures/gui/charging_panel.png", 176, 203);

    private int mode;
    private int value;
    private boolean skip;
    private int skipValue;

    private final IconButton skipButton;
    private final ScrollInput skipInput;
    private ScrollInput valueInput = null;

    public ChargingModuleScreen(ChargingModuleMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);

        this.mode = menu.data.getMode();
        this.value = menu.data.getValue();
        this.skip = menu.data.getSkip();
        this.skipValue = menu.data.getSkipValue();

        addPlayerInventoryTitle(8, 110);

        addSelectionScrollInput(95, 27, 52, 10, List.of(
                Component.translatable("gui_term.create_neobots.percent"),
                Component.translatable("gui_term.create_neobots.seconds")
        )).titled(Component.translatable("gui_term.create_neobots.mode"))
                .setState(mode)
                .calling(mode -> {
                    this.mode = mode;
                    if (this.mode == 0 && value > 100) {
                        if (valueInput != null) valueInput.setState(100);
                        sendPacket(1, 100);
                        value = 100;
                    }
                    sendPacket(0, mode);
                });
        valueInput = addScrollInput(27, 27, 52, 10).withRange(0, 3600)
                .setState(value)
                .titled(Component.translatable("gui_term.create_neobots.value"))
                .calling(value -> {
                    if (mode == 0 && value > 100) {
                        if (valueInput != null) valueInput.setState(100);
                        this.value = 100;
                    } else this.value = value;
                    sendPacket(1, this.value);
                });
        skipInput = addScrollInput(51, 51, 96, 10).withRange(0, 100)
                .titled(Component.translatable("gui_term.create_neobots.value"))
                .calling(value -> {
                    this.skipValue = value;
                    sendPacket(2, this.skipValue);
                });
        skipInput.visible = skip;

        skipButton = addIconButton(25, 47, AllIcons.I_SKIP_MISSING).withCallback(() -> {
            skip = !skip;
            skipInput.visible = skip;
            sendPacket(0, skip);
            updateIconButtons();
        });
        addIconButton(148, 79, AllIcons.I_CONFIRM).withCallback(() -> {
            sendPacket(1, true);
            menu.getInventory().player.closeContainer();
        });


        addTitleCentered(4).withColor(0x582424);
        addLabel(s -> skip ?
                Component.translatable("gui_term.create_neobots.skip_if_above", skipValue + "%") :
                Component.translatable("gui_term.create_neobots.disabled"), 54, 52).withColor(() -> skip ? 0xffffff : 0x404040).withShadow();
        addLabel(s -> mode == 0 ?
                Component.translatable("gui_term.create_neobots.percent") :
                Component.translatable("gui_term.create_neobots.seconds"), 98, 28).withColor(0xffffff).withShadow();
        addLabel(s -> mode == 0 ?
                Component.literal(value + "%") :
                NeoBotsHelper.formatSeconds(value), 30, 28).withColor(0xffffff).withShadow();
    }

    @Override protected boolean isIconButtonActive(IconButton button) {
        return button == skipButton && skip;
    }

    @Override protected Texture getBackground() {
        return BG;
    }
}