package xyz.agmstudio.neobots.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.menus.AbstractMenu;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiConsumer;

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
    public void draw(GuiGraphics g, int x, int y, int width, int height) {
        if (width < 0) width = sizeX;
        if (height < 0) height = sizeY;
        g.blit(texture, x, y, 0, 0, width, height, sizeX, sizeY);
    }
    // --- Draw a sub-region ---
    public void drawRegion(GuiGraphics g, int x, int y, int u, int v, int w, int h) {
        g.blit(texture, x, y, u, v, w, h, sizeX, sizeY);
    }
    // --- Draw a sub-region scaled ---
    public void drawRegion(GuiGraphics g, int x, int y, int width, int height, int u, int v, int wSrc, int hSrc) {
        if (width < 0) width = wSrc;
        if (height < 0) height = hSrc;
        g.blit(texture, x, y, u, v, width, height, sizeX, sizeY);
    }
    // --- Draw as a scaled frame (9-slice) ---
    public void drawFrame(GuiGraphics g, int x, int y, int width, int height, int corner) {
        drawFrame(g, x, y, width, height, corner, corner);
    }
    public void drawFrame(GuiGraphics g, int x, int y, int width, int height, int cw, int ch) {
        int midSrcW = sizeX - 2 * cw;
        int midSrcH = sizeY - 2 * ch;

        int midDstW = width - 2 * cw;
        int midDstH = height - 2 * ch;

        // ---- Corners (no scaling) ----
        g.blit(texture, x, y, 0, 0, cw, ch, sizeX, sizeY);
        g.blit(texture, x + width - cw, y, sizeX - cw, 0, cw, ch, sizeX, sizeY);
        g.blit(texture, x, y + height - ch, 0, sizeY - ch, cw, ch, sizeX, sizeY);
        g.blit(texture, x + width - cw, y + height - ch, sizeX - cw, sizeY - ch, cw, ch, sizeX, sizeY);

        // ---- Edges (scale in 1D) ----
        g.blit(texture, x + cw, y, midDstW, ch, cw, 0, midSrcW, ch, sizeX, sizeY);
        g.blit(texture, x + cw, y + height - ch, midDstW, ch, cw, sizeY - ch, midSrcW, ch, sizeX, sizeY);
        g.blit(texture, x, y + ch, cw, midDstH, 0, ch, cw, midSrcH, sizeX, sizeY);
        g.blit(texture, x + width - cw, y + ch, cw, midDstH, sizeX - cw, ch, cw, midSrcH, sizeX, sizeY);

        // ---- Center (scale in 2D) ----
        g.blit(texture, x + cw, y + ch, midDstW, midDstH, cw, ch, midSrcW, midSrcH, sizeX, sizeY);
    }
    // --- Draw as a tiled frame (9-slice) ---
    public void drawFrameTiled(GuiGraphics g, int x, int y, int width, int height, int corner) {
        drawFrameTiled(g, x, y, width, height, corner, corner);
    }
    public void drawFrameTiled(GuiGraphics g, int x, int y, int width, int height, int cw, int ch) {
        int midSrcW = sizeX - 2 * cw;
        int midSrcH = sizeY - 2 * ch;
        int midDstW = width - 2 * cw;
        int midDstH = height - 2 * ch;

        // ---- Corners ----
        g.blit(texture, x, y, 0, 0, cw, ch, sizeX, sizeY); // Top-left
        g.blit(texture, x + width - cw, y, sizeX - cw, 0, cw, ch, sizeX, sizeY); // Top-right
        g.blit(texture, x, y + height - ch, 0, sizeY - ch, cw, ch, sizeX, sizeY); // Bottom-left
        g.blit(texture, x + width - cw, y + height - ch, sizeX - cw, sizeY - ch, cw, ch, sizeX, sizeY); // Bottom-right

        // ---- Edges ----
        // Top and Bottom edges (tile horizontally)
        for (int tx = 0; tx < midDstW; tx += midSrcW) {
            int w = Math.min(midSrcW, midDstW - tx); // last tile may be clipped
            g.blit(texture, x + cw + tx, y, cw, 0, w, ch, sizeX, sizeY); // Top
            g.blit(texture, x + cw + tx, y + height - ch, cw, sizeY - ch, w, ch, sizeX, sizeY); // Bottom
        }

        // Left and Right edges (tile vertically)
        for (int ty = 0; ty < midDstH; ty += midSrcH) {
            int h = Math.min(midSrcH, midDstH - ty);
            g.blit(texture, x, y + ch + ty, 0, ch, cw, h, sizeX, sizeY); // Left
            g.blit(texture, x + width - cw, y + ch + ty, sizeX - cw, ch, cw, h, sizeX, sizeY); // Right
        }

        // ---- Center (tile in 2D) ----
        for (int ty = 0; ty < midDstH; ty += midSrcH) {
            int h = Math.min(midSrcH, midDstH - ty);
            for (int tx = 0; tx < midDstW; tx += midSrcW) {
                int w = Math.min(midSrcW, midDstW - tx);
                g.blit(texture, x + cw + tx, y + ch + ty, cw, ch, w, h, sizeX, sizeY);
            }
        }
    }


    public record Drawer(BiConsumer<AbstractMenu.Screen<?>, GuiGraphics> drawer, boolean drawBeforeBg) {
        public void draw(AbstractMenu.Screen<?> screen, GuiGraphics g) {
            drawer.accept(screen, g);
        }
    }
    public Drawer drawer(int x, int y, boolean drawBeforeBg) {
        return new Drawer((s, g) -> draw(g, x + s.getGuiLeft(), y + s.getGuiTop()), drawBeforeBg);
    }
    public Drawer drawer(int x, int y, int width, int height, boolean drawBeforeBg) {
        return new Drawer((s, g) -> draw(g, x + s.getGuiLeft(), y + s.getGuiTop(), width, height), drawBeforeBg);
    }
    public Drawer regionDrawer(int x, int y, int u, int v, int w, int h, boolean drawBeforeBg) {
        return new Drawer((s, g) -> drawRegion(g, x + s.getGuiLeft(), y + s.getGuiTop(), u, v, w, h), drawBeforeBg);
    }
    public Drawer regionDrawer(int x, int y, int wDraw, int hDraw, int u, int v, int wSrc, int hSrc, boolean drawBeforeBg) {
        return new Drawer((s, g) -> drawRegion(g, x + s.getGuiLeft(), y + s.getGuiTop(), wDraw, hDraw, u, v, wSrc, hSrc), drawBeforeBg);
    }
    public Drawer frameDrawer(int x, int y, int width, int height, int corner, boolean tiled, boolean drawBeforeBg) {
        if (tiled) return new Drawer((s, g) -> drawFrameTiled(g, x + s.getGuiLeft(), y + s.getGuiTop(), width, height, corner), drawBeforeBg);
        return new Drawer((s, g) -> drawFrame(g, x + s.getGuiLeft(), y + s.getGuiTop(), width, height, corner), drawBeforeBg);
    }
    public Drawer frameDrawer(int x, int y, int width, int height, int cw, int ch, boolean tiled, boolean drawBeforeBg) {
        if (tiled) return new Drawer((s, g) -> drawFrameTiled(g, x + s.getGuiLeft(), y + s.getGuiTop(), width, height, cw, ch), drawBeforeBg);
        return new Drawer((s, g) -> drawFrame(g, x + s.getGuiLeft(), y + s.getGuiTop(), width, height, cw, ch), drawBeforeBg);
    }
}