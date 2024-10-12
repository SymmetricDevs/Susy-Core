package supersymmetry.client.renderer.handler;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import supersymmetry.Supersymmetry;
import supersymmetry.common.entities.EntityRocket;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class RocketRenderer extends Render<EntityRocket> {

    private static final ResourceLocation texture = new ResourceLocation(Supersymmetry.MODID, "textures/entities/rocket.png");
    protected RocketModel model = new RocketModel();

    public RocketRenderer(RenderManager renderManagerIn){
        super(renderManagerIn);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityRocket entity) {
        return texture;
    }

    @Override
    public void doRender(EntityRocket entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        this.setupTranslation(x, y, z);
        this.bindEntityTexture(entity);
        float scaleX = 0.0625F, scaleY = 0.0625F, scaleZ = 0.0625F;
        GlStateManager.scale(scaleX, scaleY, scaleZ);
        GlStateManager.rotate(180, 1.0F, 0.0F, 0.5F);
        this.model.render(entity, partialTicks, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    public void setupTranslation(double x, double y, double z) {
        GlStateManager.translate((float)x, (float)y + 2.F, (float)z);
    }

}
