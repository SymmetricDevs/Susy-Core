package supersymmetry.common.world;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import supersymmetry.api.SusyLog;
import supersymmetry.common.world.sky.*;

public class SuSySkyRenderer extends IRenderHandler {

    public static final ResourceLocation SUN_TEXTURE = new ResourceLocation("susy", "textures/environment/sun.png");

    private SkyRenderData[] celestialObjects;
    private SkyColorData skyColorData;
    private boolean debugLogged = false;

    public SuSySkyRenderer() {
        this.celestialObjects = new SkyRenderData[0];
        this.skyColorData = null;
        SusyLog.logger.info("SuSySkyRenderer created");
    }

    public void setCelestialObjects(SkyRenderData... objects) {
        this.celestialObjects = objects;
        SusyLog.logger.info("Set " + objects.length + " celestial objects");
        for (int i = 0; i < objects.length; i++) {
            SusyLog.logger.info("  Object " + i + ": texture=" + objects[i].getTexture() +
                    ", type=" + objects[i].getPositionType() +
                    ", size=" + objects[i].getSize());
        }
    }

    public void setSkyColorData(SkyColorData colorData) {
        this.skyColorData = colorData;
        SusyLog.logger.info("Set custom sky color data");
    }

    public SkyColorData getSkyColorData() {
        return this.skyColorData;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        if (!debugLogged) {
            SusyLog.logger.info("SuSySkyRenderer.render() called with " + celestialObjects.length + " objects");
            debugLogged = true;
        }

        float celestialAngle = world.getCelestialAngle(partialTicks);
        long worldTime = world.getWorldTime();

        // Render sky background color
        if (skyColorData != null) {
            renderSkyBackground(celestialAngle);
        }

        // Setup render state for celestial objects
        GlStateManager.disableFog();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);

        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableTexture2D();

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

    private void renderSkyBackground(float celestialAngle) {
        Vec3d skyColor = skyColorData.getSkyColor(celestialAngle);

        GlStateManager.disableTexture2D();
        GlStateManager.disableBlend();

        // Set sky color
        GlStateManager.color((float) skyColor.x, (float) skyColor.y, (float) skyColor.z, 1.0F);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        // Render sky dome
        GlStateManager.depthMask(false);

        for (int i = 0; i < 6; ++i) {
            GlStateManager.pushMatrix();

            if (i == 1) {
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            }
            if (i == 2) {
                GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            }
            if (i == 3) {
                GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
            }
            if (i == 4) {
                GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
            }
            if (i == 5) {
                GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
            }

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
            buffer.pos(-100.0D, -100.0D, -100.0D).endVertex();
            buffer.pos(-100.0D, -100.0D, 100.0D).endVertex();
            buffer.pos(100.0D, -100.0D, 100.0D).endVertex();
            buffer.pos(100.0D, -100.0D, -100.0D).endVertex();
            tessellator.draw();

            GlStateManager.popMatrix();
        }

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderCelestialSphereObject(SkyRenderData data, float celestialAngle, long worldTime) {
        GlStateManager.pushMatrix();

        // 1. Standard Rotation setup
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F); // Orient to east/west

        // 2. Apply Orbital Inclination (The "Wobble")
        // We rotate around the Z-axis (East-West axis) to tilt the path North/South
        float currentInclination = data.getCurrentInclination(worldTime);
        if (currentInclination != 0.0F) {
            GlStateManager.rotate(currentInclination, 0.0F, 0.0F, 1.0F);
        }
        GlStateManager.rotate(celestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(data.getAzimuthOffset() * 360.0F, 0.0F, 0.0F, 1.0F);

        ResourceLocation texture = data.getTexture();
        if (texture == null) {
            SusyLog.logger.warn("Celestial sphere object has null texture!");
            texture = SUN_TEXTURE;
        }
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);

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

        GlStateManager.rotate(data.getRotationX(), 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(data.getRotationZ(), 0.0F, 0.0F, 1.0F);

        ResourceLocation texture = data.getTexture();
        if (texture == null) {
            SusyLog.logger.warn("Zenith object has null texture!");
            texture = SUN_TEXTURE;
        }
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);

        if (data.useLinearFiltering()) {
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        } else {
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        float b = data.getBrightness();
        GlStateManager.color(b, b, b, 1.0F);

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
        buffer.pos(-size, -size, -100.0D).tex(uMin, vMin).endVertex();
        buffer.pos(size, -size, -100.0D).tex(uMax, vMin).endVertex();
        buffer.pos(size, size, -100.0D).tex(uMax, vMax).endVertex();
        buffer.pos(-size, size, -100.0D).tex(uMin, vMax).endVertex();
        tess.draw();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.popMatrix();
    }

    // Helper method for WorldProvider to check positions
    public SkyRenderData getSun() {
        for (SkyRenderData obj : celestialObjects) {
            // Assuming Sun is the celestial sphere object for now, or identify by texture/ID
            if (obj.getPositionType() == SkyRenderData.PositionType.CELESTIAL_SPHERE) return obj;
        }
        return null;
    }

    public SkyRenderData getObjectAtZenith() {
        for (SkyRenderData obj : celestialObjects) {
            if (obj.getPositionType() == SkyRenderData.PositionType.ZENITH) return obj;
        }
        return null;
    }
}
