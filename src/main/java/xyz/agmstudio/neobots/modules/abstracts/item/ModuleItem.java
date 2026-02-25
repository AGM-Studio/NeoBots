package xyz.agmstudio.neobots.modules.abstracts.item;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.modules.abstracts.task.ModuleTask;
import xyz.agmstudio.neobots.modules.abstracts.data.ModuleData;
import xyz.agmstudio.neobots.robos.NeoBotEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class ModuleItem<D extends ModuleData, T extends ModuleTask<D>> extends Item {
    public static void getBaseRecipe(DataGenContext<Item, Item> ctx, RegistrateRecipeProvider prov) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get(), 4)
                .pattern("CAC")
                .pattern("APA")
                .pattern("RAR")
                .define('P', AllItems.PRECISION_MECHANISM)
                .define('A', AllBlocks.ANDESITE_CASING)
                .define('C', AllBlocks.COGWHEEL)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_precision", RegistrateRecipeProvider.has(AllItems.PRECISION_MECHANISM))
                .save(prov);
    }

    protected final String key;
    private final ModuleTask.Gen<D, T> taskGenerator;
    private final ModuleData.Gen<D> dataGenerator;

    public ModuleItem(String key, Properties properties, ModuleTask.Gen<D, T> taskGenerator, ModuleData.Gen<D> dataGenerator) {
        super(properties);
        this.key = key;
        this.taskGenerator = taskGenerator;
        this.dataGenerator = dataGenerator;
    }

    public abstract ModuleTier getTier();

    /** Generates a task for the bot to be executed (Uses load to automatically load the data)*/
    public T getTask(NeoBotEntity bot, ItemStack stack) {
        T task = taskGenerator.generate(bot, getData(bot.level(), stack));
        CompoundTag tag = bot.getTaskData();
        if (tag != null && Objects.equals(task.getId(), tag.getString("id"))) task.load(tag);
        return task;
    }

    /** Generates a data class for this module */
    public D getData(Level level, ItemStack stack) {
        return dataGenerator.generate(level, stack);
    }

    public @NotNull Component getModuleDescription() {
        return Component.translatable("module.create_neobots." + key + ".tooltip.description");
    }
    public @NotNull FontHelper.Palette getPalette() {
        return FontHelper.Palette.GRAY_AND_GOLD;
    }
    public @NotNull FontHelper.Palette getShiftPalette() {
        return FontHelper.Palette.GRAY_AND_BLUE;
    }

    @Override public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext ctx, @NotNull List<Component> tooltip, @NotNull TooltipFlag flags) {
        tooltip.add(TooltipHelper.holdShift(FontHelper.Palette.GRAY_AND_BLUE, false));
        tooltip.add(CommonComponents.SPACE);
        if (flags.hasShiftDown())
            tooltip.addAll(FontHelper.cutTextComponent(getModuleDescription(), getShiftPalette()));
        else {
            List<Component> dataTooltip = new ArrayList<>();
            getData(ctx.level(), stack).addTooltip(dataTooltip, ctx, flags);
            for (Component component: dataTooltip)
                tooltip.addAll(FontHelper.cutTextComponent(component, getPalette()));
        }
    }

    @Override public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp && this instanceof MenuProvider mp)
            sp.openMenu(mp, buf -> buf.writeEnum(hand));

        return InteractionResultHolder.consume(stack);
    }
}