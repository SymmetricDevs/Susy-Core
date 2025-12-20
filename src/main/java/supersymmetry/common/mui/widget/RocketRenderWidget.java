package supersymmetry.common.mui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import org.lwjgl.opengl.GL11;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.client.utils.RenderUtil;
import supersymmetry.api.rocketry.rockets.AFSRendered;

public class RocketRenderWidget extends Widget {

    private static final double PHI = (1.0 + Math.sqrt(5.0)) / 2.0;
    private static final double SQRT_2 = Math.sqrt(2.0);
    private static final double SQRT_3 = Math.sqrt(3.0);

    public static float[] getRotationMatrix(double t) {
        double ax = (t * PHI) % 1.0;
        double ay = (t * SQRT_2) % 1.0;
        double az = (t * SQRT_3) % 1.0;
        float thetaX = (float) Math.toRadians(ax * 360.0);
        float thetaY = (float) Math.toRadians(ay * 360.0);
        float thetaZ = (float) Math.toRadians(az * 360.0);
        float cX = (float) Math.cos(thetaX);
        float sX = (float) Math.sin(thetaX);
        // formatter eats the square
        float[] Rx = { 1, 0, 0, 0, 0, cX, sX, 0, 0, -sX, cX, 0, 0, 0, 0, 1 };
        float cY = (float) Math.cos(thetaY);
        float sY = (float) Math.sin(thetaY);
        float[] Ry = { cY, 0, -sY, 0, 0, 1, 0, 0, sY, 0, cY, 0, 0, 0, 0, 1 };
        float cZ = (float) Math.cos(thetaZ);
        float sZ = (float) Math.sin(thetaZ);
        float[] Rz = { cZ, sZ, 0, 0, -sZ, cZ, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
        float[] temp = matrixMult(Rz, Ry);
        return matrixMult(temp, Rx);
    }

    public static void drawBox(Vec3d min, Vec3d max, float r, float g, float b, float a) {
        double x0 = min.x;
        double y0 = min.y;
        double z0 = min.z;

        double x1 = max.x;
        double y1 = max.y;
        double z1 = max.z;
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.disableTexture2D();
        // GlStateManager.enableBlend();
        GlStateManager.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableDepth();

        Tessellator tess = Tessellator.getInstance();
        var buffer = tess.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        addLine(buffer, x0, y0, z0, x1, y0, z0, r, g, b, a);
        addLine(buffer, x1, y0, z0, x1, y0, z1, r, g, b, a);
        addLine(buffer, x1, y0, z1, x0, y0, z1, r, g, b, a);
        addLine(buffer, x0, y0, z1, x0, y0, z0, r, g, b, a);

        // Top rectangle
        addLine(buffer, x0, y1, z0, x1, y1, z0, r, g, b, a);
        addLine(buffer, x1, y1, z0, x1, y1, z1, r, g, b, a);
        addLine(buffer, x1, y1, z1, x0, y1, z1, r, g, b, a);
        addLine(buffer, x0, y1, z1, x0, y1, z0, r, g, b, a);

        // Vertical edges
        addLine(buffer, x0, y0, z0, x0, y1, z0, r, g, b, a);
        addLine(buffer, x1, y0, z0, x1, y1, z0, r, g, b, a);
        addLine(buffer, x1, y0, z1, x1, y1, z1, r, g, b, a);
        addLine(buffer, x0, y0, z1, x0, y1, z1, r, g, b, a);

        tess.draw();
        // GlStateManager.enableDepth();
        // GlStateManager.disableBlend();
        // GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    private static float[] matrixMult(float[] a, float[] b) {
        float[] out = new float[16];
        for (int row = 0; row < 4; ++row) {
            for (int col = 0; col < 4; ++col) {
                out[col * 4 + row] = a[0 * 4 + row] * b[col * 4 + 0] + a[1 * 4 + row] * b[col * 4 + 1] +
                        a[2 * 4 + row] * b[col * 4 + 2] + a[3 * 4 + row] * b[col * 4 + 3];
            }
        }
        return out;
    }

    // was easier to copy paste this way <3
    private static void addLine(
                                BufferBuilder buffer,
                                double x1,
                                double y1,
                                double z1,
                                double x2,
                                double y2,
                                double z2,
                                float r,
                                float g,
                                float b,
                                float a) {
        buffer.pos(x1, y1, z1).color(r, g, b, a).endVertex();
        buffer.pos(x2, y2, z2).color(r, g, b, a).endVertex();
    }

    public Entity entity;

    public Render<Entity> renderer;

    public float interpolation = 0f;

    public AxisAlignedBB model_aabb;
    public double scale;
    public boolean should_go_side_to_side = false;

    // this was to see the bounding box :goog:

    public RocketRenderWidget(Size size, Position pos, Entity entity) {
        super(pos, size);
        if (entity instanceof AFSRendered r) {
            this.model_aabb = r.aabb();

        } else {
            throw new RuntimeException();
        }
        this.entity = entity;
        this.renderer = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(entity);
        var side_len = Math.max(
                Math.abs(model_aabb.minX) + Math.abs(model_aabb.maxX),
                Math.abs(model_aabb.minZ) + Math.abs(model_aabb.maxZ)); // this is in blocks i think
        scale = ((float) size.height / side_len);
        should_go_side_to_side = (model_aabb.maxY * scale) > size.width; // probably always true
        var dvdzone = 0;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        // Vec3d cam = Minecraft.getMinecraft().player.getPositionEyes(partialTicks);
        var pos = this.getPosition();
        var size = this.getSize();

        RenderUtil.useScissor(
                pos.x,
                pos.y,
                size.width,
                size.height,
                () -> {
                    GlStateManager.pushAttrib();
                    GlStateManager.pushMatrix();
                    {
                        GlStateManager.translate(pos.x, pos.y + size.height / 2, 1.0F);
                        GlStateManager.scale(scale, scale, scale);
                        GlStateManager.rotate(270, 0, 0, 1);

                        interpolation = (interpolation + partialTicks / 100);

                        // SusyLog.logger.info(angle);
                        // var m = getRotationMatrix(interpolation);
                        // FloatBuffer buf = BufferUtils.createFloatBuffer(16);
                        // buf.put(m);
                        // buf.flip();
                        // GlStateManager.multMatrix(buf);
                        GlStateManager.rotate(interpolation * 40, 0, 1, 0);

                        GlStateManager.color(0f, 1f, 0f);

                        GlStateManager.disableTexture2D(); // stuff for the wireframe
                        GlStateManager.disableBlend();
                        GlStateManager.disableAlpha();
                        GlStateManager.disableCull();
                        GlStateManager.disableDepth();
                        GlStateManager.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

                        var a = this.model_aabb;
                        this.drawBox(
                                new Vec3d(a.minX, a.minY, a.minZ), new Vec3d(a.maxX, a.maxY, a.maxZ), 1, 0, 0, 1);

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
                });
        // //not sure why this doesnt work, would be nice if someone fixed it :goog:
        // GlStateManager.pushAttrib();
        // GlStateManager.pushMatrix();
        // {
        // GlStateManager.translate(pos.x, pos.y, 1.0F); // replace this with pos later
        // this.drawRect2D(0, 0, size.width, size.height, 1, 1, 1, 1);
        // }
        // GlStateManager.popMatrix();
        // GlStateManager.popAttrib();
    }
}
