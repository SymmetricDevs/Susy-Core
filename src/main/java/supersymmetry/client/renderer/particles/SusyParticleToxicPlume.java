package supersymmetry.client.renderer.particles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import supersymmetry.Supersymmetry;

@SideOnly(Side.CLIENT)
public class SusyParticleToxicPlume extends Particle {

    private static final ResourceLocation PLUME_SPRITE = new ResourceLocation(Supersymmetry.MODID, "particle/plume");

    public SusyParticleToxicPlume(World world, double x, double y, double z) {
        super(world, x, y, z);

        this.motionX = 0.0;
        this.motionY = 0.1;
        this.motionZ = 0.0;

        this.particleRed   = 0.0f;
        this.particleGreen = 0.75f;
        this.particleBlue  = 0.0f;
        this.particleAlpha = 0.9f;

        this.particleScale = 30.0f;

        this.particleMaxAge = 500;
        this.canCollide = false;

        this.setParticleTexture(
                Minecraft.getMinecraft().getTextureMapBlocks()
                        .getAtlasSprite(PLUME_SPRITE.toString())
        );

    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        }

        this.move(this.motionX, this.motionY, this.motionZ);

        float lifeFraction = (float) this.particleAge / this.particleMaxAge;
        if (lifeFraction > 0.8f) {
            this.particleAlpha = 0.9f * (1.0f - ((lifeFraction - 0.8f) / 0.2f));
        }
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

    @Override
    public int getFXLayer() {
        return 1;
    }
}
