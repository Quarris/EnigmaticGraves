package dev.quarris.enigmaticgraves.content;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import dev.quarris.enigmaticgraves.utils.ModRef;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

import java.util.function.Function;

public class GraveEntityRenderer extends EntityRenderer<GraveEntity> {

    private final GraveModel model = new GraveModel();
    private static final ResourceLocation TEX = ModRef.res("textures/grave.png");

    public GraveEntityRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void render(GraveEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        matrixStackIn.push();
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-entityYaw+180));
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
        this.model.render(matrixStackIn, bufferIn.getBuffer(this.getRenderType(entityIn)), packedLightIn, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        matrixStackIn.pop();
    }

    private RenderType getRenderType(GraveEntity entity) {
        return this.model.getRenderType(this.getEntityTexture(entity));
    }

    @Override
    public ResourceLocation getEntityTexture(GraveEntity entity) {
        return TEX;
    }
}
