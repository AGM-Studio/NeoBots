package xyz.agmstudio.neobots.index;

import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.block.battery.BatteryBlock;
import xyz.agmstudio.neobots.block.battery.BatteryItem;
import xyz.agmstudio.neobots.block.charger.ChargerBlock;
import xyz.agmstudio.neobots.block.charging_pad.ChargingPadBlock;

import javax.annotation.ParametersAreNonnullByDefault;

import static xyz.agmstudio.neobots.NeoBots.REGISTRATE;


@ParametersAreNonnullByDefault
public final class CNBBlocks {
    public static void register() {}
    private static @NotNull <T extends Block> ResourceLocation loc(DataGenContext<Block, T> ctx, String sub) {
        return NeoBots.rl("block/" + ctx.getName() + sub);
    }

    private static <T extends Block> @NotNull NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> blockStateProvider(String... subs) {
        final String sub = subs.length == 0 ? "" : String.join("/", subs);
        return (ctx, prov) -> prov.simpleBlock(ctx.getEntry(),
                prov.models().getExistingFile(loc(ctx, sub)));
    }
    private static <T extends Block> @NotNull NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> horizontalFacingBlockState(String... subs) {
        final String sub = subs.length == 0 ? "" : String.join("/", subs);
        return (ctx, prov) -> {
            var model = prov.models().getExistingFile(loc(ctx, sub));
            prov.getVariantBuilder(ctx.getEntry()).forAllStates(state -> {
                Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
                int yRot = switch (facing) {
                    case EAST -> 90;
                    case SOUTH -> 180;
                    case WEST -> 270;
                    default -> 0;
                };
                return ConfiguredModel.builder().modelFile(model).rotationY(yRot).build();
            });
        };
    }

    private static <T extends BlockItem> @NotNull NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> itemModelProvider(String... subs) {
        final String sub = subs.length == 0 ? "" : String.join("/", subs);
        return (ctx, prov) -> prov.withExistingParent(ctx.getName(), prov.modLoc("block/" + ctx.getName() + sub));
    }

    public static final BlockEntry<ChargingPadBlock> CHARGING_PAD = REGISTRATE.block("charging_pad", ChargingPadBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .transform(TagGen.axeOrPickaxe())
            .blockstate(blockStateProvider())
            .item().model(itemModelProvider())
            .tab(CNBCreativeModeTabs.MAIN.getKey()).build()
            .register();

    public static final BlockEntry<ChargerBlock> CHARGER = REGISTRATE.block("charger", ChargerBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .transform(TagGen.axeOrPickaxe())
            .blockstate(horizontalFacingBlockState())
            .item().model(itemModelProvider())
            .tab(CNBCreativeModeTabs.MAIN.getKey()).build()
            .register();

    public static final BlockEntry<BatteryBlock> BATTERY = REGISTRATE.block("battery", BatteryBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .transform(TagGen.pickaxeOnly())
            .blockstate(blockStateProvider())
            .item(BatteryItem::new).model(itemModelProvider())
            .tab(CNBCreativeModeTabs.MAIN.getKey()).build()
            .register();
}