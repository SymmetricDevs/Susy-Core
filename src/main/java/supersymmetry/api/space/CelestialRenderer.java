package supersymmetry.api.space;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import supersymmetry.client.shaders.ShaderManager;
import supersymmetry.client.shaders.util.ShaderUtils;

public class CelestialRenderer extends IRenderHandler {

    private final Map<CelestialObject, BodyRenderer> renderers = new HashMap<>();

    public void registerRenderer(CelestialObject body, BodyRenderer renderer) {
        renderers.put(body, renderer);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public final void render(float partialTicks, WorldClient world, Minecraft mc) {
        ShaderManager.ensureInitialised();

        float[] viewMat = ShaderUtils.getMatrix(GL11.GL_MODELVIEW_MATRIX);
        float[] projMat = ShaderUtils.getMatrix(GL11.GL_PROJECTION_MATRIX);

        renderSkyBackground();

        int dimId = world.provider.getDimension();
        Planetoid ground = Planetoid.PLANETOIDS.inverse().get(dimId);
        if (ground == null) return;

        Star primary = CelestialObject.findPrimaryStar(ground);
        if (primary == null) return;

        List<Star> allStars = findAllStars(primary);

        double worldTime = world.getWorldTime() + partialTicks;

        List<CelestialObject> candidates = collectBodies(ground, primary, allStars);
        if (candidates.isEmpty()) return;

        Map<CelestialObject, Vec3d> positions = computeAllPositions(candidates, ground, worldTime);
        Vec3d groundCenter = positions.get(ground);

        Vec3d localUpEcl = computeLocalUp(mc, ground);

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GlStateManager.disableFog();
        GlStateManager.disableLighting();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_FRONT);
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        for (CelestialObject body : candidates) {
            if (body == ground) continue;

            BodyRenderer renderer = renderers.get(body);
            if (renderer == null) continue;

            Vec3d bodyPos = positions.get(body);
            Vec3d relativeEcl = bodyPos.subtract(groundCenter);
            double distAU = relativeEcl.length();
            if (distAU < 1e-15) continue;

            Vec3d frameRotated = rotateToLocalFrame(relativeEcl, localUpEcl);
            Vec3d mcDir = new Vec3d(frameRotated.x, frameRotated.z, frameRotated.y).normalize();

            Vec3d lookVec = mc.player.getLook(partialTicks);
            if (mcDir.dotProduct(lookVec) < -0.1) continue;
            if (isOccludedBySphere(groundCenter, relativeEcl, body, positions, candidates)) continue;

            double bodyRadiusAU = body.getRadiusAU();
            double angularSizeDeg = Math.toDegrees(2.0 * Math.atan(bodyRadiusAU / distAU));

            List<StarLight> lights = computeLights(allStars, body, bodyPos, positions, localUpEcl);

            BodyRenderData data = new BodyRenderData(body, mcDir, distAU, angularSizeDeg, bodyRadiusAU,
                    body == primary, false, lights, viewMat, projMat);

            renderer.render(data);
        }

        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableAlpha();
        GlStateManager.enableLighting();
        GlStateManager.enableFog();
        GL11.glPopAttrib();
    }

