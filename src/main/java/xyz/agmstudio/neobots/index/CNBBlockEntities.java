package xyz.agmstudio.neobots.index;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.minecraft.core.Direction;
import xyz.agmstudio.neobots.block.HalfShaftVisual;
import xyz.agmstudio.neobots.block.charger.ChargerBlockEntity;
import xyz.agmstudio.neobots.block.charger.ChargerRenderer;
import xyz.agmstudio.neobots.block.charging_pad.ChargingPadBlockEntity;
import xyz.agmstudio.neobots.block.charging_pad.ChargingPadRenderer;

import static xyz.agmstudio.neobots.NeoBots.REGISTRATE;

public interface CNBBlockEntities {
    static void register() {}

    BlockEntityEntry<ChargingPadBlockEntity> CHARGING_PAD = REGISTRATE.blockEntity("charging_pad", ChargingPadBlockEntity::new)
            .visual(() -> HalfShaftVisual.facing(Direction.DOWN), false)
            .validBlock(CNBBlocks.CHARGING_PAD::get)
            .renderer(() -> ChargingPadRenderer::new).register();

    BlockEntityEntry<ChargerBlockEntity> CHARGER = REGISTRATE.blockEntity("charger_block", ChargerBlockEntity::new)
            .validBlock(CNBBlocks.CHARGER::get)
            .renderer(() -> ChargerRenderer::new).register();
}