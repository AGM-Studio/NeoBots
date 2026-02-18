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
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.index.CNBEntities;
import xyz.agmstudio.neobots.robos.NeoBotEntity;
import xyz.agmstudio.neobots.robos.brass.roller.BrassRoller;

import java.util.List;

public class CNBPonderScenes {
    public static void botAssembly(SceneBuilder builder, @NotNull SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("bot_assembly", "Assemble Bots");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.world().showSection(util.select().layer(0), Direction.UP);

        BlockPos wheelPos  = util.grid().at(2, 1, 2);
        BlockPos casingPos = util.grid().at(2, 2, 2);
        BlockPos headPos   = util.grid().at(2, 3, 2);

        scene.overlay().showText(60)
                .text("Assembling a bot is the same as an Iron Golem. Place the parts as shown!")
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
                .text("The head is the core block. By placing the head, you will have your bot.")
                .placeNearTarget().pointAt(util.vector().centerOf(headPos));
        scene.idle(60);

        scene.world().setBlock(wheelPos, Blocks.AIR.defaultBlockState(), true);
        scene.world().setBlock(casingPos, Blocks.AIR.defaultBlockState(), true);
        scene.world().setBlock(headPos, Blocks.AIR.defaultBlockState(), true);

        Vec3 pos = new Vec3(2.5, 1, 2.5);
        ElementLink<EntityElement> roller = scene.world().createEntity(world -> {
            BrassRoller entity = new BrassRoller(CNBEntities.BRASS_ROLLER.get(), world);
            entity.moveTo(pos.x, pos.y, pos.z);
            entity.setState(NeoBotEntity.State.STOPPED);
            return entity;
        });

        scene.idle(10);
        scene.overlay().showText(60).colored(PonderPalette.RED)
                .text("Note that the bots will turn off as spawned due lack of energy.")
                .placeNearTarget().pointAt(util.vector().centerOf(wheelPos));
        scene.idle(60);
        scene.overlay().showText(80).colored(PonderPalette.BLUE)
                .text("Give them a charged battery and they will start their tasks.")
                .placeNearTarget().pointAt(util.vector().centerOf(wheelPos));
        scene.idle(40);
        scene.world().modifyEntities(NeoBotEntity.class, e -> {
            e.setState(NeoBotEntity.State.RUNNING);
        });
        scene.idle(40);
        scene.markAsFinished();
    }
    public static void charging(SceneBuilder builder, @NotNull SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("charging", "Charging Pad, Charger & Batteries");
        scene.configureBasePlate(0, 0, 5);

        BlockPos pad = util.grid().at(2, 2, 2);
        BlockPos charger = util.grid().at(2, 3, 2);

        scene.showBasePlate();
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.world().showSection(util.select().position(6, 0, 1), Direction.UP);
        for (int x = 6; x > 1; x--) {
            scene.idle(5);
            scene.world().showSection(util.select().position(x, 1, 2), Direction.DOWN);
        }
        scene.idle(5);
        scene.world().showSection(util.select().position(2, 2, 2), Direction.DOWN);
        scene.idle(5);
        scene.overlay().showText(60)
                .text("Charging pad is a kinetic block powered from below.")
                .placeNearTarget().pointAt(util.vector().centerOf(pad));
        scene.idle(60);

        scene.addKeyframe();
        scene.world().showSection(util.select().position(charger), Direction.DOWN);
        scene.overlay().showText(60).colored(PonderPalette.INPUT)
                .text("The charger on to of the pad will be able to charge batteries.")
                .placeNearTarget().pointAt(util.vector().centerOf(charger));
        scene.idle(60);
        scene.overlay().showText(60).colored(PonderPalette.OUTPUT)
                .text("Batteries can be inserted using funnels or just right clicking on it.")
                .placeNearTarget().pointAt(util.vector().centerOf(charger));
        scene.idle(60);
        scene.world().hideSection(util.select().position(charger), Direction.UP);
        scene.idle(5);

        scene.addKeyframe();
        scene.overlay().showText(60)
                .text("The charging pad can also charge bots directly above it.")
                .placeNearTarget().pointAt(util.vector().centerOf(pad));
        List<BlockPos> path = List.of(
                util.grid().at(1, 1, 0),
                util.grid().at(2, 1, 0),
                util.grid().at(2, 2, 1)
        );
        scene.world().showSection(util.select().position(2, 1, 1), Direction.DOWN);
        scene.idle(5);
        for (BlockPos pos: path) {
            scene.world().showSection(util.select().position(pos), Direction.DOWN);
            scene.idle(5);
        }
        scene.effects().indicateSuccess(util.grid().at(0, 1, 0));
        scene.idle(5);
        for (BlockPos pos: path) {
            scene.effects().indicateSuccess(pos.above());
            scene.idle(5);
        }
        scene.effects().indicateSuccess(util.grid().at(2, 3, 2));
        scene.idle(5);

        Vec3 pos = new Vec3(2.5, 3, 2.5);
        ElementLink<EntityElement> roller = scene.world().createEntity(world -> {
            BrassRoller entity = new BrassRoller(CNBEntities.BRASS_ROLLER.get(), world);
            entity.moveTo(pos.x, pos.y, pos.z);
            entity.setState(NeoBotEntity.State.RUNNING);
            return entity;
        });
        scene.idle(10);
        scene.overlay().showText(60).colored(PonderPalette.BLUE)
                .text("Though the bot needs to connect to the pad using a charging module.")
                .placeNearTarget().pointAt(util.vector().centerOf(charger));
        scene.idle(60);
        scene.overlay().showText(60).colored(PonderPalette.BLUE)
                .text("And the pad can only charge one bot at a time... more might result in a bot crash.")
                .placeNearTarget().pointAt(util.vector().centerOf(charger));
        scene.idle(60);
        scene.markAsFinished();
    }
}