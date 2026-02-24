package xyz.agmstudio.neobots.robos.roller.brass;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.robos.roller.NeoBotRollerRenderer;

public class BrassRollerRenderer extends NeoBotRollerRenderer<BrassRoller, BrassRollerModel<BrassRoller>> {
    private static final ResourceLocation TEXTURE       = ResourceLocation.fromNamespaceAndPath(NeoBots.MOD_ID, "textures/entity/brass_roller.png");
    private static final ResourceLocation TEXTURE_OFF   = ResourceLocation.fromNamespaceAndPath(NeoBots.MOD_ID, "textures/entity/brass_roller_off.png");
    private static final ResourceLocation TEXTURE_CRASH = ResourceLocation.fromNamespaceAndPath(NeoBots.MOD_ID, "textures/entity/brass_roller_crash.png");

    public BrassRollerRenderer(EntityRendererProvider.Context context) {
        super(context, new BrassRollerModel<>(context.bakeLayer(BrassRollerModel.LAYER_LOCATION)), 0.25f);
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