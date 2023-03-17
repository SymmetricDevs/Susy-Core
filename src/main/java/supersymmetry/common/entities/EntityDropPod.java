package supersymmetry.common.entities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class EntityDropPod extends EntityLiving implements IAnimatable {

    private AnimationFactory factory = new AnimationFactory(this);

    public EntityDropPod(World worldIn) {
        super(worldIn);
        this.deathTime = 0;
    }

    public EntityDropPod(World worldIn, double x, double y, double z) {
        super(worldIn);
        this.setLocationAndAngles(x, y, z, 0.F, 0.F);
        this.setEntityBoundingBox(new AxisAlignedBB(x - 0.5, y, z - 0.5, x + 0.5, y + 2, z + 0.5));
        this.deathTime = 0;
    }

    public EntityDropPod(World worldIn, BlockPos pos) {
        this(worldIn, (float)pos.getX() - 0.5F, (float)pos.getY(), (float)pos.getZ() + 0.5);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        this.motionY = MathHelper.clamp(this.motionY, -1., 100.);
        this.fallDistance = 2.F;
    }

    @Override
    public boolean canBeLeashedTo(EntityPlayer player) {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public boolean canBeHitWithPotion() {
        return false;
    }

    @Override
    public void knockBack(Entity entityIn, float strength, double xRatio, double zRatio) {

    }

    @Override
    public void setAir(int air) {
        super.setAir(300);
    }

    @Override
    public void registerControllers(AnimationData animationData) {

    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
