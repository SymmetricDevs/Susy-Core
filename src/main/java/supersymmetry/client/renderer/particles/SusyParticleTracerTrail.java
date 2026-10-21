package supersymmetry.client.renderer.particles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import supersymmetry.Supersymmetry;

public class SusyParticleTracerTrail extends Particle {

    private static final ResourceLocation TRACER_SPRITE = new ResourceLocation(Supersymmetry.MODID, "particle/tracer");
    private static final int realMaxAge = 2;

    public SusyParticleTracerTrail(World worldIn, double x, double y, double z, float R, float G, float B, int delay) {
        super(worldIn, x, y, z);

        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;

        this.particleRed = R;
        this.particleGreen = G;
        this.particleBlue = B;
        this.particleAngle = (float) Math.PI / 2;
        this.particleScale = 0.6f / (this.particleAge - (particleMaxAge - realMaxAge));
        this.particleAlpha = 0;

        this.particleMaxAge = realMaxAge + delay;

        this.canCollide = false;

        this.setParticleTexture(
                Minecraft.getMinecraft().getTextureMapBlocks()
                        .getAtlasSprite(TRACER_SPRITE.toString()));
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        }

        // don't show up until the delay is over
        if (this.particleAge < particleMaxAge - realMaxAge) {
            this.particleAlpha = 0;
        } else {
            // then fade out
            this.particleAlpha = (float) Math
                    .sqrt((-((double) (this.particleAge - (particleMaxAge - realMaxAge)) / realMaxAge) + 1));
        }
    }

    @Override
    public int getFXLayer() {
        return 1;
    }

    @Override
    public void renderParticle(BufferBuilder buffer, net.minecraft.entity.Entity entityIn, float partialTicks,
                               float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
    }

    @Override
    public boolean shouldDisableDepth() {
        return true;
    }
}
