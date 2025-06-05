package supersymmetry.client.renderer.particles;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;

/**
 * Froth bubble particle mostly copied from the standard minecraft bubble particle.
 * Allows for using custom colors and doesn't despawn outside of water allowing it to be used in the Froth Flotation Tank
 * @author h3tR
 */


public class SusyParticleFrothBubble extends Particle {

    public SusyParticleFrothBubble(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int color)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        this.particleRed = ((color >> 16) & 0xFF) / 255f;
        this.particleGreen = ((color >> 8) & 0xFF) / 255f;
        this.particleBlue = (color & 0xFF) / 255f;
        this.setParticleTextureIndex(32); //Bubble texture
        this.setSize(0.02F, 0.02F);
        this.particleScale *= this.rand.nextFloat() * 0.6F + 0.2F;
        this.motionX = xSpeedIn * 0.20000000298023224D + (Math.random() * 2.0D - 1.0D) * 0.019999999552965164D;
        this.motionY = ySpeedIn * 0.20000000298023224D + (Math.random() * 2.0D - 1.0D) * 0.019999999552965164D;
        this.motionZ = zSpeedIn * 0.20000000298023224D + (Math.random() * 2.0D - 1.0D) * 0.019999999552965164D;
        this.particleMaxAge = (int)(4.0D / (Math.random() * 0.8D + 0.2D));
    }


    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.motionY += 0.002D;
        this.move(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.8500000238418579D;
        this.motionY *= 0.8500000238418579D;
        this.motionZ *= 0.8500000238418579D;

        if (this.particleMaxAge-- <= 0)
        {
            this.setExpired();
        }
    }

}

