package supersymmetry.client.renderer.sky;


import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IRenderHandler;
import org.lwjgl.opengl.GL11;
import supersymmetry.Supersymmetry;

import java.util.Random;

public class SkyRendererMoon extends IRenderHandler {

    public static ResourceLocation EARTH_TEXTURE = new ResourceLocation(Supersymmetry.MODID, "textures/sky/earth.png");
    private static final ResourceLocation SUN_TEXTURES = new ResourceLocation("textures/environment/sun.png");

    private final Random rand = new Random(45654);
    private final int earthSize = 50;
    private final int starHeight = 100;
    private final float starSize = 0.25F;

    private final int starGLCallList;


    public SkyRendererMoon() {
        this.starGLCallList = GLAllocation.generateDisplayLists(3);
        GL11.glPushMatrix();
        GL11.glNewList(this.starGLCallList, GL11.GL_COMPILE);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        this.renderStars(buffer);
        Tessellator.getInstance().draw();
        GL11.glEndList();
        GL11.glPopMatrix();
    }

    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        GlStateManager.pushMatrix();
        GlStateManager.disableFog();
        GlStateManager.enableBlend();
        BufferBuilder bb = Tessellator.getInstance().getBuffer();

        GlStateManager.enableTexture2D();

        mc.getTextureManager().bindTexture(EARTH_TEXTURE);
        // GlStateManager.rotate(world.getCelestialAngle(partialTicks) * 360, 1, 0, 0);
        bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        int distEarth = mc.gameSettings.renderDistanceChunks * 16;
        bb.pos(0, distEarth, 0).tex(0, 0).endVertex();
        bb.pos(earthSize / 2, distEarth, 0).tex(1, 0).endVertex();
        bb.pos(earthSize / 2, distEarth, earthSize / 2).tex(1, 1).endVertex();
        bb.pos(0, distEarth, earthSize / 2).tex(0, 1).endVertex();
        Tessellator.getInstance().draw();

        // Sun render
        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(world.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);
        float sunSize = 30.0F;
        double distSun = mc.gameSettings.renderDistanceChunks * 32;
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        mc.getTextureManager().bindTexture(SUN_TEXTURES);
        bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        bb.pos(-sunSize, distSun, -sunSize).tex(0.0D, 0.0D).endVertex();
        bb.pos(sunSize, distSun, -sunSize).tex(1.0D, 0.0D).endVertex();
        bb.pos(sunSize, distSun, sunSize).tex(1.0D, 1.0D).endVertex();
        bb.pos(-sunSize, distSun, sunSize).tex(0.0D, 1.0D).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.popMatrix();

        GlStateManager.disableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        GL11.glCallList(this.starGLCallList);
        GlStateManager.depthMask(true);

        // End of sky render
        GlStateManager.popMatrix();
        GlStateManager.enableFog();
        GlStateManager.enableTexture2D();

    }

    private void renderStars(BufferBuilder bufferBuilderIn)
    {
        Random random = new Random(10843L);
        bufferBuilderIn.begin(7, DefaultVertexFormats.POSITION);

        for (int i = 0; i < 1500; ++i)
        {
            double d0 = random.nextFloat() * 2.0F - 1.0F;
            double d1 = random.nextFloat() * 2.0F - 1.0F;
            double d2 = random.nextFloat() * 2.0F - 1.0F;
            double d3 = 0.15F + random.nextFloat() * 0.1F;
            double d4 = d0 * d0 + d1 * d1 + d2 * d2;

            if (d4 < 1.0D && d4 > 0.01D)
            {
                d4 = 1.0D / Math.sqrt(d4);
                d0 = d0 * d4;
                d1 = d1 * d4;
                d2 = d2 * d4;
                double d5 = d0 * 100.0D;
                double d6 = d1 * 100.0D;
                double d7 = d2 * 100.0D;
                double d8 = Math.atan2(d0, d2);
                double d9 = Math.sin(d8);
                double d10 = Math.cos(d8);
                double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
                double d12 = Math.sin(d11);
                double d13 = Math.cos(d11);
                double d14 = random.nextDouble() * Math.PI * 2.0D;
                double d15 = Math.sin(d14);
                double d16 = Math.cos(d14);

                for (int j = 0; j < 4; ++j)
                {
                    double d18 = (double)((j & 2) - 1) * d3;
                    double d19 = (double)((j + 1 & 2) - 1) * d3;
                    double d21 = d18 * d16 - d19 * d15;
                    double d22 = d19 * d16 + d18 * d15;
                    double d23 = d21 * d12 + 0.0D * d13;
                    double d24 = 0.0D * d12 - d21 * d13;
                    double d25 = d24 * d9 - d22 * d10;
                    double d26 = d22 * d9 + d24 * d10;
                    bufferBuilderIn.pos(d5 + d25, d6 + d23, d7 + d26).endVertex();
                }
            }
        }
    }


}
