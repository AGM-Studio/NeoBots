package xyz.agmstudio.neobots.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import xyz.agmstudio.neobots.NeoBots;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class Texture {
    public final ResourceLocation texture;
    public final int sizeX, sizeY;
    public Texture(ResourceLocation texture, int sizeX, int sizeY) {
        this.texture = texture;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }
    public Texture(String texture, int sizeX, int sizeY) {
        this(ResourceLocation.fromNamespaceAndPath(NeoBots.MOD_ID, texture), sizeX, sizeY);
    }

    // --- Simple draw, 1:1 ---
    public void draw(GuiGraphics g, int x, int y) {
        g.blit(texture, x, y, 0, 0, sizeX, sizeY, sizeX, sizeY);
    }
    // --- Draw with scaling ---
    public void drawScaled(GuiGraphics g, int x, int y, int width, int height) {
        if (width < 0) width = sizeX;
        if (height < 0) height = sizeY;
        g.blit(texture, x, y, 0, 0, width, height, sizeX, sizeY);
    }
    // --- Draw a sub-region ---
    public void drawRegion(GuiGraphics g, int x, int y, int u, int v, int w, int h) {
        g.blit(texture, x, y, u, v, w, h, sizeX, sizeY);
    }
    // --- Draw a sub-region scaled ---
    public void drawRegion(GuiGraphics g, int x, int y,  int u, int v, int width, int height, int wSrc, int hSrc) {
        if (width < 0) width = wSrc;
        if (height < 0) height = hSrc;
        g.blit(texture, x, y, u, v, width, height, wSrc, hSrc, sizeX, sizeY);
    }

    public ScreenDrawer drawer(int x, int y, boolean drawBeforeBg) {
        return new ScreenDrawer((s, g) -> draw(g, x + s.getGuiLeft(), y + s.getGuiTop()), drawBeforeBg);
    }
    public ScreenDrawer drawer(int x, int y, int width, int height, boolean drawBeforeBg) {
        return new ScreenDrawer((s, g) -> drawScaled(g, x + s.getGuiLeft(), y + s.getGuiTop(), width, height), drawBeforeBg);
    }
    public ScreenDrawer regionDrawer(int x, int y, int u, int v, int w, int h, boolean drawBeforeBg) {
        return new ScreenDrawer((s, g) -> drawRegion(g, x + s.getGuiLeft(), y + s.getGuiTop(), u, v, w, h), drawBeforeBg);
    }
    public ScreenDrawer regionDrawer(int x, int y, int wDraw, int hDraw, int u, int v, int wSrc, int hSrc, boolean drawBeforeBg) {
        return new ScreenDrawer((s, g) -> drawRegion(g, x + s.getGuiLeft(), y + s.getGuiTop(), wDraw, hDraw, u, v, wSrc, hSrc), drawBeforeBg);
    }
}