package xyz.agmstudio.neobots.robos.brass.roller;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

public class BrassRollerRenderer extends MobRenderer<BrassRoller, BrassRollerModel<BrassRoller>> {
    private static final ResourceLocation TEXTURE       = ResourceLocation.fromNamespaceAndPath(NeoBots.MOD_ID, "textures/entity/brass_roller.png");
    private static final ResourceLocation TEXTURE_OFF   = ResourceLocation.fromNamespaceAndPath(NeoBots.MOD_ID, "textures/entity/brass_roller_off.png");
    private static final ResourceLocation TEXTURE_CRASH = ResourceLocation.fromNamespaceAndPath(NeoBots.MOD_ID, "textures/entity/brass_roller_crash.png");

    public BrassRollerRenderer(EntityRendererProvider.Context context) {
        super(context, new BrassRollerModel<>(context.bakeLayer(BrassRollerModel.LAYER_LOCATION)), 0.25f);
    }

    @Override public @NotNull ResourceLocation getTextureLocation(@NotNull BrassRoller roller) {
        return switch (NeoBotEntity.State.of(roller)) {
            case NO_CHARGE, STOPPED -> roller.animTick > 20 ? TEXTURE : TEXTURE_OFF;
            case CRASHED -> {
                if (roller.animTick > 30) {
                    if (roller.animTick > 45) yield TEXTURE_CRASH;
                    if (roller.animTick < 40 && roller.animTick > 35) yield TEXTURE_CRASH;
                    yield TEXTURE;
                }
                yield TEXTURE_CRASH;
            }
            default -> TEXTURE;
        };
    }
}