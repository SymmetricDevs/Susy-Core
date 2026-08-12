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

        Star primary = ground.findPrimaryStar();
        if (primary == null) return;

        List<Star> allStars = findAllStars(primary);
        // multiply this to make stuff move around faster :3
        double worldTime = (world.getWorldTime() + partialTicks) * 100.0;

        List<CelestialObject> candidates = collectBodies(ground, primary, allStars);
        if (candidates.isEmpty()) return;

        Map<CelestialObject, Vec3d> positions = computeAllPositions(candidates, ground, worldTime);
        Vec3d groundCenter = positions.get(ground);

        Vec3d localUp = computeLocalUp(mc, ground);
        Vec3d spinAxis = ground.getRotationAxis();
        double spinAngle = ground.getRotationAngle(worldTime);

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
            Vec3d relative = bodyPos.subtract(groundCenter);
            double distAU = relative.length();
            if (distAU < 1e-15) continue;

            if (spinAxis != null) relative = Orbit.rotateAboutAxis(relative, spinAxis, spinAngle);
            Vec3d mcDir = rotateToLocalFrame(relative, localUp).normalize();

            Vec3d lookVec = mc.player.getLook(partialTicks);
            if (mcDir.dotProduct(lookVec) < -0.1) continue;
            if (isOccludedBySphere(groundCenter, relative, body, positions, candidates)) continue;

            double bodyRadiusAU = body.getRadiusAU();
            double angularSizeDeg = Math.toDegrees(2.0 * Math.atan(bodyRadiusAU / distAU));

            List<StarLight> lights = computeLights(allStars, body, bodyPos, positions, localUp, spinAxis,
                    spinAngle);

            BodyRenderData data = new BodyRenderData(worldTime, body, mcDir, distAU, angularSizeDeg,
                    bodyRadiusAU, body == primary, false, lights, viewMat, projMat);

            renderer.render(data);

            if (!body.getFeatures().isEmpty()) {
                Vec3d hostCenterRender = new Vec3d(mcDir.x * 100.0, mcDir.y * 100.0, mcDir.z * 100.0);
                float hostRadiusRender = (float) (100.0 * Math.tan(Math.toRadians(angularSizeDeg / 2.0)));
                for (CelestialFeature feature : body.getFeatures()) {
                    feature.render(data, hostCenterRender, hostRadiusRender, false, localUp);
                }
            }
        }

        if (!ground.getFeatures().isEmpty()) {
            Vec3d surfaceLocalUp = Orbit.rotateAboutAxis(
                    Orbit.surfacePointToLocalUp(mc.player.posX, mc.player.posZ, ground.getRadius()),
                    ground.getRotationAxis(), -ground.getRotationAngle(worldTime));

            Vec3d sunRel = Orbit.computeAbsolutePosition(primary, worldTime)
                    .subtract(Orbit.computeAbsolutePosition(ground, worldTime));
            double sunDist = sunRel.length();
            Vec3d sunDirView = Orbit.toViewDir(sunRel.normalize(), surfaceLocalUp);
            Vec3d sunColorVec = primary.getColor();
            float intensity = (float) Math.min(1.0, primary.getMass() / (sunDist * sunDist));
            List<StarLight> groundLights = new ArrayList<>();
            groundLights.add(new StarLight(sunDirView, sunColorVec, intensity));

            BodyRenderData surfaceData = new BodyRenderData(worldTime, ground, new Vec3d(0, 1, 0), 0.0,
                    180.0, ground.getRadiusAU(), false, true, groundLights, viewMat, projMat);

            float planetRadiusView = 1000.0f;
            Vec3d hostCenterRender = new Vec3d(0, -planetRadiusView, 0);

            for (CelestialFeature feature : ground.getFeatures()) {
                feature.render(surfaceData, hostCenterRender, planetRadiusView, true, surfaceLocalUp);
            }
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
                                          Vec3d localUp, Vec3d spinAxis, double spinAngle) {
        List<StarLight> lights = new ArrayList<>();
        for (Star star : allStars) {
            if (star == target) continue;
            Vec3d starPos = positions.get(star);
            if (starPos == null) continue;

            Vec3d starRel = starPos.subtract(targetPos);
            double dist = starRel.length();
            if (dist < 1e-15) continue;

            if (spinAxis != null) starRel = Orbit.rotateAboutAxis(starRel, spinAxis, spinAngle);
            Vec3d lightDir = rotateToLocalFrame(starRel, localUp).normalize();
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
        if (mc.player == null) return new Vec3d(0, 1, 0);
        return Orbit.surfacePointToLocalUp(mc.player.posX, mc.player.posZ, ground.getRadius());
    }

    private static Vec3d rotateToLocalFrame(Vec3d v, Vec3d localUp) {
        return Orbit.rotateToLocalFrame(v, localUp);
    }

    private static void addDescendants(CelestialObject node, List<CelestialObject> result) {
        for (CelestialObject child : node.getChildBodies()) {
            result.add(child);
            addDescendants(child, result);
        }
    }
}
