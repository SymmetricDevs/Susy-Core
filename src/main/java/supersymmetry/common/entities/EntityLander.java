package supersymmetry.common.entities;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import supersymmetry.client.audio.MovingSoundLander;

public class EntityLander extends EntityLiving implements IAnimatable {

    private static final DataParameter<Boolean> HAS_LANDED = EntityDataManager.<Boolean>createKey(EntityLander.class,
            DataSerializers.BOOLEAN);

    private AnimationFactory factory = new AnimationFactory(this);
    @SideOnly(Side.CLIENT)
    private MovingSoundLander soundLander;

    public EntityLander(World worldIn) {
        super(worldIn);
        this.deathTime = 0;
    }

    public EntityLander(World worldIn, double x, double y, double z) {
        this(worldIn);
        this.setLocationAndAngles(x, y, z, 0.F, 0.F);
        this.setEntityBoundingBox(new AxisAlignedBB(x - 2.5, y, z - 2.5, x + 2.5, y + 4, z + 2.5));
    }

    public EntityLander(World worldIn, BlockPos pos) {
        this(worldIn, (float) pos.getX() - 0.5F, (float) pos.getY(), (float) pos.getZ() + 0.5);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(HAS_LANDED, false);
    }

    @Override
    public void writeEntityToNBT(@NotNull NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("landed", this.hasLanded());
    }

    @Override
    public void readEntityFromNBT(@NotNull NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setLanded(compound.getBoolean("Landed"));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
    }

    public boolean hasLanded() {
        return this.dataManager.get(HAS_LANDED);
    }

    public void setLanded(boolean landed) {
        this.dataManager.set(HAS_LANDED, landed);
    }

    public boolean canPlayerDismount() {
        return this.isDead || this.hasLanded();
    }

    private void explode() {
        int explosionStrength = 1;
        if (getRidingEntity() != null && getRidingEntity() instanceof EntityPlayer) {
            explosionStrength = 6;
        }
        this.world.newExplosion(this, this.posX, this.posY, this.posZ, explosionStrength, false, false);
        this.setDead();
    }

    @Override
    public void onDeath(@NotNull DamageSource source) {
        super.onDeath(source);
        this.explode();
    }

    @Override
    protected void removePassenger(@NotNull Entity passenger) {
        if (this.canPlayerDismount()) {
            super.removePassenger(passenger);
            if (passenger instanceof EntityLiving living) {
                living.setNoAI(false);
            }
        }
    }

    @Override
    public void updatePassenger(@NotNull Entity passenger) {
        super.updatePassenger(passenger);
        float xOffset = MathHelper.sin(this.renderYawOffset * 0.1F);
        float zOffset = MathHelper.cos(this.renderYawOffset * 0.1F);
        passenger.setPosition(this.posX + (double) (0.1F * xOffset),
                this.posY + (double) (this.height * 0.2F) + passenger.getYOffset() + 0.0D,
                this.posZ - (double) (0.1F * zOffset));

        if (passenger instanceof EntityLivingBase) {
            ((EntityLivingBase) passenger).renderYawOffset = this.renderYawOffset;
        }
    }

    @Override
    public boolean shouldDismountInWater(@NotNull Entity rider) {
        return false;
    }

    @Override
    public boolean handleWaterMovement() {
        return isInWater();
    }

    @Override
    public void fall(float distance, float damageMultiplier) {}

    @Override
    protected boolean canDespawn() {
        return this.posY > 1000;
    }

    @Override
    public boolean canBeLeashedTo(@NotNull EntityPlayer player) {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean canBeHitWithPotion() {
        return false;
    }

    @Override
    protected void addPassenger(@NotNull Entity passenger) {
        if (this.getPassengers().isEmpty()) {
            super.addPassenger(passenger);
            if (passenger instanceof EntityLiving living) {
                living.setNoAI(true);
            }
        }
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (this.world.isRemote) {
            setupLanderSound();
        }
    }

    @SideOnly(Side.CLIENT)
    public void setupLanderSound() {
        this.soundLander = new MovingSoundLander(this);
        Minecraft.getMinecraft().getSoundHandler().playSound(this.soundLander);
    }

    @Override
    public void knockBack(@NotNull Entity entityIn, float strength, double xRatio, double zRatio) {}

    @Override
    public void setAir(int air) {
        super.setAir(300);
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (this.hasLanded()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.ladder.extend",
                    ILoopType.EDefaultLoopTypes.PLAY_ONCE));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData
                .addAnimationController(new AnimationController<EntityLander>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
