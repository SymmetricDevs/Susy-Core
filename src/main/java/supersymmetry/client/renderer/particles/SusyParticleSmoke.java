package supersymmetry.client.renderer.particles;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleSmokeNormal;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

@SideOnly(Side.CLIENT)
public class SusyParticleSmoke extends ParticleSmokeNormal {

    public SusyParticleSmoke(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn,
                             double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, 3.F);
    }

    @SideOnly(Side.CLIENT)
    public static class Factory implements IParticleFactory {

        public Particle createParticle(int particleID, @NotNull World worldIn, double xCoordIn, double yCoordIn,
                                       double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn,
                                       int @NotNull... parameters) {
            return new SusyParticleSmoke(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        }
    }
}
