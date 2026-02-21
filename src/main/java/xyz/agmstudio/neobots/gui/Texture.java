package xyz.agmstudio.neobots.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import xyz.agmstudio.neobots.NeoBots;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class Texture implements Drawable {
    public final ResourceLocation texture;
    public final int sizeX, sizeY;
    public final int u, v, w, h;            // UV Values
    public final int width, height;
    public final boolean tiled;
    private Texture(ResourceLocation texture, int sizeX, int sizeY, int width, int height, int u, int v, int w, int h, boolean tiled) {
        this.texture = texture;
        this.tiled = tiled;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.width = width;
        this.height = height;
        this.u = u;
        this.v = v;
        this.w = w;
        this.h = h;
    }
    public Texture(ResourceLocation texture, int sizeX, int sizeY) {
        this(texture, sizeX, sizeY, 0, 0, 0, 0, sizeX, sizeY, false);
    }
    public Texture(String texture, int sizeX, int sizeY) {
        this(NeoBots.rl(texture), sizeX, sizeY, 0, 0, 0, 0, sizeX, sizeY, false);
    }

    public Texture region(int u, int v, int w, int h) {
        if (u < 0) u += sizeX;
        if (v < 0) v += sizeY;
        return new Texture(texture, sizeX, sizeY, width, height, u, v, w, h, tiled);
    }
    public Texture resize(int width, int height) {
        return new Texture(texture, sizeX, sizeY, width, height, u, v, w, h, tiled);
    }
    public Texture stretched(int width, int height) {
        return new Texture(texture, sizeX, sizeY, width, height, u, v, w, h, false);
    }
    public Texture tiled(int width, int height) {
        return new Texture(texture, sizeX, sizeY, width, height, u, v, w, h, true);
    }
    public Texture tiled() {
        return new Texture(texture, sizeX, sizeY, width, height, u, v, w, h, true);
    }

    @Override public void draw(GuiGraphics g, int x, int y) {
        int dstW = width <= 0 ? w : width;
        int dstH = height <= 0 ? h : height;
        if (!tiled) g.blit(texture, x, y, dstW, dstH, u, v, w, h, sizeX, sizeY);
        else {
            for (int dx = 0; dx < dstW; dx += w) {
                int tileW = Math.min(w, dstW - dx);
                for (int dy = 0; dy < dstH; dy += h) {
                    int tileH = Math.min(h, dstH - dy);
                    g.blit(texture, x + dx, y + dy, tileW, tileH, u, v, tileW, tileH, sizeX, sizeY);
                }
            }
        }
    }
}