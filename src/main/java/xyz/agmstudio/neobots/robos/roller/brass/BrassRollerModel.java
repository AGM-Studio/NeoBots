package xyz.agmstudio.neobots.robos.roller.brass;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.robos.roller.NeoBotRollerModel;

import javax.annotation.ParametersAreNonnullByDefault;


@ParametersAreNonnullByDefault
public class BrassRollerModel<T extends BrassRoller> extends NeoBotRollerModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(NeoBots.rl("brass_roller"), "main");
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

        PartDefinition Wheel = partdefinition.addOrReplaceChild("wheel", CubeListBuilder.create().texOffs(36, 44).addBox(-2.0F, -5.0F, -5.0F, 4.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 19.5F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public BrassRollerModel(ModelPart root) {
        super(root);
    }
}