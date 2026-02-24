package xyz.agmstudio.neobots.robos.roller;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public abstract class NeoBotRollerModel<T extends NeoBotRollerEntity> extends HierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rArm;
    private final ModelPart lArm;
    private final ModelPart wheelEdge;
    private final ModelPart wheel;

    public NeoBotRollerModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.rArm = root.getChild("rightArm");
        this.lArm = root.getChild("leftArm");
        this.wheel = root.getChild("wheel");
        this.wheelEdge = root.getChild("wheelEdge");
    }

    @Override public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.applyHeadRotation(netHeadYaw, headPitch);

        this.animateWalk(NeoBotRollerRenderer.WALK, limbSwing, limbSwingAmount, 2f, 2.5f);
        this.animate(entity.idleAnimationState, NeoBotRollerRenderer.IDLE, ageInTicks, 1f);
        this.animate(entity.crashAnimationState, NeoBotRollerRenderer.CRASH, ageInTicks, 1f);
        this.animate(entity.shutdownAnimationState, NeoBotRollerRenderer.TURN_OFF, ageInTicks, 1f);
        this.animate(entity.turnonAnimationState, NeoBotRollerRenderer.TURN_ON, ageInTicks, 1f);

        this.wheel.xRot = (float) (entity.getOldWheelRot() +
                (entity.getWheelRot() - entity.getOldWheelRot()) * Minecraft.getInstance().getFrameTimeNs() / 1E9f);
    }

    private void applyHeadRotation(float headYaw, float headPitch) {
        this.head.yRot = Mth.clamp(headYaw, -30f, 30f) * ((float)Math.PI / 180f);
        this.head.xRot = Mth.clamp(headPitch, -25f, 45) * ((float)Math.PI / 180f);
    }

    @Override public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        rArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        lArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        wheel.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        wheelEdge.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override public @NotNull ModelPart root() {
        return root;
    }
}