package xyz.agmstudio.neobots.menus;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.NeoBots;

public class WithdrawModuleScreen extends AbstractContainerScreen<WithdrawModuleMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NeoBots.MOD_ID, "textures/gui/one_slot_panel.png");

    private int count;

    private Button plusButton;
    private Button minusButton;

    private boolean holdingPlus = false;
    private boolean holdingMinus = false;

    private int holdTicks = 0;
    private static final int HOLD_DELAY = 10;   // ticks before repeat starts
    private static final int HOLD_RATE  = 2;    // repeat every N ticks

    public WithdrawModuleScreen(WithdrawModuleMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 137;
        this.count = menu.getCount();
    }

    @Override protected void init() {
        super.init();

        int x = leftPos + 50;
        int y = topPos + 28;

        minusButton = Button.builder(Component.literal("-"), b -> change(-1))
                .bounds(x, y, 12, 12).build();

        plusButton = Button.builder(Component.literal("+"), b -> change(+1))
                .bounds(x + 27, y, 12, 12).build();

        addRenderableWidget(minusButton);
        addRenderableWidget(plusButton);
    }

    @Override public boolean mouseClicked(double mx, double my, int button) {
        if (minusButton.isMouseOver(mx, my)) {
            holdingMinus = true;
            holdTicks = 0;
        }
        if (plusButton.isMouseOver(mx, my)) {
            holdingPlus = true;
            holdTicks = 0;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override public boolean mouseReleased(double mx, double my, int button) {
        holdingMinus = false;
        holdingPlus = false;
        holdTicks = 0;

        if (minecraft != null && minecraft.gameMode != null)
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, Math.clamp(count, 1, 64));

        return super.mouseReleased(mx, my, button);
    }

    @Override protected void containerTick() {
        super.containerTick();
        if (!holdingMinus && !holdingPlus) return;

        if (++holdTicks < HOLD_DELAY) return;
        if ((holdTicks - HOLD_DELAY) % HOLD_RATE != 0) return;

        if (holdingMinus) change(-1);
        if (holdingPlus) change(+1);
    }

    private void change(int delta) {
        count = count + delta;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
    }

    @Override protected void renderLabels(@NotNull GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, 8, 6, 0x404040, false);
        g.drawString(font, Component.literal("Target:"), 8, 18, 0x404040, false);
        g.drawString(font, Component.literal(menu.getPos().toShortString()), 50, 18, 0x404040, false);
        g.drawString(font, Component.literal("Count:"), 8, 30, 0x404040, false);
        g.drawString(font, Component.literal((count < 10 ? "0": "") + count), 64, 30, 0x404040, false);
        g.drawString(font, playerInventoryTitle, 8, 41, 0x404040, false);
    }
}
