package supersymmetry.common.world;

import java.util.Random;

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

    // Star field data
    private static final int STAR_COUNT = 1500;
    private float[] starPositions;
    private float[] starBrightness;
    private float[] starSizes;
    private boolean starsInitialized = false;
    private boolean renderStars = true;
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

    /**
     * Enable or disable star rendering
     */
    public void setRenderStars(boolean render) {
        this.renderStars = render;
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

        // Render stars if enabled
        if (renderStars) {
            float sunElevation = calculateSunElevation(celestialAngle);
            float starVisibility = calculateStarVisibility(sunElevation);
            if (starVisibility > 0.0F) {
                renderStars(starVisibility);
            }
        }

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

    private float calculateSunElevation(float celestialAngle) {
        // Convert celestial angle to elevation (-90 to +90 degrees)
        float angle = celestialAngle * 360.0F;
        if (angle > 180.0F) angle -= 360.0F;
        return 90.0F - angle;
    }

    private float calculateStarVisibility(float sunElevation) {
        // Stars fully visible when sun is 18° below horizon (astronomical twilight)
        // Stars invisible when sun is above horizon
        if (sunElevation > 0) {
            return 0.0F; // Sun is up, no stars
        } else if (sunElevation < -18.0F) {
            return 1.0F; // Full night, full stars
        } else {
            // Twilight zone: fade stars in/out
            return (-sunElevation) / 18.0F;
        }
    }

    private void initializeStars() {
        if (starsInitialized) return;

        starPositions = new float[STAR_COUNT * 3]; // x, y, z for each star
        starBrightness = new float[STAR_COUNT];
        starSizes = new float[STAR_COUNT];

        Random rand = new Random(10842L); // Fixed seed for consistent stars

        for (int i = 0; i < STAR_COUNT; i++) {
            // Generate random point on a sphere
            float theta = rand.nextFloat() * (float) Math.PI * 2.0F; // Azimuth
            float phi = (float) Math.acos(2.0F * rand.nextFloat() - 1.0F); // Elevation

            float distance = 100.0F; // Same distance as celestial sphere

            starPositions[i * 3] = distance * (float) Math.sin(phi) * (float) Math.cos(theta);
            starPositions[i * 3 + 1] = distance * (float) Math.sin(phi) * (float) Math.sin(theta);
            starPositions[i * 3 + 2] = distance * (float) Math.cos(phi);

            // Random brightness (some stars brighter than others)
            starBrightness[i] = 0.5F + rand.nextFloat() * 0.5F;

            // Random size (most small, some larger)
            starSizes[i] = rand.nextFloat() < 0.9F ? 0.15F : 0.15F + rand.nextFloat() * 0.2F;
        }

        starsInitialized = true;
    }

    private void renderStars(float visibility) {
        if (!starsInitialized) {
            initializeStars();
        }

        GlStateManager.pushMatrix();

        // Disable texture for point rendering
        GlStateManager.disableTexture2D();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();

        // Render stars as points
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        for (int i = 0; i < STAR_COUNT; i++) {
            float x = starPositions[i * 3];
            float y = starPositions[i * 3 + 1];
            float z = starPositions[i * 3 + 2];

            float brightness = starBrightness[i] * visibility;
            float size = starSizes[i];

            // Simple quad facing the camera
            buffer.pos(x - size, y - size, z).color(brightness, brightness, brightness, visibility).endVertex();
            buffer.pos(x - size, y + size, z).color(brightness, brightness, brightness, visibility).endVertex();
            buffer.pos(x + size, y + size, z).color(brightness, brightness, brightness, visibility).endVertex();
            buffer.pos(x + size, y - size, z).color(brightness, brightness, brightness, visibility).endVertex();
        }

        tess.draw();

        // Re-enable texture
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.popMatrix();
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
