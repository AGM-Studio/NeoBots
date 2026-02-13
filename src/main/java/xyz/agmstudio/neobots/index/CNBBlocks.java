package xyz.agmstudio.neobots.index;

import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.state.BlockBehaviour;
import xyz.agmstudio.neobots.block.battery.BatteryBlock;
import xyz.agmstudio.neobots.block.battery.BatteryItem;
import xyz.agmstudio.neobots.block.charger.ChargerBlock;
import xyz.agmstudio.neobots.block.charging_pad.ChargingPadBlock;

import static xyz.agmstudio.neobots.NeoBots.REGISTRATE;

public interface CNBBlocks {
    static void register() {}

    BlockEntry<ChargingPadBlock> CHARGING_PAD = REGISTRATE.block("charging_pad", ChargingPadBlock::new)
            .initialProperties(SharedProperties::stone).tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .simpleItem().register();

    BlockEntry<ChargerBlock> CHARGER = REGISTRATE.block("charger", ChargerBlock::new)
            .initialProperties(SharedProperties::stone).tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .simpleItem().register();

    BlockEntry<BatteryBlock> BATTERY = REGISTRATE.block("battery", BatteryBlock::new)
            .initialProperties(SharedProperties::copperMetal).tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .item(BatteryItem::new).build()
            .register();
}