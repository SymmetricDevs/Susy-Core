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
import supersymmetry.api.rocketry.rockets.AFSRendered;

public class RocketRenderWidget extends Widget {

    public Entity entity;
    public Render<Entity> renderer;
    public float interpolation = 0f;
    public AxisAlignedBB model_aabb;
    private static final double PHI = (1.0 + Math.sqrt(5.0)) / 2.0;
    private static final double SQRT_2 = Math.sqrt(2.0);
    private static final double SQRT_3 = Math.sqrt(3.0);

    public RocketRenderWidget(Size size, Position pos, Entity entity) {
        super(pos, size);
        if (entity instanceof AFSRendered r) {
            this.model_aabb = r.aabb();

        } else {
            throw new RuntimeException();
        }
        this.entity = entity;
        this.renderer = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(entity);
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        GlStateManager.disableDepth();
        // Vec3d cam = Minecraft.getMinecraft().player.getPositionEyes(partialTicks);
        var pos = this.getPosition();
        var size = this.getSize();
        {
            // GlStateManager.pushMatrix();
            //
            // // GlStateManager.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
            // GlStateManager.disableDepth();
            // GlStateManager.disableCull();
            //
            // GlStateManager.translate(pos.x, pos.y, 1.0F);

            // GlStateManager.enableDepth();
            // GlStateManager.enableCull();
            // GlStateManager.popMatrix();
            // this.drawRect2D(pos.x, pos.y, pos.x + size.width, pos.y + size.height, 1, 0, 0, 1);
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate(mouseX, mouseY, 1.0F); // replace this with pos later
        var scale = 15d;
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
        GlStateManager.disableCull();
        GlStateManager.disableDepth();
        GlStateManager.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

        var a = this.model_aabb;
        this.drawBox(new Vec3d(a.minX, a.minY, a.minZ), new Vec3d(a.maxX, a.maxY, a.maxZ), 1, 0, 0, 1);

        GlStateManager.colorMask(false, true, false, true);
        renderer.doRender(entity, 0, 0, 0, 0, partialTicks);

        GlStateManager.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        GlStateManager.enableTexture2D();
        GlStateManager.enableCull();
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();

        GlStateManager.color(1f, 1f, 1f);
        GlStateManager.colorMask(true, true, true, true);

        GlStateManager.popMatrix();
    }

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

    public static void drawRect2D(
                                  double x1, double z1, double x2, double z2, float r, float g, float b, float a) {
        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        double y = 0.0D;
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x1, y, z1).color(r, g, b, a).endVertex();
        buffer.pos(x1, y, z2).color(r, g, b, a).endVertex();
        buffer.pos(x2, y, z2).color(r, g, b, a).endVertex();
        buffer.pos(x2, y, z1).color(r, g, b, a).endVertex();
        tessellator.draw();

        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    // this was to see the bounding box :goog:

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
}
