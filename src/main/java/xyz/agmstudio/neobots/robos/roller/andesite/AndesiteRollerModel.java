package xyz.agmstudio.neobots.robos.roller.andesite;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import xyz.agmstudio.neobots.NeoBots;
import xyz.agmstudio.neobots.robos.roller.NeoBotRollerModel;

import javax.annotation.ParametersAreNonnullByDefault;


@ParametersAreNonnullByDefault
public class AndesiteRollerModel<T extends AndesiteRoller> extends NeoBotRollerModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(NeoBots.rl("andesite_roller"), "main");
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition wheel = partdefinition.addOrReplaceChild("wheel", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, -5.0F, -5.0F, 3.0F, 10.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(39, 54).addBox(-2.0F, -2.0F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(27, 17).addBox(8.0F, -5.0F, -5.0F, 3.0F, 10.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(28, 54).addBox(11.0F, -2.0F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 19.5F, 0.0F));

        PartDefinition wheelEdge = partdefinition.addOrReplaceChild("wheelEdge", CubeListBuilder.create().texOffs(17, 47).addBox(-3.0F, 6.5F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(29, 38).addBox(-2.0F, 2.0F, -3.0F, 4.0F, 9.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(50, 53).addBox(2.0F, 6.5F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 11.0F, 0.0F));

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(33, 0).addBox(-4.0F, -5.0F, -2.0F, 8.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

        PartDefinition rightArm = partdefinition.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(0, 47).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, 1.0F, 0.0F));

        PartDefinition leftArm = partdefinition.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(50, 38).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, 1.0F, 0.0F));

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(54, 15).addBox(4.0F, -6.0F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(58, 5).addBox(5.0F, -5.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(54, 24).addBox(-5.0F, -6.0F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(58, 10).addBox(-6.0F, -5.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public AndesiteRollerModel(ModelPart root) {
        super(root);
    }
}