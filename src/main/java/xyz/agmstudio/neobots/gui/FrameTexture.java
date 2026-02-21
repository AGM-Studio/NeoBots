package xyz.agmstudio.neobots.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import xyz.agmstudio.neobots.NeoBots;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class FrameTexture {
    public final Texture texture;
    public final int top, bottom, left, right;

    public FrameTexture(ResourceLocation texture, int sizeX, int sizeY, int top, int bottom, int left, int right) {
        this.texture = new Texture(texture, sizeX, sizeY);
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
    }
    public FrameTexture(ResourceLocation texture, int sizeX, int sizeY, int top, int side) {
        this(texture, sizeX, sizeY, top, top, side, side);
    }
    public FrameTexture(ResourceLocation texture, int sizeX, int sizeY, int corner) {
        this(texture, sizeX, sizeY, corner, corner, corner, corner);
    }
    public FrameTexture(String texture, int sizeX, int sizeY, int top, int bottom, int left, int right) {
        this(NeoBots.rl(texture), sizeX, sizeY, top, bottom, left, right);
    }
    public FrameTexture(String texture, int sizeX, int sizeY, int top, int side) {
        this(NeoBots.rl(texture), sizeX, sizeY, top, side);
    }
    public FrameTexture(String texture, int sizeX, int sizeY, int corner) {
        this(NeoBots.rl(texture), sizeX, sizeY, corner);
    }

    private void drawCorners(GuiGraphics g, int x, int y, int width, int height) {
        // Top-left
        texture.drawRegion(g, x, y, 0, 0, left, top);
        // Top-right
        texture.drawRegion(g, x + width - right, y, texture.sizeX - right, 0, right, top);
        // Bottom-left
        texture.drawRegion(g, x, y + height - bottom, 0, texture.sizeY - bottom, left, bottom);
        // Bottom-right
        texture.drawRegion(g, x + width - right, y + height - bottom, texture.sizeX - right, texture.sizeY - bottom, right, bottom);
    }

    public void draw(GuiGraphics g, int x, int y, int width, int height, boolean tiled) {
        if (tiled) drawTiled(g, x, y, width, height);
        else drawStretched(g, x, y, width, height);
    }
    private void drawStretched(GuiGraphics g, int x, int y, int width, int height) {
        int midSrcW = texture.sizeX - left - right;
        int midSrcH = texture.sizeY - top - bottom;

        width = Math.max(width, left + right);
        height = Math.max(height, top + bottom);
        int midDstW = width - left - right;
        int midDstH = height - top - bottom;

        this.drawCorners(g, x, y, width, height);
        // Top edge
        texture.drawRegion(g, x + left, y, midDstW, top, left, 0, midSrcW, top);
        // Bottom edge
        texture.drawRegion(g, x + left, y + height - bottom, midDstW, bottom, left, texture.sizeY - bottom, midSrcW, bottom);
        // Left edge
        texture.drawRegion(g, x, y + top, left, midDstH, 0, top, left, midSrcH);
        // Right edge
        texture.drawRegion(g, x + width - right, y + top, right, midDstH, texture.sizeX - right, top, right, midSrcH);
        // Center
        texture.drawRegion(g, x + left, y + top, midDstW, midDstH, left, top, midSrcW, midSrcH);
    }
    public void drawTiled(GuiGraphics g, int x, int y, int width, int height) {
        int midSrcW = texture.sizeX - left - right;
        int midSrcH = texture.sizeY - top - bottom;

        width = Math.max(width, left + right);
        height = Math.max(height, top + bottom);
        int midDstW = width - left - right;
        int midDstH = height - top - bottom;

        // ---- Corners ----
        this.drawCorners(g, x, y, width, height);

        // ---- Top & Bottom Edges (tile horizontally) ----
        for (int dx = 0; dx < midDstW; dx += midSrcW) {
            int tileW = Math.min(midSrcW, midDstW - dx);
            texture.drawRegion(g, x + left + dx, y, tileW, top, left, 0, tileW, top);
            texture.drawRegion(g, x + left + dx, y + height - bottom, tileW, bottom, left, texture.sizeY - bottom, tileW, bottom);
        }

        // ---- Left & Right Edges (tile vertically) ----
        for (int dy = 0; dy < midDstH; dy += midSrcH) {
            int tileH = Math.min(midSrcH, midDstH - dy);
            texture.drawRegion(g, x, y + top + dy, left, tileH, 0, top, left, tileH);
            texture.drawRegion(g, x + width - right, y + top + dy, right, tileH, texture.sizeX - right, top, right, tileH);
        }

        // ---- Center (tile both axes) ----
        for (int dx = 0; dx < midDstW; dx += midSrcW) {
            int tileW = Math.min(midSrcW, midDstW - dx);
            for (int dy = 0; dy < midDstH; dy += midSrcH) {
                int tileH = Math.min(midSrcH, midDstH - dy);
                texture.drawRegion(g, x + left + dx, y + top + dy, tileW, tileH, left, top, tileW, tileH);
            }
        }
    }

    public void drawAround(GuiGraphics g, int x, int y, int width, int height, boolean tiled) {
        draw(g, x - left, y - top, width + left + right, height + top + bottom, tiled);
    }

    public ScreenDrawer drawer(int x, int y, int width, int height, boolean tiled, boolean drawBeforeBg) {
        return new ScreenDrawer((s, g) -> draw(g, x + s.getGuiLeft(), y + s.getGuiTop(), width, height, tiled), drawBeforeBg);
    }
    public ScreenDrawer drawerAround(int x, int y, int width, int height, boolean tiled, boolean drawBeforeBg) {
        return new ScreenDrawer((s, g) -> drawAround(g, x + s.getGuiLeft(), y + s.getGuiTop(), width, height, tiled), drawBeforeBg);
    }
}