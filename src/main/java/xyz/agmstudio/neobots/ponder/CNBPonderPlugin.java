package xyz.agmstudio.neobots.ponder;

import com.simibubi.create.foundation.ponder.PonderWorldBlockEntityFix;
import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.api.registration.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.index.CNBPonders;

import javax.annotation.ParametersAreNonnullByDefault;


@ParametersAreNonnullByDefault
public class CNBPonderPlugin implements PonderPlugin {
    @Override public @NotNull String getModId() {
        return NeoBots.MOD_ID;
    }

    @Override public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        CNBPonders.registerScenes(helper);
    }

    @Override public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        CNBPonders.registerTags(helper);
    }

    @Override public void registerSharedText(SharedTextRegistrationHelper helper) {
        // Nothing for now
    }

    @Override public void onPonderLevelRestore(PonderLevel ponderLevel) {
        PonderWorldBlockEntityFix.fixControllerBlockEntities(ponderLevel);
    }

    @Override public void indexExclusions(IndexExclusionHelper helper) {
        // Nothing for now
    }
}