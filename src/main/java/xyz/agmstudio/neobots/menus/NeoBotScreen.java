package xyz.agmstudio.neobots.menus;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import xyz.agmstudio.neobots.menus.abstracts.AbstractScreen;
import xyz.agmstudio.neobots.menus.gui.FrameTexture;
import xyz.agmstudio.neobots.menus.gui.Texture;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

public class NeoBotScreen extends AbstractScreen<NeoBotMenu> {
    private static final Texture BG = new Texture("textures/gui/neobot.png", 224, 215);

    private int state;
    private final IconButton stop;
    private final IconButton start;
    private final IconButton moduleButton;
    private final IconButton upgradeButton;
    private final IconButton botInventoryButton;
    
    public NeoBotScreen(NeoBotMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);

        menu.moduleGroup.client(this)
                .withTexture(i -> i == menu.activeModule.get() ? Texture.ACTIVE_SLOT_TEXTURE : Texture.SLOT_TEXTURE)
                .withTextureSize(20, 20).withTextureOffset(2, 2)
                .addLabel(Component.translatable("gui.create_neobots.modules_tab"), -112, 23).withColor(0x582424).build()
                .withFrame(FrameTexture.BRASS_FRAME).minWidth(128).drawBeforeBg().build();
        menu.upgradeGroup.client(this)
                .withTexture(Texture.UPGRADE_SLOT_TEXTURE).withTextureSize(20, 20).withTextureOffset(2, 2)
                .addLabel(Component.translatable("gui.create_neobots.upgrades_tab"), -112, 23).withColor(0x582424).build()
                .withFrame(FrameTexture.BRASS_FRAME).minWidth(128).drawBeforeBg().build();
        menu.botInventoryGroup.client(this)
                .withTexture(Texture.PACKAGE_SLOT_TEXTURE)
                .addLabel(Component.translatable("gui.create_neobots.bot_inventory"), -86, 23).withColor(0x582424).build()
                .withFrame(FrameTexture.BRASS_FRAME).minWidth(128).offset(12, 7).offsetSize(0, 11) .drawBeforeBg().build()
                .withFrame(FrameTexture.PACKAGE_FRAME).offset(0, -4).drawBeforeBg().build();

        addPlayerInventoryTitle(112, 100).centered().withColor(0x000000);

        state = NeoBotEntity.STATE.get(menu.bot);
        stop  = addIconButton(167,  69, AllIcons.I_PAUSE).withCallback(() -> {
            state = 0; updateIconButtons();
            sendPacket(0, false);
        });
        start = addIconButton(189, 69, AllIcons.I_PLAY).withCallback(() -> {
            state = 1; updateIconButtons();
            sendPacket(0, true);
        });

        addIconButton(139, 69, AllIcons.I_REFRESH).withCallback(() -> {
            state = 1; updateIconButtons();
            sendPacket(1, true);
        });
        moduleButton = addIconButton(17, 69, AllIcons.I_TOOLBOX).withCallback(() -> {
            menu.moduleGroup.setVisible(true);
            menu.upgradeGroup.setVisible(false);
            menu.botInventoryGroup.setVisible(false);
            updateIconButtons();
        });
        moduleButton.setToolTip(Component.translatable("gui.create_neobots.modules_tab"));
        upgradeButton = addIconButton(39, 69, AllIcons.I_PRIORITY_VERY_HIGH).withCallback(() -> {
            menu.moduleGroup.setVisible(false);
            menu.upgradeGroup.setVisible(true);
            menu.botInventoryGroup.setVisible(false);
            updateIconButtons();
        });
        upgradeButton.setToolTip(Component.translatable("gui.create_neobots.upgrades_tab"));
        botInventoryButton = addIconButton(61, 69, AllIcons.I_DICE).withCallback(() -> {
            menu.moduleGroup.setVisible(false);
            menu.upgradeGroup.setVisible(false);
            menu.botInventoryGroup.setVisible(true);
            updateIconButtons();
        });
        botInventoryButton.setToolTip(Component.translatable("gui.create_neobots.bot_inventory"));

        updateIconButtons();

        addLabel(s -> menu.bot.getDisplayName(), 112, 4).withColor(0xffffff).withShadow().centered();
        addLabel(s -> NeoBotEntity.TASK_STATUS.get(menu.bot), 15, 27).withColor(0xffffff).withShadow().width(194);
    }

    @Override protected void updateIconButtons() {
        stop.active = state == 1;
        start.active = state != 1 && state != -2;

        super.updateIconButtons();
    }

    @Override protected boolean isIconButtonActive(IconButton button) {
        if (button == moduleButton) return menu.moduleGroup.isVisible();
        if (button == upgradeButton) return menu.upgradeGroup.isVisible();
        if (button == botInventoryButton) return menu.botInventoryGroup.isVisible();
        return false;
    }

    @Override protected Texture getBackground() {
        return BG;
    }
}