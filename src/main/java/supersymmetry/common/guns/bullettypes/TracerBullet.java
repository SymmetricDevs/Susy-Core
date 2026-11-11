package supersymmetry.common.guns.bullettypes;

import gregtech.client.particle.GTParticleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import supersymmetry.api.guns.SuSyAmmoType;
import supersymmetry.client.renderer.particles.SusyParticleBlueTracer;
import supersymmetry.client.renderer.particles.SusyParticleGreenTracer;
import supersymmetry.client.renderer.particles.SusyParticleRedTracer;

public class TracerBullet extends SuSyAmmoType {

    public float r;
    public float g;
    public float b;

    public static final double particleDistance = 0.05;

    public TracerBullet(double damageMultiplier, double penetrationMultiplier, double lossOnPenetration, float r,
                        float g, float b, String name) {
        super(damageMultiplier, penetrationMultiplier, lossOnPenetration, 1, name);
        this.r = r;
        this.g = g;
        this.b = b;
    }

    /// Spawns the tracer trail along the line between pos1 and pos2
    @Override
    public void spawnParticles(World worldIn, Vec3d pos1, Vec3d pos2, int delay) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.addScheduledTask(() -> {
            if (mc.world != null) {
                spawnTracerParticles(mc.world, pos1, pos2, delay, r, g, b);
            }
        });
    }

    public static void spawnTracerParticles(World world, Vec3d pos1, Vec3d pos2, int delay, float r, float g, float b) {
        double length = pos1.distanceTo(pos2);
        if (length < particleDistance) {
            return;
        }

        int count = (int) (length / particleDistance);
        double progressStep = particleDistance / length;
        for (int i = 0; i < count; i++) {
            double progress = progressStep * i;
            double x = pos1.x + (pos2.x - pos1.x) * progress;
            double y = pos1.y + (pos2.y - pos1.y) * progress;
            double z = pos1.z + (pos2.z - pos1.z) * progress;
            if (r > 0.9) {
                GTParticleManager.INSTANCE.addEffect(new SusyParticleRedTracer(world, x, y, z, 0, 0, 0, delay));
            } else if (g > 0.9) {
                GTParticleManager.INSTANCE.addEffect(new SusyParticleGreenTracer(world, x, y, z, 0, 0, 0, delay));
            } else {
                GTParticleManager.INSTANCE.addEffect(new SusyParticleBlueTracer(world, x, y, z, 0, 0, 0, delay));
            }

        }
    }
}
