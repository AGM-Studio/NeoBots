package xyz.agmstudio.neobots.block.charger;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;


@ParametersAreNonnullByDefault
public class ChargerRenderer extends SafeBlockEntityRenderer<ChargerBlockEntity> {
    public ChargerRenderer(BlockEntityRendererProvider.Context context) {}

    @Override protected void renderSafe(ChargerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        renderItem(be, partialTicks, ms, buffer, light, overlay);
    }

    protected void renderItem(ChargerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        ItemStack stack = be.inventory.getStackInSlot(0);
        if (stack.isEmpty()) return;

        ms.pushPose();

        // Move to center of block
        ms.translate(0.5, 0.5, 0.5);

        // Rotate with block facing
        Direction facing = be.getBlockState().getValue(FACING);
        float yRot = -facing.toYRot();
        ms.mulPose(Axis.YP.rotationDegrees(yRot));

        // Flip upside down
        ms.mulPose(Axis.XP.rotationDegrees(180));

        // Scale nicely
        float scale = 2.0f;
        ms.scale(scale, scale, scale);

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel model = itemRenderer.getModel(stack, be.getLevel(), null, 0);
        itemRenderer.render(stack, ItemDisplayContext.FIXED, false, ms, buffer, light, overlay, model);
        ms.popPose();
    }
}