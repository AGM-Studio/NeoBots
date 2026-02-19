package xyz.agmstudio.neobots;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.agmstudio.neobots.index.*;
import xyz.agmstudio.neobots.network.NetworkHandler;
import xyz.agmstudio.neobots.ponder.CNBPonderPlugin;

import java.util.HashMap;
import java.util.Map;

@Mod(NeoBots.MOD_ID)
public class NeoBots {
    public static final String MOD_ID = "create_neobots";
    public static final String MOD_NAME = "NeoBots";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID)
            .setTooltipModifierFactory(item ->
                    new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                            .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
            );

    @Contract("_ -> new")
    public static @NotNull ResourceLocation rl(String value) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, value);
    }
    public NeoBots(IEventBus bus, ModContainer container) {
        REGISTRATE.defaultCreativeTab("neobots");

        CNBDataComponents.register(bus);
        CNBItems.register();
        CNBBlocks.register(bus);
        CNBBlockEntities.register();
        CNBEntities.register(bus);
        CNBMenus.register();
        CNBLang.register();
        CNBCreativeModeTabs.register(bus);

        NetworkHandler.registerPackets(bus);
        if (FMLEnvironment.dist.isClient()) {
            bus.addListener(this::clientSetup);
        }
        bus.addListener(this::commonSetup);

        REGISTRATE.registerEventListeners(bus);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new CNBPonderPlugin());
    }

    private static final HashMap<String, String> itemFixes = new HashMap<>();
    public static void addItemAlias(String old, String current) {
        itemFixes.put(old, current);
    }
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            for (Map.Entry<String, String > entry: itemFixes.entrySet())
                BuiltInRegistries.ITEM.addAlias(rl(entry.getKey()), rl(entry.getValue()));
        });
    }
}