    private void renderSkyBackground() {
        GlStateManager.disableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.disableCull();
        GlStateManager.color(0.0f, 0.0f, 0.0f, 1.0f);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        GlStateManager.depthMask(false);

        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        buf.pos(-100, -100, -100).endVertex();
        buf.pos(-100, -100, 100).endVertex();
        buf.pos(100, -100, 100).endVertex();
        buf.pos(100, -100, -100).endVertex();

        buf.pos(-100, 100, 100).endVertex();
        buf.pos(-100, 100, -100).endVertex();
        buf.pos(100, 100, -100).endVertex();
        buf.pos(100, 100, 100).endVertex();

        buf.pos(-100, -100, -100).endVertex();
        buf.pos(-100, 100, -100).endVertex();
        buf.pos(100, 100, -100).endVertex();
        buf.pos(100, -100, -100).endVertex();

        buf.pos(100, -100, -100).endVertex();
        buf.pos(100, 100, -100).endVertex();
        buf.pos(100, 100, 100).endVertex();
        buf.pos(100, -100, 100).endVertex();

        buf.pos(-100, -100, 100).endVertex();
        buf.pos(100, -100, 100).endVertex();
        buf.pos(100, 100, 100).endVertex();
        buf.pos(-100, 100, 100).endVertex();

        buf.pos(-100, -100, -100).endVertex();
        buf.pos(-100, -100, 100).endVertex();
        buf.pos(-100, 100, 100).endVertex();
        buf.pos(-100, 100, -100).endVertex();

        tess.draw();

        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    private List<StarLight> computeLights(List<Star> allStars, CelestialObject target,
                                          Vec3d targetPos, Map<CelestialObject, Vec3d> positions,
                                          Vec3d localUpEcl) {
        List<StarLight> lights = new ArrayList<>();
        for (Star star : allStars) {
            if (star == target) continue;
            Vec3d starPos = positions.get(star);
            if (starPos == null) continue;

            Vec3d starRelEcl = starPos.subtract(targetPos);
            double dist = starRelEcl.length();
            if (dist < 1e-15) continue;

            Vec3d frameRotated = rotateToLocalFrame(starRelEcl, localUpEcl);
            Vec3d lightDir = new Vec3d(frameRotated.x, frameRotated.z, frameRotated.y).normalize();
            float intensity = (float) Math.min(1.0, star.getMass() / (dist * dist));

            lights.add(new StarLight(lightDir, star.getColor(), intensity));
        }
        return lights;
    }

    private List<CelestialObject> collectBodies(Planetoid ground, Star primary, List<Star> allStars) {
        List<CelestialObject> bodies = new ArrayList<>();
        for (Star star : allStars) {
            bodies.add(star);
            for (CelestialObject child : star.getChildBodies()) {
                if (child != ground) {
                    bodies.add(child);
                }
            }
        }
        addDescendants(ground, bodies);
        return bodies;
    }

    private static List<Star> findAllStars(Star primary) {
        List<Star> stars = new ArrayList<>();
        stars.add(primary);
        if (primary.getParentBody() != null) {
            for (CelestialObject sibling : primary.getParentBody().getChildBodies()) {
                if (sibling instanceof Star && sibling != primary) {
                    stars.add((Star) sibling);
                }
            }
        }
        return stars;
    }

    private static boolean isOccludedBySphere(Vec3d origin, Vec3d toTarget, CelestialObject target,
                                              Map<CelestialObject, Vec3d> positions,
                                              List<CelestialObject> allBodies) {
        for (CelestialObject occluder : allBodies) {
            if (occluder == target) continue;
            double r = occluder.getRadiusAU();
            if (r <= 1e-15) continue;
            if (raySphereIntersects(origin, toTarget, positions.get(occluder), r)) return true;
        }
        return false;
    }

    private static boolean raySphereIntersects(Vec3d origin, Vec3d toTarget,
                                               Vec3d sphereCenter, double sphereRadius) {
        Vec3d oc = origin.subtract(sphereCenter);
        double a = toTarget.dotProduct(toTarget);
        double b = 2.0 * oc.dotProduct(toTarget);
        double c = oc.dotProduct(oc) - sphereRadius * sphereRadius;
        double disc = b * b - 4.0 * a * c;
        if (disc < 0) return false;

        double sqrtDisc = Math.sqrt(disc);
        double t1 = (-b - sqrtDisc) / (2.0 * a);
        double t2 = (-b + sqrtDisc) / (2.0 * a);

        return (t1 > 0.0 && t1 < 1.0) || (t2 > 0.0 && t2 < 1.0);
    }

    private static Map<CelestialObject, Vec3d> computeAllPositions(
                                                                   List<CelestialObject> bodies, Planetoid ground,
                                                                   double worldTime) {
        Map<CelestialObject, Vec3d> positions = new HashMap<>();
        for (CelestialObject body : bodies) {
            positions.put(body, Orbit.computeAbsolutePosition(body, worldTime));
        }
        positions.put(ground, Orbit.computeAbsolutePosition(ground, worldTime));
        return positions;
    }

    private Vec3d computeLocalUp(Minecraft mc, Planetoid ground) {
        if (mc.player == null) return new Vec3d(0, 0, 1);
        return CelestialObject.surfacePointToLocalUp(mc.player.posX, mc.player.posZ, ground.getRadius());
    }

    private static Vec3d rotateToLocalFrame(Vec3d v, Vec3d localUp) {
        Vec3d target = new Vec3d(0, 0, 1);
        double cosA = localUp.dotProduct(target);
        double sinA = Math.sqrt(Math.max(0.0, 1.0 - cosA * cosA));
        if (sinA < 1e-12) {
            if (cosA < 0) return new Vec3d(-v.x, -v.y, -v.z);
            return v;
        }
        Vec3d axis = localUp.crossProduct(target);
        axis = axis.scale(1.0 / (axis.length() + 1e-30));

        double kDotV = v.dotProduct(axis);
        Vec3d kCrossV = axis.crossProduct(v);

        return v.scale(cosA)
                .add(kCrossV.scale(sinA))
                .add(axis.scale(kDotV * (1.0 - cosA)));
    }

    private static void addDescendants(CelestialObject node, List<CelestialObject> result) {
        for (CelestialObject child : node.getChildBodies()) {
            result.add(child);
            addDescendants(child, result);
        }
    }
}
