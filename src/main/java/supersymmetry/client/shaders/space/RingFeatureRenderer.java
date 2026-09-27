package supersymmetry.client.shaders.space;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.Vec3d;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import supersymmetry.api.space.BodyRenderData;
import supersymmetry.api.space.CelestialFeature;
import supersymmetry.api.space.StarLight;
import supersymmetry.client.shaders.ShaderManager;
import supersymmetry.client.shaders.util.ShaderUtils;

public class RingFeatureRenderer implements CelestialFeature {

    private static final double THICKNESS = 0.1;

    private final double innerRadius;
    private final double outerRadius;
    private final double thickness;
    private final Vec3d color;

    public RingFeatureRenderer(double innerRadius, double outerRadius, Vec3d color) {
        this(innerRadius, outerRadius, THICKNESS, color);
    }

    public RingFeatureRenderer(double innerRadius, double outerRadius, double thickness, Vec3d color) {
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        this.thickness = thickness;
        this.color = color;
    }

    @Override
    public void render(BodyRenderData context, Vec3d hostCenterRender, float hostRadiusRender,
                       boolean fromSurface, Vec3d localUp) {
        if (!ShaderManager.shadersAllowed()) return;

        float[] viewMat = context.viewMatrix;
        float[] projMat = context.projectionMatrix;
        if (viewMat == null || projMat == null) return;

        int progId = ShaderManager.getRawProgram("ring.vert", "ring.frag");
        if (progId <= 0) return;

        Vec3d ringNormal = context.source.getRotationAxis();
        if (ringNormal == null) ringNormal = new Vec3d(0, 1, 0);
        Vec3d ringNormalView = ringNormal.normalize();

        Vec3d sunDirView;
        Vec3d sunColorVec;
        if (!context.lights.isEmpty()) {
            StarLight light = context.lights.get(0);
            sunDirView = light.direction;
            sunColorVec = light.color;
        } else {
            sunDirView = new Vec3d(0, 1, 0);
            sunColorVec = new Vec3d(1, 1, 1);
        }

        float[] hostCenter = new float[] { (float) hostCenterRender.x, (float) hostCenterRender.y,
                (float) hostCenterRender.z };
        float hostRadius = hostRadiusRender;
        float[] ringNormalArr = new float[] { (float) ringNormalView.x, (float) ringNormalView.y,
                (float) ringNormalView.z };
        float[] sunDir = new float[] { (float) sunDirView.x, (float) sunDirView.y, (float) sunDirView.z };
        float[] sunColor = new float[] { (float) sunColorVec.x, (float) sunColorVec.y, (float) sunColorVec.z };
        float[] ringColor = new float[] { (float) color.x, (float) color.y, (float) color.z };

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.disableCull();
        GL11.glViewport(0, 0,
                Minecraft.getMinecraft().displayWidth,
                Minecraft.getMinecraft().displayHeight);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        GL20.glUseProgram(progId);

        ShaderUtils.set3f(progId, "u_hostCenter", hostCenter);
        ShaderUtils.set1f(progId, "u_hostRadius", hostRadius);
        ShaderUtils.set3f(progId, "u_ringNormal", ringNormalArr);
        ShaderUtils.set1f(progId, "u_ringInner", (float) (innerRadius * hostRadius));
        ShaderUtils.set1f(progId, "u_ringOuter", (float) (outerRadius * hostRadius));
        ShaderUtils.set1f(progId, "u_ringThickness", (float) (thickness * hostRadius));
        ShaderUtils.set3f(progId, "u_ringColor", ringColor);
        ShaderUtils.set3f(progId, "u_sunDir", sunDir);
        ShaderUtils.set3f(progId, "u_sunColor", sunColor);
        ShaderUtils.setMat4(progId, "u_invView", ShaderUtils.invertMat4(viewMat));
        ShaderUtils.setMat4(progId, "u_invProjection", ShaderUtils.invertMat4(projMat));
        ShaderUtils.set1f(progId, "u_fromSurface", fromSurface ? 1.0f : 0.0f);

        ShaderUtils.drawFullScreenQuad();

        GL20.glUseProgram(0);
        GL11.glPopAttrib();
    }
}
