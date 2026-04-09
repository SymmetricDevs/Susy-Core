package supersymmetry.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.world.World;

public class ParticleFlareSmoke extends Particle {

    public ParticleFlareSmoke(World worldIn, double x, double y, double z, float R, float G, float B) {
        super(worldIn, x, y, z);

        this.motionX = (rand.nextDouble() - 0.5) * 0.01;
        this.motionY = 0.4;
        this.motionZ = (rand.nextDouble() - 0.5) * 0.01;

        this.particleRed = R;
        this.particleGreen = G;
        this.particleBlue = B;

        this.particleScale = 10f;
        this.multipleParticleScaleBy(3.0f); // MUCH larger final size

        this.particleMaxAge = 100;

        this.canCollide = false;

    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        }

        // Move upward
        this.motionY += 0.0005; // slight acceleration
        this.move(this.motionX, this.motionY, this.motionZ);

        // Fade out slowly
        this.particleAlpha = 1.0f - ((float) this.particleAge / this.particleMaxAge);
    }

    @Override
    public void renderParticle(BufferBuilder buffer, net.minecraft.entity.Entity entityIn,
                               float partialTicks, float rotationX, float rotationZ,
                               float rotationYZ, float rotationXY, float rotationXZ) {
        super.renderParticle(buffer, entityIn, partialTicks,
                rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
    }

    @Override
    public boolean shouldDisableDepth() {
        return true;
    }
}
