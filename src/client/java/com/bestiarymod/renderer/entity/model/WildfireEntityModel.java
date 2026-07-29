package com.bestiarymod.renderer.entity.model;

import com.bestiarymod.renderer.entity.state.WildfireRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class WildfireEntityModel extends EntityModel<WildfireRenderState> {
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart helmet;
    private final ModelPart shields;
    private final ModelPart frontShield;
    private final ModelPart rightShield;
    private final ModelPart backShield;
    private final ModelPart leftShield;

    public WildfireEntityModel(ModelPart root) {
        super(root);
        this.root = root;
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.helmet = this.head.getChild("helmet");
        this.shields = root.getChild("shields");
        this.frontShield = this.shields.getChild("frontShield");
        this.rightShield = this.shields.getChild("rightShield");
        this.backShield = this.shields.getChild("backShield");
        this.leftShield = this.shields.getChild("leftShield");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-2.0F, -21.0F, -2.0F, 4.0F, 21.0F, 4.0F),
            PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head",
            CubeListBuilder.create()
                .texOffs(0, 26)
                .addBox(-4.0F, -5.0F, -4.0F, 8.0F, 8.0F, 8.0F),
            PartPose.offset(0.0F, -24.0F, 0.0F));

        head.addOrReplaceChild("helmet",
            CubeListBuilder.create()
                .texOffs(0, 43)
                .addBox(-4.0F, -7.5F, -4.0F, 8.0F, 9.0F, 8.0F, new CubeDeformation(0.2F)),
            PartPose.offset(0.0F, 1.5F, 0.0F));

        PartDefinition shields = root.addOrReplaceChild("shields",
            CubeListBuilder.create(),
            PartPose.offset(0.0F, 24.0F, 0.0F));

        shields.addOrReplaceChild("frontShield",
            CubeListBuilder.create()
                .texOffs(17, 0)
                .addBox(-5.0F, 3.5F, -9.5F, 10.0F, 17.0F, 2.0F),
            PartPose.offsetAndRotation(0.0F, -22.0F, 0.0F, 0.0F, -0.2618F, 0.0F));

        shields.addOrReplaceChild("rightShield",
            CubeListBuilder.create()
                .texOffs(17, 0)
                .addBox(-5.0F, 3.5F, -9.5F, 10.0F, 17.0F, 2.0F),
            PartPose.offsetAndRotation(0.0F, -22.0F, 0.0F, -0.2618F, 1.5708F, 0.0F));

        shields.addOrReplaceChild("backShield",
            CubeListBuilder.create()
                .texOffs(17, 0)
                .addBox(-5.0F, 3.5F, -9.5F, 10.0F, 17.0F, 2.0F),
            PartPose.offsetAndRotation(0.0F, -22.0F, 0.0F, -0.2618F, 3.1416F, 0.0F));

        shields.addOrReplaceChild("leftShield",
            CubeListBuilder.create()
                .texOffs(17, 0)
                .addBox(-5.0F, 3.5F, -9.5F, 10.0F, 17.0F, 2.0F),
            PartPose.offsetAndRotation(0.0F, -22.0F, 0.0F, -0.2618F, -1.5708F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(WildfireRenderState state) {
        super.setupAnim(state);
        if (state.wildfire != null) {
            int shields = state.wildfire.getActiveShieldsCount();
            this.frontShield.visible = shields >= 1;
            this.rightShield.visible = shields >= 2;
            this.backShield.visible = shields >= 3;
            this.leftShield.visible = shields >= 4;
            float rot = state.ageInTicks * 0.05F;
            this.shields.yRot = rot;
            this.shields.xRot = (float) Math.sin(rot * 0.5F) * 0.05F;
        }
    }
}
