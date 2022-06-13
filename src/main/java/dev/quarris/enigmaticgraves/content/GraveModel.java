package dev.quarris.enigmaticgraves.content;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

public class GraveModel extends EntityModel<Entity> {
    private final ModelPart pot;
    private final ModelPart sapling_r1;
    private final ModelPart sapling_r2;
    private final ModelPart grave;

    public GraveModel(ModelPart model) {
        this.pot = model.getChild("pot");
        this.sapling_r1 = this.pot.getChild("sapling_r1");
        this.sapling_r2 = this.pot.getChild("sapling_r2");
        this.grave = model.getChild("grave");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition pot = root.addOrReplaceChild("pot", CubeListBuilder.create()
            .texOffs(23, 38).addBox(-3.0F, -5.0F, 4.0F, 3.0F, 3.0F, 1.0F)
            .texOffs(0, 4).addBox(-4.0F, -5.0F, 1.0F, 1.0F, 3.0F, 3.0F)
            .texOffs(0, 0).addBox(-3.0F, -2.0F, 1.0F, 3.0F, 1.0F, 3.0F)
            .texOffs(16, 33).addBox(0.0F, -5.0F, 1.0F, 1.0F, 3.0F, 3.0F)
            .texOffs(39, 29).addBox(-3.0F, -5.0F, 0.0F, 3.0F, 3.0F, 1.0F)
            .texOffs(19, 42).addBox(-3.0F, -4.0F, 1.0F, 3.0F, 1.0F, 3.0F), PartPose.offset(-1.0F, 23.0F, 0.0F));

        PartDefinition sapling_r1 = pot.addOrReplaceChild("sapling_r1", CubeListBuilder.create()
            .texOffs(0, 33).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 0.0F), PartPose.offsetAndRotation(-1.5F, -7.5F, 2.5F, 0.0F, 0.7854F, 0.0F));

        PartDefinition sapling_r2 = pot.addOrReplaceChild("sapling_r2", CubeListBuilder.create()
            .texOffs(0, 33).addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 0.0F), PartPose.offsetAndRotation(-1.5F, -7.5F, 2.5F, 0.0F, -0.7854F, 0.0F));

        PartDefinition grave = root.addOrReplaceChild("grave", CubeListBuilder.create()
            .texOffs(0, 0).addBox(-7.0F, 0.0F, -5.0F, 14.0F, 1.0F, 12.0F)
            .texOffs(0, 13).addBox(-7.0F, -13.0F, -7.0F, 14.0F, 14.0F, 2.0F)
            .texOffs(32, 13).addBox(-8.0F, -13.0F, -7.5F, 1.0F, 14.0F, 3.0F)
            .texOffs(0, 29).addBox(-7.0F, -14.0F, -7.5F, 14.0F, 1.0F, 3.0F)
            .texOffs(31, 30).addBox(7.0F, -13.0F, -7.5F, 1.0F, 14.0F, 3.0F)
            .texOffs(0, 52).addBox(-6.0F, -1.0F, -5.0F, 12.0F, 1.0F, 11.0F), PartPose.offset(0.0F, 23.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }


    @Override
    public void setupAnim(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        Minecraft.getInstance().getItemRenderer().renderStatic();
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        matrixStack.pushPose();
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(180));
        matrixStack.translate(0, -1.5, 0);
        this.pot.render(matrixStack, buffer, packedLight, packedOverlay);
        this.grave.render(matrixStack, buffer, packedLight, packedOverlay);
        matrixStack.popPose();
    }

    public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}