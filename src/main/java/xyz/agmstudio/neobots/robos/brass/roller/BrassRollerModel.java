package xyz.agmstudio.neobots.robos.brass.roller;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import xyz.agmstudio.neobots.NeoBots;

import javax.annotation.ParametersAreNonnullByDefault;


@ParametersAreNonnullByDefault
public class BrassRollerModel<T extends BrassRoller> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(NeoBots.rl("brass_roller"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rArm;
    private final ModelPart lArm;
    private final ModelPart wheelEdge;
    private final ModelPart wheel;

    public BrassRollerModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.rArm = root.getChild("rightArm");
        this.lArm = root.getChild("leftArm");
        this.wheel = root.getChild("Wheel");
        this.wheelEdge = root.getChild("wheelEdge");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(2, 1).addBox(-3.0F, -7.0F, -4.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 21).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(51, 23).addBox(-2.0F, -6.0F, 4.0F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(24, 56).addBox(-2.5F, -15.0F, 0.0F, 5.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(33, 9).addBox(-4.0F, -5.0F, -2.0F, 8.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

        PartDefinition rightArm = partdefinition.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(17, 38).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, 3.0F, 0.0F));

        PartDefinition leftArm = partdefinition.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(48, 28).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, 3.0F, 0.0F));

        PartDefinition wheelEdge = partdefinition.addOrReplaceChild("wheelEdge", CubeListBuilder.create().texOffs(29, 0).addBox(-5.0F, 0.0F, -3.0F, 10.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 54).addBox(-3.0F, 6.5F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(11, 55).addBox(2.0F, 6.5F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(32, 23).addBox(3.0F, 2.0F, -3.0F, 2.0F, 9.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 38).addBox(-5.0F, 2.0F, -3.0F, 2.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 11.0F, 0.0F));

        PartDefinition Wheel = partdefinition.addOrReplaceChild("Wheel", CubeListBuilder.create().texOffs(36, 44).addBox(-2.0F, -5.0F, -5.0F, 4.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 19.5F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.applyHeadRotation(netHeadYaw, headPitch);

        this.animateWalk(BrassRollerAnimations.WALK, limbSwing, limbSwingAmount, 2f, 2.5f);
        this.animate(entity.idleAnimationState, BrassRollerAnimations.IDLE, ageInTicks, 1f);
        this.animate(entity.crashAnimationState, BrassRollerAnimations.CRASH, ageInTicks, 1f);
        this.animate(entity.shutdownAnimationState, BrassRollerAnimations.TURN_OFF, ageInTicks, 1f);
        this.animate(entity.turnonAnimationState, BrassRollerAnimations.TURN_ON, ageInTicks, 1f);

        this.wheel.xRot = (float) entity.wheelRot;
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