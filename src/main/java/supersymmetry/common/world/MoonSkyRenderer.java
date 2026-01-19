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
    private static final long TICKS_PER_MC_DAY = 24000L;

    // Configuration
    private float earthSize = 20.0F;
    private float sunSize = 20.0F;
    private float earthBrightness = 1.0F;

    private boolean textureProcessed = false;

    // Star field data
    private static final int STAR_COUNT = 1500;
    private float[] starPositions;
    private float[] starBrightness;
    private float[] starSizes;
    private boolean starsInitialized = false;

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

        // Calculate realistic lunar celestial angle
        float celestialAngle = calculateLunarCelestialAngle(world.getWorldTime(), partialTicks);

        // Calculate sun brightness factor (for stars visibility)
        float sunAngleDegrees = celestialAngle * 360.0F;
        float sunElevation = 90.0F - sunAngleDegrees; // Positive when sun is above horizon
        float starVisibility = calculateStarVisibility(sunElevation);

        // Render stars first (so they appear behind sun and Earth)
        if (starVisibility > 0.01F) {
            renderStars(starVisibility);
        }

        // Render sun on the celestial sphere with realistic movement
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

    /**
     * Calculate the celestial angle for the Moon's day/night cycle
     * The Moon's day is 29.53 Earth days long
     */
    private float calculateLunarCelestialAngle(long worldTime, float partialTicks) {
        // Convert world time to lunar time
        // One lunar day = 29.53 Minecraft days = 29.53 * 24000 ticks
        float lunarDayTicks = LUNAR_MONTH_DAYS * TICKS_PER_MC_DAY;

        // Calculate position within the lunar day
        float lunarDayProgress = ((worldTime % lunarDayTicks) + partialTicks) / lunarDayTicks;

        // Normalize to 0-1 range
        lunarDayProgress = lunarDayProgress - (float) Math.floor(lunarDayProgress);

        return lunarDayProgress;
    }

    /**
     * Calculate star visibility based on sun elevation
     * Stars fade out as sun rises, fade in as sun sets
     */
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

    /**
     * Initialize the star field with random positions and brightness
     */
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

    /**
     * Render the star field
     */
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

            // Create a small quad for each star (billboard)
            // We need to calculate the right and up vectors relative to camera
            // For simplicity, we'll make them axis-aligned

            GlStateManager.color(brightness, brightness, brightness, visibility);

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

    private void renderEarthAtZenith(ResourceLocation texture, float size, int phase) {
        GlStateManager.pushMatrix();

        // Rotate straight upward – zenith (Moon is tidally locked)
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);

        // Rotate the Earth texture 90 degrees clockwise
        GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);

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
