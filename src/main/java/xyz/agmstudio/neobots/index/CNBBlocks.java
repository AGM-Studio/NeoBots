package xyz.agmstudio.neobots.index;

import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.energy.ComponentEnergyStorage;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.block.battery.BatteryBlock;
import xyz.agmstudio.neobots.block.battery.BatteryItem;
import xyz.agmstudio.neobots.block.charger.ChargerBlock;
import xyz.agmstudio.neobots.block.charger.ChargerRenderer;
import xyz.agmstudio.neobots.block.charging_pad.ChargingPadBlock;
import xyz.agmstudio.neobots.block.parts.BrassRollerHead;
import xyz.agmstudio.neobots.block.parts.BrassWheel;
import xyz.agmstudio.neobots.block.parts.BrassWheelBase;
import xyz.agmstudio.neobots.block.parts.RollerWheel;

import javax.annotation.ParametersAreNonnullByDefault;

import static xyz.agmstudio.neobots.NeoBots.REGISTRATE;
import static xyz.agmstudio.neobots.index.CNBDataComponents.BATTERY_DATA;


@ParametersAreNonnullByDefault
public final class CNBBlocks {
    public static void register(IEventBus bus) {
        bus.addListener(CNBBlocks::registerCapabilities);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            bus.addListener(CNBBlocks::clientSetup);
        }
    }

    public static void registerCapabilities(@NotNull RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.EnergyStorage.ITEM,
                (stack, ctx) -> new ComponentEnergyStorage(stack, BATTERY_DATA.get(), BatteryItem.CAPACITY),
                CNBBlocks.BATTERY.asItem()
        );
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CNBBlockEntities.CHARGER.get(),
                (be, ctx) -> be.getInventory());
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> BlockEntityRenderers.register(CNBBlockEntities.CHARGER.get(), ChargerRenderer::new));
    }

    private static @NotNull <T extends Block> ResourceLocation loc(DataGenContext<Block, T> ctx, String... subs) {
        final String sub = subs.length == 0 ? "" : String.join("/", subs);
        return NeoBots.rl("block/" + ctx.getName() + sub);
    }

    private static <T extends Block> @NotNull NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> blockStateProvider(String... subs) {
        return (ctx, prov) -> prov.simpleBlock(ctx.getEntry(), prov.models().getExistingFile(loc(ctx, subs)));
    }
    private static <T extends Block> @NotNull NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> horizontalFacingBlockState(String... subs) {
        return (ctx, prov) -> {
            var model = prov.models().getExistingFile(loc(ctx, subs));
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
    private static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> directionalBlockState(String... subs) {
        return (ctx, prov) -> {
            var model = prov.models().getExistingFile(loc(ctx, subs));
            prov.getVariantBuilder(ctx.getEntry()).forAllStates(state -> {
                Direction facing = state.getValue(DirectionalBlock.FACING);
                int xRot = switch (facing) {
                    case DOWN -> 180;
                    case NORTH, SOUTH, WEST, EAST -> 90;
                    default -> 0;
                };

                int yRot = switch (facing) {
                    case SOUTH -> 180;
                    case WEST -> 270;
                    case EAST -> 90;
                    default -> 0;
                };

                return ConfiguredModel.builder().modelFile(model).rotationX(xRot).rotationY(yRot).build();
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
            .recipe(ChargingPadBlock::getRecipe)
            .tab(CNBCreativeModeTabs.MAIN.getKey()).build()
            .register();

    public static final BlockEntry<ChargerBlock> CHARGER = REGISTRATE.block("charger", ChargerBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .transform(TagGen.axeOrPickaxe())
            .blockstate(horizontalFacingBlockState())
            .item().model(itemModelProvider())
            .recipe(ChargerBlock::getRecipe)
            .tab(CNBCreativeModeTabs.MAIN.getKey()).build()
            .register();

    public static final BlockEntry<BatteryBlock> BATTERY = REGISTRATE.block("battery", BatteryBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .transform(TagGen.pickaxeOnly())
            .blockstate(blockStateProvider())
            .item(BatteryItem::new).model(itemModelProvider())
            .recipe(BatteryItem::getRecipe)
            .tab(CNBCreativeModeTabs.MAIN.getKey()).build()
            .register();

    // Bot Parts
    public static final BlockEntry<RollerWheel> ROLLER_WHEEL = REGISTRATE.block("roller_wheel", RollerWheel::new)
            .initialProperties(SharedProperties::wooden)
            .blockstate(directionalBlockState())
            .item().model(itemModelProvider())
            .recipe(RollerWheel::getRecipe)
            .tab(CNBCreativeModeTabs.MAIN.getKey()).build()
            .register();

    public static final BlockEntry<BrassRollerHead> BRASS_ROLLER_HEAD = REGISTRATE.block("brass_roller_head", BrassRollerHead::new)
            .initialProperties(() -> Blocks.PLAYER_HEAD)
            .blockstate(horizontalFacingBlockState())
            .item().model(itemModelProvider())
            .recipe(BrassRollerHead::getRecipe)
            .tab(CNBCreativeModeTabs.MAIN.getKey()).build()
            .register();
    public static final BlockEntry<BrassWheelBase> BRASS_WHEEL_BASE = REGISTRATE.block("brass_wheel_base", BrassWheelBase::new)
            .initialProperties(SharedProperties::softMetal)
            .blockstate(horizontalFacingBlockState())
            .item().model(itemModelProvider())
            .recipe(BrassWheelBase::getRecipe)
            .tab(CNBCreativeModeTabs.MAIN.getKey()).build()
            .register();
    public static final BlockEntry<BrassWheel> BRASS_WHEEL = REGISTRATE.block("brass_wheel", BrassWheel::new)
            .initialProperties(SharedProperties::softMetal)
            .blockstate(horizontalFacingBlockState())
            .item().model(itemModelProvider())
            .recipe(BrassWheel::getRecipe)
            .tab(CNBCreativeModeTabs.MAIN.getKey()).build()
            .register();
}