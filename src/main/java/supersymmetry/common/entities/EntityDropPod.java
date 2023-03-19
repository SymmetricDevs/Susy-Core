package supersymmetry.common.entities;

import jdk.nashorn.internal.ir.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class EntityDropPod extends EntityLiving implements IAnimatable {

    private static final DataParameter<Boolean> HAS_LANDED = EntityDataManager.<Boolean>createKey(EntityDropPod.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> TIME_SINCE_LANDING = EntityDataManager.<Integer>createKey(EntityDropPod.class, DataSerializers.VARINT);

    private AnimationFactory factory = new AnimationFactory(this);

    public EntityDropPod(World worldIn) {
        super(worldIn);
        this.deathTime = 0;
    }

    public EntityDropPod(World worldIn, double x, double y, double z) {
        this(worldIn);
        this.setLocationAndAngles(x, y, z, 0.F, 0.F);
        this.setEntityBoundingBox(new AxisAlignedBB(x - 0.5, y, z - 0.5, x + 0.5, y + 2, z + 0.5));
    }

    public EntityDropPod(World worldIn, BlockPos pos) {
        this(worldIn, (float)pos.getX() - 0.5F, (float)pos.getY(), (float)pos.getZ() + 0.5);
    }

    public boolean canPlayerDismount() {
        return this.isDead || this.getTimeSinceLanding() >= 60;
    }

    public boolean hasLanded() {
        return this.dataManager.get(HAS_LANDED);
    }

    public void setLanded(boolean landed) {
        this.dataManager.set(HAS_LANDED, landed);
    }

    public int getTimeSinceLanding() {
        return this.dataManager.get(TIME_SINCE_LANDING);
    }

    private void setTimeSinceLanding(int timeSinceLanding) {
        this.dataManager.set(TIME_SINCE_LANDING, timeSinceLanding);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(HAS_LANDED, false);
        this.dataManager.register(TIME_SINCE_LANDING, 0);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("landed", this.hasLanded());
        compound.setInteger("time_since_landing", this.getTimeSinceLanding());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setLanded(compound.getBoolean("Landed"));
        this.setTimeSinceLanding(compound.getInteger("time_since_landing"));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if(!this.onGround && this.motionY < 0.0D) {
            this.motionY *= 0.9D;
        }

        if(this.canPlayerDismount()) {
            for(Entity rider : this.getRecursivePassengers()) {
                rider.dismountRidingEntity();
            }
        }

        if(!world.isRemote) {
            this.setLanded(this.hasLanded() || this.onGround);

            if (this.motionY <= 0.01D) {
                BlockPos pos = new BlockPos(this.posX, this.posY - 1, this.posZ);
                if (this.world.getBlockState(pos).getBlockHardness(this.world, pos) < 0.3) {
                    this.world.setBlockToAir(pos);
                }
            }

            if (this.hasLanded()) {
                this.setTimeSinceLanding(this.getTimeSinceLanding() + 1);
            }
        }
    }

    @Override
    protected void removePassenger(Entity passenger) {
        if (this.canPlayerDismount()) {
            super.removePassenger(passenger);
        }
    }

    @Override
    public void updatePassenger(Entity passenger) {
        super.updatePassenger(passenger);
        float f = MathHelper.sin(this.renderYawOffset * 0.017453292F);
        float f1 = MathHelper.cos(this.renderYawOffset * 0.017453292F);
        float f2 = 0.1F;
        float f3 = 0.0F;
        passenger.setPosition(this.posX + (double)(0.1F * f), this.posY + (double)(this.height * 0.2F) + passenger.getYOffset() + 0.0D, this.posZ - (double)(0.1F * f1));

        if (passenger instanceof EntityLivingBase) {
            ((EntityLivingBase)passenger).renderYawOffset = this.renderYawOffset;
        }
    }

    @Override
    public void fall(float distance, float damageMultiplier) {

    }

    @Override
    protected boolean canDespawn() {
        return false;
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
