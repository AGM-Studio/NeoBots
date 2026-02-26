package xyz.agmstudio.neobots.menus;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import xyz.agmstudio.neobots.menus.abstracts.AbstractScreen;
import xyz.agmstudio.neobots.menus.gui.Texture;
import xyz.agmstudio.neobots.utils.NeoBotsHelper;

public class TransferModuleScreen extends AbstractScreen<TransferModuleMenu> {
    private static final Texture BG = new Texture("textures/gui/one_slot_panel.png", 176, 203);

    private int count;
    private boolean skip;
    private final IconButton skipButton;

    public TransferModuleScreen(TransferModuleMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);

        this.count = menu.data.getCount();
        this.skip = menu.data.getSkip();

        addPlayerInventoryTitle(8, 110);

        addScrollInput(51, 51, 96, 10).withRange(1, 577)
                .setState(menu.data.getCount())
                .titled(Component.translatable("gui_term.create_neobots.count"))
                .calling(value -> {
                    count = value;
                    sendPacket(0, count);
                });
        skipButton = addIconButton(40, 79, AllIcons.I_SKIP_MISSING).withCallback(() -> {
            skip = !skip;
            sendPacket(0, skip);
            updateIconButtons();
        });
        addIconButton(148, 79, AllIcons.I_CONFIRM).withCallback(() -> {
            sendPacket(1, true);
            menu.getInventory().player.closeContainer();
        });


        addTitleCentered(4).withColor(0x582424);
        addLabel(s -> NeoBotsHelper.countAsStacks(count), 54, 52).withColor(0xffffff).withShadow();

        int targetColor = 0xcc0000;
        Component target = Component.translatable("gui.create_neobots.not_target.container");
        if (menu.data.getTarget() != null) {
            targetColor = 0xffffff;
            target = menu.getInventory().player.level().getBlockState(menu.data.getTarget()).getBlock().getName()
                    .append(Component.literal(" (" + menu.data.getTarget().toShortString() + ")"));
        }
        addLabel(target, 30, 28).withColor(targetColor).withShadow();
    }

    @Override protected boolean isIconButtonActive(IconButton button) {
        return button == skipButton && skip;
    }

    @Override protected Texture getBackground() {
        return BG;
    }
}