package xyz.agmstudio.neobots.index;

import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
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
        HELPER.registerTag(BOTS).addToIndex().item(CNBBlocks.BRASS_ROLLER_HEAD).register();
        HELPER.addToTag(BOTS)
                .add(CNBBlocks.BRASS_ROLLER_HEAD)
                .add(CNBBlocks.ANDESITE_ROLLER_HEAD);
    }

    public static void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        HELPER.addStoryBoard(CNBBlocks.ANDESITE_ROLLER_HEAD, "andesite_roller_assemble", CNBPonderScenes::andesiteRollerAssembly, BOTS);
        HELPER.addStoryBoard(CNBBlocks.BRASS_ROLLER_HEAD, "brass_roller_assemble", CNBPonderScenes::brassRollerAssembly, BOTS);
        HELPER.addStoryBoard(CNBBlocks.CHARGING_PAD, "charging", CNBPonderScenes::charging, AllCreatePonderTags.KINETIC_APPLIANCES);
        HELPER.forComponents(CNBBlocks.CHARGER, CNBBlocks.BATTERY)
                .addStoryBoard("charging", CNBPonderScenes::charging);
    }
}