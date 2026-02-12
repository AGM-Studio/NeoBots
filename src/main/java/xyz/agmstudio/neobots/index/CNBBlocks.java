package xyz.agmstudio.neobots.index;

import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.state.BlockBehaviour;
import xyz.agmstudio.neobots.block.charger.ChargerBlock;
import xyz.agmstudio.neobots.block.charging_pad.ChargingPadBlock;

import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static xyz.agmstudio.neobots.NeoBots.REGISTRATE;

public interface CNBBlocks {
    static void register() {}

    BlockEntry<ChargingPadBlock> CHARGING_PAD = REGISTRATE.block("charging_pad", ChargingPadBlock::new)
            .initialProperties(SharedProperties::stone).properties(BlockBehaviour.Properties::noOcclusion)
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .transform(axeOrPickaxe())
            .simpleItem()//.transform(customItemModel())
            .register();

    BlockEntry<ChargerBlock> CHARGER = REGISTRATE.block("charger", ChargerBlock::new)
            .initialProperties(SharedProperties::wooden).properties(BlockBehaviour.Properties::noOcclusion)
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .transform(axeOrPickaxe())
            .simpleItem()//.transform(customItemModel())
            .register();
}