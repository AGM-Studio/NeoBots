package xyz.agmstudio.neobots.robos.roller.andesite;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.robos.roller.NeoBotRollerRenderer;

public class AndesiteRollerRenderer extends NeoBotRollerRenderer<AndesiteRoller, AndesiteRollerModel<AndesiteRoller>> {
    private static final ResourceLocation TEXTURE       = ResourceLocation.fromNamespaceAndPath(NeoBots.MOD_ID, "textures/entity/andesite_roller.png");
    private static final ResourceLocation TEXTURE_OFF   = ResourceLocation.fromNamespaceAndPath(NeoBots.MOD_ID, "textures/entity/andesite_roller_off.png");
    private static final ResourceLocation TEXTURE_CRASH = ResourceLocation.fromNamespaceAndPath(NeoBots.MOD_ID, "textures/entity/andesite_roller_crash.png");

    public AndesiteRollerRenderer(EntityRendererProvider.Context context) {
        super(context, new AndesiteRollerModel<>(context.bakeLayer(AndesiteRollerModel.LAYER_LOCATION)), 0.25f);
    }

    @Override public ResourceLocation TEXTURE() {
        return TEXTURE;
    }
    @Override public ResourceLocation TEXTURE_OFF() {
        return TEXTURE_OFF;
    }
    @Override public ResourceLocation TEXTURE_CRASH() {
        return TEXTURE_CRASH;
    }
}