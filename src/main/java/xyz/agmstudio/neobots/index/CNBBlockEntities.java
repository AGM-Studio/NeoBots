package xyz.agmstudio.neobots.index;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.block.charging_pad.ChargingPadBlock;
import xyz.agmstudio.neobots.block.HalfShaftVisual;
import xyz.agmstudio.neobots.block.charging_pad.ChargingPadBlockEntity;
import xyz.agmstudio.neobots.block.charging_pad.ChargingPadRenderer;

public final class CNBBlockEntities {
    public static final BlockEntityEntry<ChargingPadBlockEntity> CHARGING_PAD = NeoBots.REGISTRATE.blockEntity("charging_pad_block_entity", ChargingPadBlockEntity::new)
            .visual(() -> HalfShaftVisual.facing(Direction.DOWN), false)
            .validBlock(ChargingPadBlock.BLOCK::get)
            .renderer(() -> ChargingPadRenderer::new).register();

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NeoBots.MOD_ID);

   // public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChargingPadBlockEntity>> CHARGING_PAD =
     //       BLOCK_ENTITIES.register("charging_pad_block_entity", () -> BlockEntityType.Builder.of(ChargingPadBlockEntity::new, ChargingPadBlock.BLOCK.get()).build(null));

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}