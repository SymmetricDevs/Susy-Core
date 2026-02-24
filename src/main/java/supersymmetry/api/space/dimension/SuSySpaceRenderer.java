package supersymmetry.api.space.dimension;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.IRenderHandler;

import org.lwjgl.opengl.GL11;

import supersymmetry.api.SusyLog;
import supersymmetry.api.space.QuadSphere;
import supersymmetry.api.space.RenderableCelestialObject;

public class SuSySpaceRenderer extends IRenderHandler {

    private RenderableCelestialObject[] objects = new RenderableCelestialObject[0];
    private final QuadSphere mesh = new QuadSphere(32);
    private boolean loggedOnce = false;

    public SuSySpaceRenderer setCelestialObjects(RenderableCelestialObject... objs) {
        this.objects = (objs != null) ? objs : new RenderableCelestialObject[0];
        return this;
    }

    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        if (objects.length == 0) return;

        if (!loggedOnce) {
            SusyLog.logger.info("[Space] SuSySpaceRenderer.render() called, objects=" + objects.length);
            loggedOnce = true;
        }

        long worldTime = world.getWorldTime();

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GlStateManager.disableFog();
        GlStateManager.disableLighting();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        GlStateManager.pushMatrix();

        // Scale up so objects at unit distance fill a meaningful portion of the sky.
        // The vanilla sky dome is rendered at ~100 units radius; we match that.
        GL11.glScalef(100.0f, 100.0f, 100.0f);

        for (RenderableCelestialObject obj : objects) {
            obj.renderAtPosition(worldTime, mesh);
        }

        GlStateManager.popMatrix();

        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableAlpha();
        GlStateManager.enableLighting();
        GlStateManager.enableFog();
        GL11.glPopAttrib();
    }
}
