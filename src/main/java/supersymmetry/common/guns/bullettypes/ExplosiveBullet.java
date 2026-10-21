package supersymmetry.common.guns.bullettypes;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import supersymmetry.api.guns.SuSyAmmoType;
import supersymmetry.common.entities.EntityExplosion;

public class ExplosiveBullet extends SuSyAmmoType {

    public ExplosiveBullet(double damageMultiplier, double penetrationMultiplier, String name) {
        super(damageMultiplier, penetrationMultiplier, 1.0, 1, name);
    }

    @Override
    public void onEntityHit(World worldIn, Entity hitEntity, Vec3d hitPos) {
        super.onEntityHit(worldIn, hitEntity, hitPos);
        EntityExplosion explosion = new EntityExplosion(worldIn, hitPos.x, hitPos.y, hitPos.z, 2);
        worldIn.spawnEntity(explosion);
    }

    @Override
    public void onBlockHit(World worldIn, RayTraceResult rtr, BlockPos lastUncollidableBlock) {
        super.onBlockHit(worldIn, rtr, lastUncollidableBlock);
        EntityExplosion explosion = new EntityExplosion(worldIn, rtr.hitVec.x, rtr.hitVec.y, rtr.hitVec.z, 2);
        worldIn.spawnEntity(explosion);
    }
}
