package supersymmetry.common.guns.bullettypes;

import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import supersymmetry.api.guns.SuSyAmmoType;

public class IncendiaryBullet extends SuSyAmmoType {

    public IncendiaryBullet(double damageMultiplier, double penetrationMultiplier, String name) {
        super(damageMultiplier, penetrationMultiplier, 1.0, 1, name);
    }

    @Override
    public void onEntityHit(World worldIn, Entity hitEntity, Vec3d hitPos) {
        super.onEntityHit(worldIn, hitEntity, hitPos);
        hitEntity.setFire(10);
    }

    @Override
    public void onBlockHit(World worldIn, RayTraceResult rtr, BlockPos lastUncollidableBlock) {
        super.onBlockHit(worldIn, rtr, lastUncollidableBlock);
        if (worldIn.isAirBlock(lastUncollidableBlock)) {
            worldIn.setBlockState(lastUncollidableBlock, Blocks.FIRE.getDefaultState(), 11); // copied from flint and
                                                                                             // steel
        }
    }
}
