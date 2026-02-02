package xyz.agmstudio.neobots.menus;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import xyz.agmstudio.neobots.NeoBots;

public class NeoBotScreen extends AbstractContainerScreen<NeoBotMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NeoBots.MOD_ID, "textures/gui/neobot.png");
    private static final ResourceLocation SLOT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NeoBots.MOD_ID, "textures/gui/single_slot.png");
    private static final ResourceLocation ACTIVE_SLOT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NeoBots.MOD_ID, "textures/gui/single_slot_active.png");


    public NeoBotScreen(NeoBotMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 255;
        this.imageHeight = 166;
    }

    private Button upgradeButton;

    @Override protected void init() {
        super.init();

        this.upgradeButton = Button.builder(Component.literal("Upgrade"), btn -> menu.getBot().useUpgrade()).bounds(leftPos + 108, topPos + 51, 86, 18).build();
        addRenderableWidget(upgradeButton);
    }

    @Override public void containerTick() {
        super.containerTick();

        double chance = menu.upgradeChance.get() / 10000.0;
        upgradeButton.active = chance > 0;
        upgradeButton.visible = upgradeButton.active;
        if (upgradeButton.active) upgradeButton.setMessage(
                Component.literal("Upgrade (" + (chance * 100) + "%)")
        );
    }

    @Override protected void renderBg(GuiGraphics g, float partialTick, int x, int y) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        int activeModule = menu.activeModule.get();
        for (int i = 0; i < menu.getBot().getModuleInventory().getContainerSize(); i++) {
            int px = 5 + (i % 4) * 18;
            int py = 18 + (i / 4) * 18;
            if (i == activeModule) g.blit(ACTIVE_SLOT_TEXTURE, leftPos + px, topPos + py, 0, 0, 18, 18, 18, 18);
            else g.blit(SLOT_TEXTURE, leftPos + px, topPos + py, 0, 0, 18, 18, 18, 18);
        }
    }

    @Override protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(this.font, menu.getBot().getDisplayName(), 87, 6, 0x404040, false);
        g.drawString(this.font, playerInventoryTitle, 87, 72, 0x404040, false);
        g.drawString(this.font, Component.literal("Modules"), 5, 8, 0x404040, false);
    }

}
