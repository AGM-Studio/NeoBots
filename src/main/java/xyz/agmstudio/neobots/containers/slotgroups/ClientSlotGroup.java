package xyz.agmstudio.neobots.containers.slotgroups;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import xyz.agmstudio.neobots.menus.abstracts.AbstractScreen;
import xyz.agmstudio.neobots.menus.gui.Drawable;
import xyz.agmstudio.neobots.menus.gui.FrameTexture;
import xyz.agmstudio.neobots.menus.gui.Label;
import xyz.agmstudio.neobots.menus.gui.Texture;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class ClientSlotGroup {
    private final SlotGroupHolder holder;

    private Function<Integer, Texture> texture;
    private int textureSizeX = 18;
    private int textureSizeY = 18;
    private int textureOffsetX = 1;
    private int textureOffsetY = 1;

    private final List<Supplier<Drawable.Drawer>> frames = new ArrayList<>();
    private final List<Supplier<Label>> labels = new ArrayList<>();

    public ClientSlotGroup(SlotGroupHolder holder) {
        this.holder = holder;
    }

    public ClientSlotGroup withTexture(Function<Integer, Texture> provider) {
        this.texture = provider;
        return this;
    }
    public ClientSlotGroup withTexture(Texture texture) {
        return withTexture(i -> texture);
    }
    public ClientSlotGroup withTextureSize(int x, int y) {
        this.textureSizeX = x;
        this.textureSizeY = y;
        return this;
    }
    public ClientSlotGroup withTextureOffset(int x, int y) {
        this.textureOffsetX = x;
        this.textureOffsetY = y;
        return this;
    }

    public FrameBuilder withFrame(FrameTexture frame) {
        return new FrameBuilder(this, frame);
    }
    public LabelBuilder addLabel(Function<AbstractScreen<?>, Component> text, int x, int y) {
        return new LabelBuilder(this, text, x, y);
    }
    public LabelBuilder addLabel(Component text, int x, int y) {
        return addLabel(s -> text, x, y);
    }

    public void render(AbstractScreen<?> screen, GuiGraphics g) {
        if (texture == null) return;
        int offX = screen.getGuiLeft() - textureOffsetX;
        int offY = screen.getGuiTop() - textureOffsetY;
        for (Slot slot: holder.slots) {
            if (!slot.isActive()) continue;
            Texture t = this.texture.apply(slot.index - holder.offsetIndex());
            if (t != null) t.resize(textureSizeX, textureSizeY).draw(g, slot.x + offX, slot.y + offY);
        }
    }
    public void renderBg(AbstractScreen<?> screen, GuiGraphics g, boolean isBeforeBg) {
        frames.stream().map(Supplier::get).filter(Objects::nonNull).forEach(frame -> frame.draw(screen, g, isBeforeBg));
    }
    public void renderLabels(AbstractScreen<?> screen, GuiGraphics g) {
        labels.stream().map(Supplier::get).filter(Objects::nonNull).forEach(label -> label.render(screen, g));
    }

    public static class LabelBuilder {
        private final ClientSlotGroup group;
        private final Label label;

        private LabelBuilder(ClientSlotGroup group, Function<AbstractScreen<?>, Component> text, int x, int y) {
            this.group = group;
            this.label = new Label(text, x, y);
        }

        public LabelBuilder centered() {
            label.centered();
            return this;
        }

        public LabelBuilder withColor(int color) {
            label.withColor(color);
            return this;
        }

        public LabelBuilder withShadow() {
            label.withShadow();
            return this;
        }

        public LabelBuilder width(int maxWidth) {
            label.width(maxWidth);
            return this;
        }

        public LabelBuilder scale(float scale) {
            label.scale(scale);
            return this;
        }

        public ClientSlotGroup build() {
            group.labels.add(() -> {
                if (!group.holder.isVisible()) return null;
                return label;
            });
            return group;
        }
    }
    public static class FrameBuilder {
        private final ClientSlotGroup group;
        private final FrameTexture texture;
        private int offsetX = 0;
        private int offsetY = 0;
        private int offsetW = 0;
        private int offsetH = 0;
        private int width = 0;
        private int height = 0;
        private boolean drawBeforeBg = false;

        private FrameBuilder(ClientSlotGroup group, FrameTexture texture) {
            this.group = group;
            this.texture = texture;
        }
        public FrameBuilder offset(int offsetX, int offsetY) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            return this;
        }
        public FrameBuilder offsetSize(int offsetW, int offsetH) {
            this.offsetW = offsetW;
            this.offsetH = offsetH;
            return this;
        }
        public FrameBuilder minWidth(int width) {
            this.width = width;
            return this;
        }
        public FrameBuilder minHeight(int height) {
            this.height = height;
            return this;
        }
        public FrameBuilder minSize(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }
        public FrameBuilder drawBeforeBg() {
            this.drawBeforeBg = true;
            return this;
        }
        public ClientSlotGroup build() {
            group.frames.add(() -> {
                if (!group.holder.isVisible()) return null;
                return texture.around(
                        group.holder.x() - offsetX - group.textureOffsetX,
                        group.holder.y() - offsetY - group.textureOffsetY,
                        Math.max(group.holder.activeWidth() + group.textureSizeX - 16 + offsetX + offsetW, width),
                        Math.max(group.holder.activeHeight() + group.textureSizeY - 16 + offsetY + offsetH, height),
                        drawBeforeBg
                );
            });
            return group;
        }
    }
}