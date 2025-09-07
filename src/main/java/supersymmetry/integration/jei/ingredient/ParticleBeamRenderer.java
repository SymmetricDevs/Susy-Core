package supersymmetry.integration.jei.ingredient;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.text.TextFormatting;
import supersymmetry.api.particle.Particle;
import supersymmetry.api.particle.ParticleBeam;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ParticleBeamRenderer implements IIngredientRenderer<ParticleBeam> {

    private static final int TEXT_WIDTH = 16;
    private static final int TEXT_HEIGHT = 16;

    private final double energy;
    private final double bunchSpacing;
    private final double bunchLength;
    private final int nBunches;
    private final int nParticlesPerBunch;
    private final double emittance;
    private final double beamSize;

    @Nullable
    private final IDrawable overlay;
    private final int width;
    private final int height;

    public ParticleBeamRenderer() {
        this(0, 0, 0, 0, 0, 0, 0, null, TEXT_WIDTH, TEXT_HEIGHT);
    }

    public ParticleBeamRenderer(double energy, double bunchSpacing, double bunchLength, int nBunches, int nParticlesPerBunch, double emittance, double beamSize, @Nullable IDrawable overlay, int width, int height) {
        this.energy = energy;
        this.bunchSpacing = bunchSpacing;
        this.bunchLength = bunchLength;
        this.nBunches = nBunches;
        this.nParticlesPerBunch = nParticlesPerBunch;
        this.emittance = emittance;
        this.beamSize = beamSize;
        this.overlay = overlay;
        this.width = width;
        this.height = height;
    }

    @Override
    public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable ParticleBeam ingredient) {
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();

        drawParticle(minecraft, xPosition, yPosition, ingredient);

        GlStateManager.color(1, 1, 1, 1);

        if (overlay != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0, 200);
            overlay.draw(minecraft, xPosition, yPosition);
            GlStateManager.popMatrix();
        }

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
    }

    private void drawParticle(Minecraft minecraft, final int posX, final int posY, @Nullable ParticleBeam particleBeam) {
        if (particleBeam == null) return;
        Particle particle = particleBeam.getParticle();
        if (particle == null) return;
        minecraft.renderEngine.bindTexture(particleBeam.getParticle().getTexture());
        double zLevel = 100;
        double width = 16;
        double uMin = 0;
        double uMax = 1;
        double vMin = 0;
        double vMax = 1;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferBuilder.pos(posX, posY + width, zLevel).tex(uMin, vMax).endVertex();
        bufferBuilder.pos(posX + width, posY + width, zLevel).tex(uMax, vMax).endVertex();
        bufferBuilder.pos(posX + width, posY, zLevel).tex(uMax, vMin).endVertex();
        bufferBuilder.pos(posX, posY + width, zLevel).tex(uMin, vMin).endVertex();
        tessellator.draw();
    }

    @Override
    public List<String> getTooltip(Minecraft minecraft, ParticleBeam ingredient, ITooltipFlag tooltipFlag) {
        List<String> list = new ArrayList<>();
        list.add(TextFormatting.GRAY + "This is the big test for tooltip rendering, amogus");
        return list;
    }
}
