package xyz.agmstudio.neobots.block;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.minecraft.core.Direction;

public class HalfShaftVisual<T extends KineticBlockEntity> extends SingleAxisRotatingVisual<T> {
	public static <T extends KineticBlockEntity> SimpleBlockEntityVisualizer.Factory<T> facing(Direction face) {
		return (c, b, t) -> new HalfShaftVisual<>(c, b, t, face);
	}

	private HalfShaftVisual(VisualizationContext context, T blockEntity, float partialTick, Direction facing) {
		super(context, blockEntity, partialTick, Models.partial(AllPartialModels.SHAFT_HALF, facing));
	}
}