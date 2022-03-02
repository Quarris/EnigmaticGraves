package dev.quarris.enigmaticgraves.content;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import dev.quarris.enigmaticgraves.utils.ModRef;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GraveEntityRenderer extends EntityRenderer<GraveEntity> {

    private final GraveModel model = new GraveModel();
    private static final ResourceLocation TEX = ModRef.res("textures/grave.png");

    public GraveEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(GraveEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        matrixStackIn.pushPose();
        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-entityYaw+180));
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
        this.model.renderToBuffer(matrixStackIn, bufferIn.getBuffer(this.getRenderType(entityIn)), packedLightIn, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);

        /*
        FontRenderer font = this.getFontRendererFromRenderManager();
        String name = entityIn.getOwnerName();
        float scale = Math.min(0.014f, 0.014f / (font.getStringWidth(name) / 55f));
        matrixStackIn.translate(0, 3/16f, 4.99/16f);
        matrixStackIn.scale(-scale, -scale, scale);
        float nameWidth = font.getStringWidth(name);
        float nameHeight = font.FONT_HEIGHT;
        font.drawString(matrixStackIn, name, -nameWidth / 2, -nameHeight / 2, 0xffffff);

         */

        matrixStackIn.popPose();
    }

    private RenderType getRenderType(GraveEntity entity) {
        return this.model.renderType(this.getTextureLocation(entity));
    }

    @Override
    public ResourceLocation getTextureLocation(GraveEntity entity) {
        return TEX;
    }
}
