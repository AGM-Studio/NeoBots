package xyz.agmstudio.neobots.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import xyz.agmstudio.neobots.index.CNBEntities;
import xyz.agmstudio.neobots.robos.NeoBotEntity;
import xyz.agmstudio.neobots.robos.brass.roller.BrassRoller;

public class CNBPonderScenes {
    public static void botAssembly(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("bot_assembly", "Assemble Bots");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.world().showSection(util.select().layer(0), Direction.UP);

        BlockPos wheelPos  = util.grid().at(2, 1, 2);
        BlockPos casingPos = util.grid().at(2, 2, 2);
        BlockPos headPos   = util.grid().at(2, 3, 2);

        scene.overlay().showText(60)
                .text("bot_assembly.text_1")
                .placeNearTarget();

        scene.idle(20);
        scene.world().showSection(util.select().position(wheelPos), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(casingPos), Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(util.select().position(headPos), Direction.DOWN);
        scene.effects().indicateSuccess(headPos);
        scene.idle(20);

        scene.overlay().showText(60).colored(PonderPalette.GREEN)
                .text("bot_assembly.text_2")
                .placeNearTarget().pointAt(util.vector().centerOf(headPos));

        scene.idle(60);

        scene.world().setBlock(wheelPos, Blocks.AIR.defaultBlockState(), true);
        scene.world().setBlock(casingPos, Blocks.AIR.defaultBlockState(), true);
        scene.world().setBlock(headPos, Blocks.AIR.defaultBlockState(), true);

        Vec3 pos = new Vec3(2.5, 1, 2.5);
        ElementLink<EntityElement> roller =
                scene.world().createEntity(world -> {
                    BrassRoller entity = new BrassRoller(CNBEntities.BRASS_ROLLER.get(), world);
                    entity.moveTo(pos.x, pos.y, pos.z);
                    entity.setState(NeoBotEntity.State.STOPPED);
                    return entity;
                });

        scene.idle(10);
        scene.overlay().showText(60).colored(PonderPalette.RED)
                .text("bot_assembly.text_3")
                .placeNearTarget().pointAt(util.vector().centerOf(wheelPos));
        scene.idle(60);
        scene.overlay().showText(80).colored(PonderPalette.BLUE)
                .text("bot_assembly.text_4")
                .placeNearTarget().pointAt(util.vector().centerOf(wheelPos));
        scene.idle(40);
        scene.world().modifyEntities(NeoBotEntity.class, e -> {
            e.setState(NeoBotEntity.State.RUNNING);
        });

        scene.idle(40);
    }
}