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

    // Lunar constants
    private static final float LUNAR_DAY_LENGTH = 29.53f; // Synodic month (Earth days)
    private static final float MINECRAFT_TICKS_PER_DAY = 24000f;

    // How many Minecraft days should equal one lunar day (29.53 Earth days)
    // Adjust this for gameplay: 1.0 = real speed, 0.1 = 10x faster, etc.
    private static final float TIME_SCALE = 0.1f; // 10x faster for gameplay

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

        // Calculate lunar sun position (slow, retrograde motion)
        long worldTime = world.getWorldTime();
        float lunarSunAngle = calculateLunarSunAngle(worldTime, partialTicks);

        // Render sun with custom lunar motion
        renderLunarSun(SUN_TEXTURES, sunSize, lunarSunAngle);

        // Calculate Earth phase based on sun position
        int currentPhase = calculateEarthPhaseFromSun(lunarSunAngle);

        // Render Earth at zenith (always overhead, tidally locked)
        renderEarthAtZenith(EarthFromMoon, earthSize, currentPhase);

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableFog();
    }

    /**
     * Calculate the sun's position in lunar sky
     * On the Moon, the sun moves slowly (29.53 day cycle) in retrograde direction
     */
    private float calculateLunarSunAngle(long worldTime, float partialTicks) {
        // Convert world time to effective lunar time
        float totalTicks = worldTime + partialTicks;
        float minecraftDays = totalTicks / MINECRAFT_TICKS_PER_DAY;

        // Scale to lunar day length
        float lunarDayProgress = (minecraftDays * TIME_SCALE) / LUNAR_DAY_LENGTH;

        // Normalize to 0-1 range
        float normalizedProgress = lunarDayProgress % 1.0f;

        // REVERSE direction (retrograde motion due to orbital movement)
        // On Earth: 0.0 = sunrise, 0.25 = noon, 0.5 = sunset, 0.75 = midnight
        // On Moon: reverse this, so sun moves west-to-east
        float reversedAngle = 1.0f - normalizedProgress;

        return reversedAngle;
    }

    /**
     * Render the sun with lunar motion characteristics
     */
    private void renderLunarSun(ResourceLocation texture, float size, float lunarSunAngle) {
        GlStateManager.pushMatrix();

        // Rotate to position on celestial sphere
        // Note: reversed rotation for retrograde motion
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(lunarSunAngle * 360.0F, 1.0F, 0.0F, 0.0F);

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

        // Render as billboard on celestial sphere (distance = 100)
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(-size, 100.0D, -size).tex(0, 0).endVertex();
        buffer.pos(size, 100.0D, -size).tex(1, 0).endVertex();
        buffer.pos(size, 100.0D, size).tex(1, 1).endVertex();
        buffer.pos(-size, 100.0D, size).tex(0, 1).endVertex();
        tessellator.draw();

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

    /**
     * Calculate which Earth phase to display based on sun position
     *
     * When sun is overhead: New Earth (phase 0 - dark, sun is behind Earth)
     * When sun is on horizon: Quarter Earth (phase 2 or 6)
     * When sun is opposite/midnight: Full Earth (phase 4 - fully lit)
     */
    private int calculateEarthPhaseFromSun(float lunarSunAngle) {
        // lunarSunAngle cycles from 0 to 1:
        // 0.75 = sunrise, 0.5 = noon, 0.25 = sunset, 0.0 = midnight (reversed from Earth)

        // We want:
        // noon (0.5) → New Earth (phase 0)
        // midnight (0.0/1.0) → Full Earth (phase 4)

        // Offset by 0.5 so that noon maps to 0
        float adjustedAngle = (lunarSunAngle + 0.5f) % 1.0f;

        // Map to 8 phases
        int phase = (int) (adjustedAngle * TOTAL_EARTH_PHASES) % TOTAL_EARTH_PHASES;

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
