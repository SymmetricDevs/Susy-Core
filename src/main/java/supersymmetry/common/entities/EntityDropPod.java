package supersymmetry.common.entities;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class EntityDropPod extends EntityLiving implements IAnimatable {


    public EntityDropPod(World worldIn) {
        super(worldIn);
    }

    public EntityDropPod(World worldIn, double x, double y, double z) {
        super(worldIn);
        this.setLocationAndAngles(x, y, z, 0.F, 0.F);
        this.setEntityBoundingBox(new AxisAlignedBB(x - 0.25, y, z - 0.25, x + 0.25, y + 2, z + 0.25));
    }

    public EntityDropPod(World worldIn, BlockPos pos) {
        this(worldIn, (float)pos.getX() - 0.5F, (float)pos.getY(), (float)pos.getZ() + 0.5);
    }

    @Override
    public void registerControllers(AnimationData animationData) {

    }

    @Override
    public AnimationFactory getFactory() {
        return null;
    }
}
