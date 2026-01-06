package supersymmetry.common.mui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;

import org.lwjgl.opengl.GL11;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.client.utils.RenderUtil;
import supersymmetry.api.rocketry.rockets.AFSRendered;

public class RocketRenderWidget extends Widget {

    private static final double SQRT_2 = Math.sqrt(2.0);
    private static final double SQRT_3 = Math.sqrt(3.0);

    public Entity entity;
    public Render<Entity> renderer;
    public float interpolation = 0f;
    public AxisAlignedBB modelAABB;
    public double scale;
    public double dvdZone;
    public boolean shouldGoSideToSide;

    public RocketRenderWidget(Size size, Position pos, Entity entity) {
        super(pos, size);
        if (entity instanceof AFSRendered r) {
            this.modelAABB = r.modelAABB();
        } else {
            throw new RuntimeException();
        }
        this.entity = entity;
        this.renderer = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(entity);
        double sideLen = Math.max(
                Math.abs(modelAABB.minX) + Math.abs(modelAABB.maxX),
                Math.abs(modelAABB.minZ) + Math.abs(modelAABB.maxZ));
        scale = ((float) size.height / sideLen);
        shouldGoSideToSide = (modelAABB.maxY * scale) > size.width;
        double rscale = scale * modelAABB.maxY;
        dvdZone = rscale - size.width;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position pos = this.getPosition();
        Size size = this.getSize();
        RenderUtil.useScissor(
                pos.x, pos.y, size.width, size.height, () -> render(partialTicks, pos, size));
    }

    private void render(float partialTicks, Position pos, Size size) {
        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();
        {
            double offset = (dvdZone * (interpolation * SQRT_3 * 0.2)) % (dvdZone * 2);
            double x = shouldGoSideToSide ? ((offset < dvdZone) ? pos.x - offset : pos.x - dvdZone * 2 + offset) :
                    pos.x;
            GlStateManager.translate(x, pos.y + size.height / 2, 1.0F);
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.rotate(270, 0, 0, 1);
            interpolation = (interpolation + partialTicks / 100);
            GlStateManager.rotate((float) (interpolation * 45 * SQRT_2), 0, 1, 0);
            GlStateManager.color(0f, 1f, 0f);
            GlStateManager.disableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.disableCull();
            GlStateManager.disableDepth();
            GlStateManager.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

            GlStateManager.colorMask(false, true, false, true);
            renderer.doRender(entity, 0, 0, 0, 0, partialTicks);
            GlStateManager.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
            GlStateManager.enableTexture2D();
            GlStateManager.enableCull();
            GlStateManager.enableDepth();
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.color(1f, 1f, 1f);
            GlStateManager.colorMask(true, true, true, true);
        }
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }
}
