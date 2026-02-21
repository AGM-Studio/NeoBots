package xyz.agmstudio.neobots.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import xyz.agmstudio.neobots.NeoBots;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class FrameTexture implements Drawable {
    public final Texture texture;
    public final int top, bottom, left, right;
    public final int width, height;
    private FrameTexture(Texture texture, int top, int bottom, int left, int right, int width, int height) {
        this.texture = texture;
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        this.width = width;
        this.height = height;
    }

    public FrameTexture(ResourceLocation texture, int sizeX, int sizeY) {
        this(new Texture(texture, sizeX, sizeY), 0, 0, 0, 0, sizeX, sizeY);
    }
    public FrameTexture(String texture, int sizeX, int sizeY) {
        this(new Texture(NeoBots.rl(texture), sizeX, sizeY), 0, 0, 0, 0, sizeX, sizeY);
    }

    public FrameTexture margin(int top, int bottom, int left, int right) {
        return new FrameTexture(texture, top, bottom, left, right, width, height);
    }
    public FrameTexture margin(int top, int side) {
        return new FrameTexture(texture, top, top, side, side, width, height);
    }
    public FrameTexture margin(int corner) {
        return new FrameTexture(texture, corner, corner, corner, corner, width, height);
    }
    public FrameTexture resize(int width, int height) {
        return new FrameTexture(texture, top, bottom, left, right, width, height);
    }
    public FrameTexture tiled(boolean tiled) {
        return new FrameTexture(texture.tiled(), top, bottom, left, right, width, height);
    }

    @Override public void draw(GuiGraphics g, int x, int y) {
        int width = Math.max(this.width, left + right + 1);
        int height = Math.max(this.height, top + bottom + 1);
        texture.region(0, 0, left, top).draw(g, x, y);
        texture.region(-right, 0, right, top).draw(g, x + width - right, y);
        texture.region(0, -bottom, left, bottom).draw(g, x, y + height - bottom);
        texture.region(-right, -bottom, right, bottom).draw(g, x + width - right, y + height - bottom);

        int midSrcW = texture.sizeX - left - right;
        int midSrcH = texture.sizeY - top - bottom;
        int midDstW = width - left - right;
        int midDstH = height - top - bottom;
        texture.region(left, 0, midSrcW, top).resize(midDstW, top).draw(g, x + left, y);
        texture.region(left, -bottom, midSrcW, bottom).resize(midDstW, bottom).draw(g,x + left, y + height - bottom);
        texture.region(0, top, left, midSrcH).resize(left, midDstH).draw(g, x, y + top);
        texture.region(-right, top, right, midSrcH).resize(right, midDstH).draw(g, x + width - right, y + top);
        texture.region(left, top, midSrcW, midSrcH).resize(midDstW, midDstH).draw(g, x + left, y + top);
    }

    public Drawer around(int x, int y, int w, int h, boolean drawBeforeBg) {
        return resize(w + left + right, h + top + bottom).at(x - left, y - top, drawBeforeBg);
    }
}