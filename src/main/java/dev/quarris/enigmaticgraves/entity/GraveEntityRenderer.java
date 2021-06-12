package dev.quarris.enigmaticgraves.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

public class GraveEntityRenderer extends EntityRenderer<GraveEntity> {

    private GraveModel model = new GraveModel(RenderType::getEntityCutout);
    private static final ResourceLocation TEX = new ResourceLocation("textures/painting/back.png");

    public GraveEntityRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void render(GraveEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
        model.render(matrixStackIn, bufferIn.getBuffer(this.model.getRenderType(this.getEntityTexture(entityIn))), packedLightIn, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
    }

    @Override
    public ResourceLocation getEntityTexture(GraveEntity entity) {
        return TEX;
    }

    static class GraveModel extends Model {

        private ModelRenderer renderer;

        public GraveModel(Function<ResourceLocation, RenderType> renderTypeIn) {
            super(renderTypeIn);
            this.renderer = new ModelRenderer(this, 0, 0);
            float hor = 0.6f * 16f;
            this.renderer.addBox(-hor/2, 0, -hor/2, hor, 16, hor);
        }

        @Override
        public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
            this.renderer.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        }
    }
}
