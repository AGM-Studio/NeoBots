package xyz.agmstudio.neobots.index;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.ponder.CNBPonderScenes;

import javax.annotation.ParametersAreNonnullByDefault;


@ParametersAreNonnullByDefault
public final class CNBPonders {
    public static final ResourceLocation BOTS = NeoBots.rl("bots");

    public static void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<RegistryEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);
        HELPER.registerTag(BOTS)
                .addToIndex()
                .item(CNBBlocks.BRASS_ROLLER_HEAD)
                .title("Buildable Bots")
                .description("All bots which you can assemble")
                .register();
        HELPER.addToTag(BOTS)
                .add(CNBBlocks.BRASS_ROLLER_HEAD);
    }

    public static void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        HELPER.addStoryBoard(CNBBlocks.BRASS_ROLLER_HEAD, "bot_assemble", CNBPonderScenes::botAssembly, BOTS);
    }
}