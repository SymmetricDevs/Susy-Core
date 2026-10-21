package supersymmetry.common.guns.bullettypes;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import supersymmetry.api.guns.SuSyAmmoType;
import supersymmetry.client.renderer.particles.SusyParticleTracerTrail;

public class TracerBullet extends SuSyAmmoType {

    public float r;
    public float g;
    public float b;

    public final double particleDistance = 0.05;

    public TracerBullet(double damageMultiplier, double penetrationMultiplier, double lossOnPenetration, float r,
                        float g, float b, String name) {
        super(damageMultiplier, penetrationMultiplier, lossOnPenetration, 1, name);
        this.r = r;
        this.g = g;
        this.b = b;
    }

    /// Spawns particles along a line between pos1 and pos2 every particleDistance blocks
    @Override
    public void spawnParticles(World worldIn, Vec3d pos1, Vec3d pos2, int delay) {
        Vec3d currentPos = pos1;
        double motionX = (pos2.x - pos1.x) * (particleDistance / pos1.distanceTo(pos2));
        double motionY = (pos2.y - pos1.y) * (particleDistance / pos1.distanceTo(pos2));
        double motionZ = (pos2.z - pos1.z) * (particleDistance / pos1.distanceTo(pos2));
        for (int i = 0; i < pos1.distanceTo(pos2) / particleDistance; i++) {
            SusyParticleTracerTrail tracer = new SusyParticleTracerTrail(worldIn, currentPos.x, currentPos.y,
                    currentPos.z, r, g, b, delay);
            Minecraft.getMinecraft().effectRenderer.addEffect(tracer);
            currentPos = new Vec3d(currentPos.x + motionX, currentPos.y + motionY, currentPos.z + motionZ);
        }
    }
}
