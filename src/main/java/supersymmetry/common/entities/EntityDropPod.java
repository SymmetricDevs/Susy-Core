package supersymmetry.common.entities;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
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

import gregtech.api.GTValues;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import supersymmetry.client.audio.MovingSoundDropPod;
import supersymmetry.client.renderer.particles.SusyParticleFlame;
import supersymmetry.client.renderer.particles.SusyParticleSmoke;

public class EntityDropPod extends EntityLiving implements IAnimatable {

    private static final DataParameter<Boolean> HAS_LANDED = EntityDataManager.<Boolean>createKey(EntityDropPod.class,
            DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> TIME_SINCE_LANDING = EntityDataManager
            .<Integer>createKey(EntityDropPod.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> TIME_SINCE_SPAWN = EntityDataManager
            .<Integer>createKey(EntityDropPod.class, DataSerializers.VARINT);

    private AnimationFactory factory = new AnimationFactory(this);

    @SideOnly(Side.CLIENT)
    private MovingSoundDropPod soundDropPod;

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
        this(worldIn, (float) pos.getX() - 0.5F, (float) pos.getY(), (float) pos.getZ() + 0.5);
    }

    public boolean canPlayerDismount() {
        return this.isDead || this.getTimeSinceLanding() >= 30;
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

    public boolean hasTakenOff() {
        return this.getTimeSinceLanding() > 200;
    }

    @SideOnly(Side.CLIENT)
    protected void spawnFlightParticles(boolean goingUp) {
        if (this.isDead || (goingUp && this.getTimeSinceLanding() > 500)) {
            return;
        }

        double offset = goingUp ? 0.2D : 0.5D;
        SusyParticleFlame flame1 = new SusyParticleFlame(
                this.world,
                this.posX + 0.8D,
                this.posY + 0.9D + offset,
                this.posZ + 0.2D,
                1.5 * (GTValues.RNG.nextFloat() + 0.2) * 0.08,
                -1.5,
                1.5 * (GTValues.RNG.nextFloat() - 0.5) * 0.08);
        SusyParticleFlame flame2 = new SusyParticleFlame(
                this.world,
                this.posX + 0.8D,
                this.posY + 0.9D + offset,
                this.posZ - 0.2D,
                1.5 * (GTValues.RNG.nextFloat() + 0.2) * 0.08,
                -1.5,
                1.5 * (GTValues.RNG.nextFloat() - 0.5) * 0.08);
        SusyParticleFlame flame3 = new SusyParticleFlame(
                this.world,
                this.posX - 0.8D,
                this.posY + 0.9D + offset,
                this.posZ + 0.2D,
                1.5 * (GTValues.RNG.nextFloat() - 1.2) * 0.08,
                -1.5,
                1.5 * (GTValues.RNG.nextFloat() - 0.5) * 0.08);
        SusyParticleFlame flame4 = new SusyParticleFlame(
                this.world,
                this.posX - 0.8D,
                this.posY + 0.9D + offset,
                this.posZ - 0.2D,
                1.5 * (GTValues.RNG.nextFloat() - 1.2) * 0.08,
                -1.5,
                1.5 * (GTValues.RNG.nextFloat() - 0.5) * 0.08);

        SusyParticleSmoke smoke1 = new SusyParticleSmoke(
                this.world,
                this.posX + 0.8D,
                this.posY + 0.9D + offset,
                this.posZ + 0.2D,
                1.5 * (GTValues.RNG.nextFloat() + 0.2) * 0.16,
                -1.5,
                1.5 * (GTValues.RNG.nextFloat() - 0.5) * 0.16);
        SusyParticleSmoke smoke2 = new SusyParticleSmoke(
                this.world,
                this.posX + 0.8D,
                this.posY + 0.9D + offset,
                this.posZ - 0.2D,
                1.5 * (GTValues.RNG.nextFloat() + 0.2) * 0.16,
                -1.5,
                1.5 * (GTValues.RNG.nextFloat() - 0.5) * 0.16);
        SusyParticleSmoke smoke3 = new SusyParticleSmoke(
                this.world,
                this.posX - 0.8D,
                this.posY + 0.9D + offset,
                this.posZ + 0.2D,
                1.5 * (GTValues.RNG.nextFloat() - 1.2) * 0.16,
                -1.5,
                1.5 * (GTValues.RNG.nextFloat() - 0.5) * 0.16);
        SusyParticleSmoke smoke4 = new SusyParticleSmoke(
                this.world,
                this.posX - 0.8D,
                this.posY + 0.9D + offset,
                this.posZ - 0.2D,
                1.5 * (GTValues.RNG.nextFloat() - 1.2) * 0.16,
                -1.5,
                1.5 * (GTValues.RNG.nextFloat() - 0.5) * 0.16);

        Minecraft.getMinecraft().effectRenderer.addEffect(smoke1);
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke2);
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke3);
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke4);

