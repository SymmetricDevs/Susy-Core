package supersymmetry.common.entities;

import gregtech.api.util.GTUtility;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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
import supersymmetry.client.audio.MovingSoundDrone;
import supersymmetry.common.metatileentities.multi.electric.MetaTileEntityDronePad;

public class EntityDrone extends EntityLiving implements IAnimatable {

    private static final DataParameter<Integer> AGE = EntityDataManager.<Integer>createKey(EntityDrone.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> PAD_ALTITUDE = EntityDataManager.createKey(EntityDrone.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> DESCENDING_MODE = EntityDataManager.createKey(EntityDrone.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> HAS_LANDED = EntityDataManager.createKey(EntityDrone.class, DataSerializers.BOOLEAN);

    private static final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

    private final AnimationFactory factory = new AnimationFactory(this);

    @SideOnly(Side.CLIENT)
    private MovingSoundDrone soundDrone;
    private BlockPos padPos;

    public EntityDrone(World worldIn) {
        super(worldIn);
        this.setSize(1.F, 1.F);
        rideCooldown = -1;
    }

    public EntityDrone(World worldIn, double x, double y, double z) {
        super(worldIn);
        this.setLocationAndAngles(x, y, z, 0.F, 0.F);
        this.setSize(1.F, 1.F);
        rideCooldown = -1;
        this.setEntityBoundingBox(new AxisAlignedBB(x-1, y+0, z-1, x+1, y+1, z+1));
    }

    public EntityDrone(World worldIn, BlockPos pos) {
        this(worldIn, pos.getX() + 0.5F, pos.getZ() + 0.5F, pos.getZ() + 0.5F);
    }

    public EntityDrone withPadPos(BlockPos pos) {
        this.padPos = pos;
        return this;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(AGE, 0);
        this.dataManager.register(PAD_ALTITUDE, 0);
        this.dataManager.register(DESCENDING_MODE, false);
        this.dataManager.register(HAS_LANDED, false);
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (padPos != null) {
            MetaTileEntityDronePad pad = (MetaTileEntityDronePad) GTUtility.getMetaTileEntity(world, padPos);
            this.padPos = null;
            if (pad != null) {
                pad.setDrone(null);
            }
        }
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (this.world.isRemote) {
            setupDroneSound();
        }
        if (padPos != null) {
            MetaTileEntityDronePad pad = (MetaTileEntityDronePad) GTUtility.getMetaTileEntity(world, padPos);
            if (pad != null) {
                pad.setDrone(this);
            }
        }
    }

    public void setRotationFromFacing(EnumFacing facing) {
        switch (facing) {
            case EAST -> setRotation(90.F, 0.F);
            case SOUTH -> setRotation(180.F, 0.F);
            case WEST -> setRotation(270.F, 0.F);
            default -> setRotation(0.F, 0.F);
        }
    }

    @SideOnly(Side.CLIENT)
    public void setupDroneSound() {
        this.soundDrone = new MovingSoundDrone(this);
        Minecraft.getMinecraft().getSoundHandler().playSound(this.soundDrone);
    }

    @Override
    public void fall(float distance, float damageMultiplier) {

    }

    @Override
    protected boolean canDespawn() {
        return false;
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
    public void knockBack(@NotNull Entity entityIn, float strength, double xRatio, double zRatio) {

    }

    private void explode() {
        this.world.newExplosion(this, this.posX, this.posY, this.posZ, 2, true, true);
        this.damageEntity(DamageSource.FLY_INTO_WALL, 100);
        this.isDead = true;
    }

    public void setDescendingMode() {
        this.dataManager.set(DESCENDING_MODE, true);
    }

    public void setPadAltitude(int y) {
        this.dataManager.set(PAD_ALTITUDE, y);
    }

    @Override
    public void setAir(int air) {
        super.setAir(300);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        int age = this.dataManager.get(AGE);
        int padAltitude = this.dataManager.get(PAD_ALTITUDE);
        boolean descendingMode = this.dataManager.get(DESCENDING_MODE);

        if (!world.isRemote) {

            if (age >= 55 && !descendingMode) {

                this.motionY += 0.125;

                if(age >= 90 && this.isCollidingWithBlocks()) {
                    this.explode();
                }


            } else if (descendingMode) {
                this.motionY = -1.D * Math.min((this.posY - padAltitude) * 0.125, 2.);
                if (this.isCollidingWithBlocks()) {
                    if (this.posY > padAltitude + 2 || this.posY < padAltitude - 2) {
                        this.explode();
                    } else if (!this.dataManager.get(HAS_LANDED)) {
                        this.setLanded();
                    }
                }
            }

            this.isDead |= this.posY > 300 || (age > 255 && !descendingMode);

            if (this.isDead) {
                this.motionY = 0.;
            }

        } else if (this.soundDrone != null) {

            if (this.firstUpdate) {
                this.soundDrone.startPlaying();
            }

            if (this.dataManager.get(HAS_LANDED)) {
                this.soundDrone.stopPlaying();
            }

        }

        this.dataManager.set(AGE, age + 1);
    }

    public boolean isCollidingWithBlocks() {
        return this.world.getBlockState(mutableBlockPos.setPos(this.posX, this.posY + 1, this.posZ)) != Blocks.AIR.getDefaultState()
                || this.world.getBlockState(mutableBlockPos.setPos(this.posX, this.posY - 1, this.posZ)) != Blocks.AIR.getDefaultState();
    }

    public boolean reachedSky() {
        return this.posY > 256 && !this.isDead;
    }

    public void setLanded() {
        this.dataManager.set(HAS_LANDED, true);
    }

    @Override
    public void writeEntityToNBT(@NotNull NBTTagCompound compound) {
        super.writeEntityToNBT(compound);;
        compound.setInteger("Age", this.dataManager.get(AGE));
        compound.setInteger("PadAltitude", this.dataManager.get(PAD_ALTITUDE));
        compound.setBoolean("DescendingMode", this.dataManager.get(DESCENDING_MODE));
        compound.setBoolean("HasLanded", this.dataManager.get(HAS_LANDED));
        compound.setLong("PadPos", padPos.toLong());
    }

    @Override
    public void readEntityFromNBT(@NotNull NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.dataManager.set(AGE, compound.getInteger("Age"));
        this.dataManager.set(PAD_ALTITUDE, compound.getInteger("PadAltitude"));
        this.dataManager.set(DESCENDING_MODE, compound.getBoolean("DescendingMode"));
        this.dataManager.set(HAS_LANDED, compound.getBoolean("HasLanded"));
        this.padPos = BlockPos.fromLong(compound.getLong("PadPos"));
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {

        int age = this.dataManager.get(AGE);
        boolean descendingMode = this.dataManager.get(DESCENDING_MODE);
        boolean hasLanded = this.dataManager.get(HAS_LANDED);

        if (age <= 55 && !descendingMode) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.drone.takeoff", ILoopType.EDefaultLoopTypes.PLAY_ONCE));
        }

        if ((age >= 55 || descendingMode) && !hasLanded) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.drone.flying", ILoopType.EDefaultLoopTypes.LOOP));
        }

        if (hasLanded) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.drone.landing", ILoopType.EDefaultLoopTypes.PLAY_ONCE));
        }

        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<EntityDrone>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
