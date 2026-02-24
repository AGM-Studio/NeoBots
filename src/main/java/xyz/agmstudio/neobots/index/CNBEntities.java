package xyz.agmstudio.neobots.index;

import com.tterrag.registrate.util.entry.EntityEntry;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import xyz.agmstudio.neobots.robos.NeoBotEntity;
import xyz.agmstudio.neobots.robos.roller.andesite.AndesiteRoller;
import xyz.agmstudio.neobots.robos.roller.andesite.AndesiteRollerModel;
import xyz.agmstudio.neobots.robos.roller.andesite.AndesiteRollerRenderer;
import xyz.agmstudio.neobots.robos.roller.brass.BrassRoller;
import xyz.agmstudio.neobots.robos.roller.brass.BrassRollerModel;
import xyz.agmstudio.neobots.robos.roller.brass.BrassRollerRenderer;

import javax.annotation.ParametersAreNonnullByDefault;

import static xyz.agmstudio.neobots.NeoBots.REGISTRATE;


@ParametersAreNonnullByDefault
public final class CNBEntities {
    public static void register(IEventBus bus) {
        bus.addListener(CNBEntities::registerAttributes);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            bus.addListener(CNBEntities::registerRenderers);
            bus.addListener(CNBEntities::registerLayers);
        }
    }

    public static final EntityEntry<AndesiteRoller> ANDESITE_ROLLER = REGISTRATE.entity("andesite_roller", AndesiteRoller::new, MobCategory.MISC).register();
    public static final EntityEntry<BrassRoller> BRASS_ROLLER = REGISTRATE.entity("brass_roller", BrassRoller::new, MobCategory.MISC).register();

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(BRASS_ROLLER.get(), NeoBotEntity.createAttributes().build());
        event.put(ANDESITE_ROLLER.get(), NeoBotEntity.createAttributes().build());
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(CNBEntities.BRASS_ROLLER.get(), BrassRollerRenderer::new);
        event.registerEntityRenderer(CNBEntities.ANDESITE_ROLLER.get(), AndesiteRollerRenderer::new);
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BrassRollerModel.LAYER_LOCATION, BrassRollerModel::createBodyLayer);
        event.registerLayerDefinition(AndesiteRollerModel.LAYER_LOCATION, AndesiteRollerModel::createBodyLayer);
    }
}