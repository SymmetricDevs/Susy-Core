package supersymmetry.client.renderer.particles;

import net.minecraft.client.particle.ParticleSmokeNormal;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SusyParticleDust extends ParticleSmokeNormal{

    public SusyParticleDust(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double p_i46348_8_, double p_i46348_10_, double p_i46348_12_,float scale, float maxAge, int color) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, p_i46348_8_, p_i46348_10_, p_i46348_12_, maxAge);
        this.particleRed = ((color >> 16) & 0xFF) / 255f;
        this.particleGreen = ((color >> 8) & 0xFF) / 255f;
        this.particleBlue = (color & 0xFF) / 255f;
        this.smokeParticleScale = (this.rand.nextFloat() * 0.5F + 0.5F) * 1.5F * scale;
        this.particleMaxAge = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
        this.particleMaxAge = (int)((float)this.particleMaxAge * maxAge);
        this.canCollide = true;
        this.particleGravity = .5F;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge)
        {
            this.setExpired();
        }

        this.setParticleTextureIndex(7 - this.particleAge * 8 / this.particleMaxAge);
        this.motionY -= 0.04D * (double)this.particleGravity;

        this.move(this.motionX, this.motionY, this.motionZ);
        this.motionY *= 0.9800000190734863D;

    }
}
