package xyz.agmstudio.neobots.robos;

import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.NeoBots;

public class NeoBotRenderer extends MobRenderer<NeoBotEntity, VillagerModel<NeoBotEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(NeoBots.MOD_ID, "textures/entity/neobot.png");

    public NeoBotRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)),
                0.5f
        );
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull NeoBotEntity entity) {
        return TEXTURE;
    }
}
