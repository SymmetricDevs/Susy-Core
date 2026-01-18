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

public class MoonSkyRenderer extends IRenderHandler {

    // Vanilla sun texture
    private static final ResourceLocation SUN_TEXTURES = new ResourceLocation("susy",
            "textures/environment/sun.png");

    // Custom Earth texture (replaces moon)
    private static final ResourceLocation EarthFromMoon = new ResourceLocation("susy",
            "textures/environment/earth_phases.png");

    // Earth phase configuration (8 phases in a 4x2 grid)
    private static final int EARTH_PHASES_COLUMNS = 4;
    private static final int EARTH_PHASES_ROWS = 2;
    private static final int TOTAL_EARTH_PHASES = 8;
    private static final float LUNAR_MONTH_DAYS = 29.53f; // Synodic month length

    // Configuration
    private float earthSize = 20.0F;
    private float sunSize = 20.0F;
    private float earthBrightness = 1.0F;

    private boolean textureProcessed = false;

    @Override
    @SideOnly(Side.CLIENT)
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        GlStateManager.disableFog();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);

        // Disable depth test and writing - celestial objects are infinitely far
        GlStateManager.depthMask(false);

        float celestialAngle = world.getCelestialAngle(partialTicks);

        // Render sun on the celestial sphere
        renderCelestialObject(SUN_TEXTURES, sunSize, celestialAngle, 0.0F, false, 0);

        // Calculate current Earth phase
        long worldTime = world.getWorldTime();
        int currentPhase = calculateEarthPhase(worldTime);

        renderEarthAtZenith(EarthFromMoon, earthSize, currentPhase);

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableFog();
    }

    private void renderEarthAtZenith(ResourceLocation texture, float size, int phase) {
        GlStateManager.pushMatrix();

        // Rotate straight upward – zenith (Moon is tidally locked)
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);

        Minecraft.getMinecraft().renderEngine.bindTexture(texture);

        // Phase UV calc
        int column = phase % EARTH_PHASES_COLUMNS;
        int row = phase / EARTH_PHASES_COLUMNS;

        float uMin = column / (float) EARTH_PHASES_COLUMNS;
        float uMax = (column + 1) / (float) EARTH_PHASES_COLUMNS;
        float vMin = row / (float) EARTH_PHASES_ROWS;
        float vMax = (row + 1) / (float) EARTH_PHASES_ROWS;

        // Mirror to face correct direction
        float tmp = uMin;
        uMin = uMax;
        uMax = tmp;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        // After 90° X rotation, -Z points to zenith
        buffer.pos(-size, -size, -100.0D).tex(uMin, vMin).endVertex();
        buffer.pos(size, -size, -100.0D).tex(uMax, vMin).endVertex();
        buffer.pos(size, size, -100.0D).tex(uMax, vMax).endVertex();
        buffer.pos(-size, size, -100.0D).tex(uMin, vMax).endVertex();

        tess.draw();

        GlStateManager.popMatrix();
    }

    private void renderCelestialObject(ResourceLocation texture, float size, float celestialAngle,
                                       float azimuthOffset, boolean hasPhases, int phase) {
        GlStateManager.pushMatrix();

        // Rotate to position on celestial sphere
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(azimuthOffset * 360.0F, 0.0F, 0.0F, 1.0F);

        // Bind texture
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);

        // Process Earth texture once to remove black pixels
        if (hasPhases && !textureProcessed) {
            removeBlackPixelsFromTexture();
            textureProcessed = true;
        }

        // Use nearest neighbor filtering
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
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

        if (hasPhases) {
            int column = phase % EARTH_PHASES_COLUMNS;
            int row = phase / EARTH_PHASES_COLUMNS;

            uMin = column / (float) EARTH_PHASES_COLUMNS;
            uMax = (column + 1) / (float) EARTH_PHASES_COLUMNS;
            vMin = row / (float) EARTH_PHASES_ROWS;
            vMax = (row + 1) / (float) EARTH_PHASES_ROWS;

            // Swap UVs to flip the texture horizontally (mirror it)
            float temp = uMin;
            uMin = uMax;
            uMax = temp;
        }

        // Render as billboard on celestial sphere (distance = 100)
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(-size, 100.0D, -size).tex(uMin, vMin).endVertex();
        buffer.pos(size, 100.0D, -size).tex(uMax, vMin).endVertex();
        buffer.pos(size, 100.0D, size).tex(uMax, vMax).endVertex();
        buffer.pos(-size, 100.0D, size).tex(uMin, vMax).endVertex();
        tessellator.draw();

        GlStateManager.popMatrix();
    }

    private void removeBlackPixelsFromTexture() {
        try {
            // Get current texture dimensions using direct LWJGL calls
            int w = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
            int h = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);

            if (w == 0 || h == 0) return;

            // Get texture pixels
            java.nio.ByteBuffer buffer = org.lwjgl.BufferUtils.createByteBuffer(w * h * 4);
            GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

            // Process each pixel
            for (int i = 0; i < w * h * 4; i += 4) {
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;

                // If pixel is pure black (0,0,0), make it transparent
                if (r == 0 && g == 0 && b == 0) {
                    buffer.put(i, (byte) 0);     // R = 0
                    buffer.put(i + 1, (byte) 0); // G = 0
                    buffer.put(i + 2, (byte) 0); // B = 0
                    buffer.put(i + 3, (byte) 0); // A = 0 (fully transparent)
                }
            }

            // Re-upload the texture
            buffer.rewind();
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
                    buffer);

        } catch (Exception e) {
            // Silently fail if texture processing doesn't work
        }
    }

    /**
     * Calculate which Earth phase to display based on world time
     * Earth phases as seen from Moon are opposite to Moon phases from Earth
     */
    private int calculateEarthPhase(long worldTime) {
        // Convert ticks to days (24000 ticks per Minecraft day)
        float minecraftDays = worldTime / 24000.0f;

        // Calculate position in lunar month
        float phaseProgress = (minecraftDays % LUNAR_MONTH_DAYS) / LUNAR_MONTH_DAYS;

        // Map to one of 8 phases
        int phase = (int) (phaseProgress * TOTAL_EARTH_PHASES) % TOTAL_EARTH_PHASES;

        return phase;
    }

    public void setEarthSize(float size) {
        this.earthSize = size;
    }

    public void setSunSize(float size) {
        this.sunSize = size;
    }

    public void setEarthBrightness(float brightness) {
        this.earthBrightness = brightness;
    }
}
