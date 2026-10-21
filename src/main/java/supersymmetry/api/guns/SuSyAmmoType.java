package supersymmetry.api.guns;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SuSyAmmoType {

    public final double damageMultiplier;
    public final double penetrationMultiplier;
    public final double lossOnPenetration;
    public final int numPellets;
    public final String name;

    public SuSyAmmoType(double damageMultiplier, double penetrationMultiplier, double lossOnPenetration, int numPellets,
                        String name) {
        this.damageMultiplier = damageMultiplier;
        this.penetrationMultiplier = penetrationMultiplier;
        this.lossOnPenetration = lossOnPenetration;
        this.numPellets = numPellets;
        this.name = name;
    }

    public void onEntityHit(World worldIn, Entity hitEntity, Vec3d hitPos) {}

    public void onBlockHit(World worldIn, RayTraceResult rtr, BlockPos lastUncollidableBlock) {}

    public void spawnParticles(World worldIn, Vec3d pos1, Vec3d pos2, int delay) {}
}
