package supersymmetry.common.world;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import supersymmetry.api.SusyLog;
import supersymmetry.common.world.sky.*;

public class SuSySkyRenderer extends IRenderHandler {

    // Default sun texture
    public static final ResourceLocation SUN_TEXTURE = new ResourceLocation("susy", "textures/environment/sun.png");

    // Celestial objects to render (passed from outside, not stored in renderer)
    private SkyRenderData[] celestialObjects;

    private boolean debugLogged = false;

    public SuSySkyRenderer() {
        this.celestialObjects = new SkyRenderData[0];
        SusyLog.logger.info("SuSySkyRenderer created");
    }

    /**
     * Set the celestial objects to render
     *
     * @param objects Array of render data for celestial objects
     */
    public void setCelestialObjects(SkyRenderData... objects) {
        this.celestialObjects = objects;
        SusyLog.logger.info("Set " + objects.length + " celestial objects");
        for (int i = 0; i < objects.length; i++) {
            SusyLog.logger.info("  Object " + i + ": texture=" + objects[i].getTexture() +
                    ", type=" + objects[i].getPositionType() +
                    ", size=" + objects[i].getSize());
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        if (!debugLogged) {
            SusyLog.logger.info("SuSySkyRenderer.render() called with " + celestialObjects.length + " objects");
            debugLogged = true;
        }

        // Setup render state
        GlStateManager.disableFog();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);

        // Disable depth test and writing - celestial objects are infinitely far
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableTexture2D();

        float celestialAngle = world.getCelestialAngle(partialTicks);
        long worldTime = world.getWorldTime();

        // Render all celestial objects
        for (int i = 0; i < celestialObjects.length; i++) {
            SkyRenderData obj = celestialObjects[i];
            try {
                if (obj.getPositionType() == SkyRenderData.PositionType.CELESTIAL_SPHERE) {
                    renderCelestialSphereObject(obj, celestialAngle, worldTime);
                } else if (obj.getPositionType() == SkyRenderData.PositionType.ZENITH) {
                    renderZenithObject(obj, worldTime);
                }
            } catch (Exception e) {
                SusyLog.logger.error("Error rendering celestial object " + i, e);
            }
        }

        // Restore render state
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableFog();
    }

    private void renderCelestialSphereObject(SkyRenderData data, float celestialAngle, long worldTime) {
        GlStateManager.pushMatrix();

        // Rotate to position on celestial sphere
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(data.getAzimuthOffset() * 360.0F, 0.0F, 0.0F, 1.0F);

        // Bind texture
        ResourceLocation texture = data.getTexture();
        if (texture == null) {
            SusyLog.logger.warn("Celestial sphere object has null texture!");
            texture = SUN_TEXTURE;
        }
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);

        // Set texture parameters
        if (data.useLinearFiltering()) {
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        } else {
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableTexture2D();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        // Calculate UV coordinates
        float uMin = 0.0F;
        float uMax = 1.0F;
        float vMin = 0.0F;
        float vMax = 1.0F;

        if (data.hasPhases()) {
            int phase = data.getCurrentPhase(worldTime);
            SkyRenderData.PhaseData phaseData = data.getPhaseData();

            int column = phase % phaseData.getColumns();
            int row = phase / phaseData.getColumns();

            uMin = column / (float) phaseData.getColumns();
            uMax = (column + 1) / (float) phaseData.getColumns();
            vMin = row / (float) phaseData.getRows();
            vMax = (row + 1) / (float) phaseData.getRows();

            if (data.shouldMirrorTexture()) {
                float temp = uMin;
                uMin = uMax;
                uMax = temp;
            }
        }

        float size = data.getSize();

        // Render as billboard on celestial sphere (distance = 100)
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(-size, 100.0D, -size).tex(uMin, vMin).endVertex();
        buffer.pos(size, 100.0D, -size).tex(uMax, vMin).endVertex();
        buffer.pos(size, 100.0D, size).tex(uMax, vMax).endVertex();
        buffer.pos(-size, 100.0D, size).tex(uMin, vMax).endVertex();
        tessellator.draw();

        GlStateManager.popMatrix();
    }

    private void renderZenithObject(SkyRenderData data, long worldTime) {
        GlStateManager.pushMatrix();

        // Rotate to zenith
        GlStateManager.rotate(data.getRotationX(), 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(data.getRotationZ(), 0.0F, 0.0F, 1.0F);

        // Bind texture
        ResourceLocation texture = data.getTexture();
        if (texture == null) {
            SusyLog.logger.warn("Zenith object has null texture!");
            texture = SUN_TEXTURE;
        }
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);

        // Set texture parameters
        if (data.useLinearFiltering()) {
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        } else {
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        // Apply brightness
        float b = data.getBrightness();
        GlStateManager.color(b, b, b, 1.0F);

        // Calculate UV coordinates
        float uMin = 0.0F;
        float uMax = 1.0F;
        float vMin = 0.0F;
        float vMax = 1.0F;

        if (data.hasPhases()) {
            int phase = data.getCurrentPhase(worldTime);
            SkyRenderData.PhaseData phaseData = data.getPhaseData();

            int column = phase % phaseData.getColumns();
            int row = phase / phaseData.getColumns();

            uMin = column / (float) phaseData.getColumns();
            uMax = (column + 1) / (float) phaseData.getColumns();
            vMin = row / (float) phaseData.getRows();
            vMax = (row + 1) / (float) phaseData.getRows();

            if (data.shouldMirrorTexture()) {
                float temp = uMin;
                uMin = uMax;
                uMax = temp;
            }
        }

        float size = data.getSize();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        // After 90° X rotation, -Z points to zenith
        buffer.pos(-size, -size, -100.0D).tex(uMin, vMin).endVertex();
        buffer.pos(size, -size, -100.0D).tex(uMax, vMin).endVertex();
        buffer.pos(size, size, -100.0D).tex(uMax, vMax).endVertex();
        buffer.pos(-size, size, -100.0D).tex(uMin, vMax).endVertex();

        tess.draw();

        // Reset color
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.popMatrix();
    }
}
