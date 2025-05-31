package supersymmetry.client.renderer.sky;


import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IRenderHandler;
import org.lwjgl.opengl.GL11;
import supersymmetry.Supersymmetry;

import java.util.Random;

public class SkyRendererMoon extends IRenderHandler {

    public static ResourceLocation EARTH_TEXTURE = new ResourceLocation(Supersymmetry.MODID, "textures/sky/earth.png");
    private Random rand = new Random(45654);
    private int earthSize = 50;
    private int starHeight = 100;
    private int drawDist = 0;
    private float starSize = 0.25F;

    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        GlStateManager.pushMatrix();
        GlStateManager.disableFog();
        mc.getTextureManager().bindTexture(EARTH_TEXTURE);
        if (drawDist == 0) drawDist = mc.gameSettings.renderDistanceChunks * 16 * 4;
        BufferBuilder bb = Tessellator.getInstance().getBuffer();
        GlStateManager.disableTexture2D();
        bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < 500; ++i) {
            int posX = rand.nextInt(drawDist) - drawDist / 2;
            int posZ = rand.nextInt(drawDist) - drawDist / 2;
            bb.pos(posX / 2, starHeight, posZ / 2).color(1F, 1F, 1F, 1F).endVertex();
            bb.pos(posX / 2 + starSize, starHeight, posZ / 2).color(1F, 1F, 1F, 1F).endVertex();
            bb.pos(posX / 2 + starSize, starHeight, posZ / 2 + starSize).color(1F, 1F, 1F, 1F).endVertex();
            bb.pos(posX / 2, starHeight, posZ / 2 + starSize).color(1F, 1F, 1F, 1F).endVertex();
        }
        Tessellator.getInstance().draw();
        GlStateManager.enableTexture2D();
        // GlStateManager.rotate(world.getCelestialAngle(partialTicks) * 360, 1, 0, 0);
        bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        int distEarth = mc.gameSettings.renderDistanceChunks * 16;
        bb.pos(0, distEarth, 0).tex(0, 0).endVertex();
        bb.pos(earthSize / 2, distEarth, 0).tex(1, 0).endVertex();
        bb.pos(earthSize / 2, distEarth, earthSize / 2).tex(1, 1).endVertex();
        bb.pos(0, distEarth, earthSize / 2).tex(0, 1).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.enableFog();
        GlStateManager.popMatrix();
    }

}
