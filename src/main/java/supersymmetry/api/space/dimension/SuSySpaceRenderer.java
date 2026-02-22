package supersymmetry.api.space.dimension;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.IRenderHandler;

import supersymmetry.api.space.QuadSphere;
import supersymmetry.api.space.RenderableCelestialObject;

/**
 * Sky renderer for space dimensions.
 *
 * <p>
 * Each {@link RenderableCelestialObject} is rendered at its correct orbital
 * position on the celestial sphere using {@link RenderableCelestialObject#renderAtPosition},
 * rather than all objects being drawn as overlapping full-sky spheres.
 * </p>
 */
public class SuSySpaceRenderer extends IRenderHandler {

    private RenderableCelestialObject[] objects = new RenderableCelestialObject[0];
    private final QuadSphere mesh = new QuadSphere(4);

    public SuSySpaceRenderer setCelestialObjects(RenderableCelestialObject... objs) {
        this.objects = (objs != null) ? objs : new RenderableCelestialObject[0];
        return this;
    }

    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        if (objects.length == 0) return;

        long worldTime = world.getWorldTime();

        GlStateManager.disableFog();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();

        GlStateManager.pushMatrix();

        // Align celestial sphere: rotate so +Y is up, then spin by the
        // dimension's celestial angle to drive the day/night cycle.
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(world.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);

        for (RenderableCelestialObject obj : objects) {
            obj.renderAtPosition(worldTime, mesh);
        }

        GlStateManager.popMatrix();

        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableAlpha();
        GlStateManager.enableFog();
    }
}
