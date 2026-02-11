package xyz.agmstudio.neobots.block.charging_pad;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class ChargingPadRenderer extends KineticBlockEntityRenderer<ChargingPadBlockEntity> {
	public ChargingPadRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(ChargingPadBlockEntity be, BlockState state) {
		return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, Direction.DOWN);
	}
}