        Minecraft.getMinecraft().effectRenderer.addEffect(flame1);
        Minecraft.getMinecraft().effectRenderer.addEffect(flame2);
        Minecraft.getMinecraft().effectRenderer.addEffect(flame3);
        Minecraft.getMinecraft().effectRenderer.addEffect(flame4);
    }

    private void handleCollidedBlocks(boolean above) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                BlockPos pos = new BlockPos(this.posX + i, above ? this.posY + 2 : this.posY - 1, this.posZ + j);
                if (this.world.getBlockState(pos).getMaterial().isLiquid()) return;
                if (this.world.getBlockState(pos).getBlockHardness(this.world, pos) < 0.3) {
                    this.world.setBlockToAir(pos);
                } else if (above) {
                    this.explode();
                    this.world.removeEntityDangerously(this);
                    break;
                }
            }
        }
    }

    private void handleCollidedBlocks() {
        handleCollidedBlocks(false);
    }

    @Override
    public void onDeath(@NotNull DamageSource source) {
        super.onDeath(source);
        this.explode();
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
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(HAS_LANDED, false);
        this.dataManager.register(TIME_SINCE_LANDING, 0);
        this.dataManager.register(TIME_SINCE_SPAWN, 0);
    }

    @Override
    public void writeEntityToNBT(@NotNull NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("landed", this.hasLanded());
        compound.setInteger("time_since_landing", this.getTimeSinceLanding());
    }

    @Override
    public void readEntityFromNBT(@NotNull NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setLanded(compound.getBoolean("Landed"));
        this.setTimeSinceLanding(compound.getInteger("time_since_landing"));
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if (this.canPlayerDismount()) {
            for (Entity rider : this.getRecursivePassengers()) {
                rider.dismountRidingEntity();
            }
        }

        if (!world.isRemote) {
            if (!this.onGround && this.motionY < 0.0D) {
                this.motionY *= 0.9D;
            }

            if (!this.hasLanded()) {
                this.handleCollidedBlocks();
                this.getPassengers().forEach(e -> e.fallDistance = 0);
            }

            this.setLanded(this.hasLanded() || this.onGround);

            if (this.hasLanded()) {
                if (this.getTimeSinceLanding() == 0) {
                    int posXRounded = MathHelper.floor(this.posX);
                    int posYBeneath = MathHelper.floor(this.posY - 1.20000000298023224D);
                    int posZRounded = MathHelper.floor(this.posZ);
                    IBlockState blockBeneath = this.world
                            .getBlockState(new BlockPos(posXRounded, posYBeneath, posZRounded));

                    if (blockBeneath.getMaterial() != Material.AIR) {
                        SoundType soundType = blockBeneath.getBlock().getSoundType(blockBeneath, world,
                                new BlockPos(posXRounded, posYBeneath, posZRounded), this);
                        this.playSound(soundType.getBreakSound(), soundType.getVolume() * 3.0F,
                                soundType.getPitch() * 0.2F);
                    }
                }
                this.setTimeSinceLanding(this.getTimeSinceLanding() + 1);
                if (this.getTimeSinceLanding() > 1000) {
                    this.setDead();
                }
            }

            if (this.hasTakenOff()) {
                if (this.motionY < 10.D) {
                    if (this.motionY < 1.D) {
                        this.motionY += 0.1;
                    }
                    this.motionY *= 1.1D;
                }
                this.handleCollidedBlocks(true);
                this.isDead = this.posY > 300;
            }
        } else {
            if (!this.hasLanded()) {
                this.spawnFlightParticles(false);
            }
            if (this.hasTakenOff()) {
                this.spawnFlightParticles(true);
            }
        }

        this.dataManager.set(TIME_SINCE_SPAWN, this.dataManager.get(TIME_SINCE_SPAWN) + 1);

        if (world.isRemote && this.soundDropPod != null) {
            if (!this.hasLanded() || this.hasTakenOff()) {
                soundDropPod.startPlaying();
            } else {
                soundDropPod.stopPlaying();
            }
        }
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
        return this.getTimeSinceLanding() > 1000;
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
    public void knockBack(@NotNull Entity entityIn, float strength, double xRatio, double zRatio) {}

    @Override
    public void setAir(int air) {
        super.setAir(300);
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (this.getTimeSinceLanding() > 0 && this.getTimeSinceLanding() < 140) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.drop_pod.complete",
                    ILoopType.EDefaultLoopTypes.PLAY_ONCE));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData
                .addAnimationController(new AnimationController<EntityDropPod>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Override
    protected void addPassenger(Entity passenger) {
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
            setupDropPodSound();
        }
    }

    @SideOnly(Side.CLIENT)
    public void setupDropPodSound() {
        this.soundDropPod = new MovingSoundDropPod(this);
        Minecraft.getMinecraft().getSoundHandler().playSound(this.soundDropPod);
    }
}
