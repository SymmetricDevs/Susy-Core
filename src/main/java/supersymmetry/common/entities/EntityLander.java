package supersymmetry.common.entities;

import gregtech.api.GTValues;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
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

public class EntityLander extends EntityLiving implements IAnimatable, ILockableContainer {

    private static final DataParameter<Boolean> HAS_LANDED = EntityDataManager.<Boolean>createKey(EntityLander.class,
            DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> TIME_SINCE_LANDING = EntityDataManager
            .<Integer>createKey(EntityLander.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> TIME_SINCE_SPAWN = EntityDataManager
            .<Integer>createKey(EntityLander.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> HAS_TAKEN_OFF = EntityDataManager.<Boolean>createKey(EntityLander.class,
            DataSerializers.BOOLEAN);

    private AnimationFactory factory = new AnimationFactory(this);

    private IItemHandlerModifiable inventory = new ItemStackHandler(36);
    private LockCode lockCode = LockCode.EMPTY_CODE;
    @SideOnly(Side.CLIENT)
    private MovingSoundDropPod soundDropPod;

    public EntityLander(World worldIn) {
        super(worldIn);
        this.deathTime = 0;
    }

    public EntityLander(World worldIn, double x, double y, double z) {
        this(worldIn);
        this.setLocationAndAngles(x, y, z, 0.F, 0.F);
        this.setEntityBoundingBox(new AxisAlignedBB(x - 0.5, y, z - 0.5, x + 0.5, y + 2, z + 0.5));
    }

    public EntityLander(World worldIn, BlockPos pos) {
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

    public void setTimeSinceLanding(int timeSinceLanding) {
        this.dataManager.set(TIME_SINCE_LANDING, timeSinceLanding);
    }

    public boolean hasTakenOff() {
        return this.dataManager.get(HAS_TAKEN_OFF);
    }

    public void setHasTakenOff(boolean takenOff) {
        this.dataManager.set(HAS_TAKEN_OFF, takenOff);
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
        this.dataManager.register(HAS_TAKEN_OFF, false);
    }

    @Override
    public void writeEntityToNBT(@NotNull NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("landed", this.hasLanded());
        compound.setInteger("time_since_landing", this.getTimeSinceLanding());
        compound.setBoolean("taken_off", this.hasTakenOff());
        
        // Write inventory
        if (this.inventory instanceof ItemStackHandler) {
            compound.setTag("Inventory", ((ItemStackHandler) this.inventory).serializeNBT());
        }
        
        // Write lock code
        if (!this.lockCode.isEmpty()) {
            this.lockCode.toNBT(compound);
        }
    }

    @Override
    public void readEntityFromNBT(@NotNull NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setLanded(compound.getBoolean("Landed"));
        this.setTimeSinceLanding(compound.getInteger("time_since_landing"));
        this.setHasTakenOff(compound.getBoolean("taken_off"));
        
        // Read inventory
        if (this.inventory instanceof ItemStackHandler && compound.hasKey("Inventory", Constants.NBT.TAG_COMPOUND)) {
            ((ItemStackHandler) this.inventory).deserializeNBT(compound.getCompoundTag("Inventory"));
        }
        
        // Read lock code
        this.lockCode = LockCode.fromNBT(compound);
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
            if (!this.onGround && this.motionY < 0.0D && this.posY < 256) {
                // Lerp between motionY and 0.5 at 10%
                this.motionY = MathHelper.clampedLerp(this.motionY, -0.5F, 0.1D);
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
                .addAnimationController(new AnimationController<EntityLander>(this, "controller", 0, this::predicate));
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

    @Override
    public boolean isLocked() {
        return this.lockCode != null && !this.lockCode.isEmpty();
    }

    @Override
    public void setLockCode(LockCode code) {
        this.lockCode = code;
    }

    @Override
    public LockCode getLockCode() {
        return this.lockCode;
    }

    @Override
    public int getSizeInventory() {
        return this.inventory.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < this.inventory.getSlots(); i++) {
            if (!this.inventory.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return this.inventory.getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = this.inventory.getStackInSlot(index);
        if (!stack.isEmpty()) {
            if (stack.getCount() <= count) {
                this.inventory.setStackInSlot(index, ItemStack.EMPTY);
                this.markDirty();
                return stack;
            } else {
                ItemStack result = stack.splitStack(count);
                if (stack.isEmpty()) {
                    this.inventory.setStackInSlot(index, ItemStack.EMPTY);
                }
                this.markDirty();
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = this.inventory.getStackInSlot(index);
        this.inventory.setStackInSlot(index, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.inventory.setStackInSlot(index, stack);
        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }
        this.markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        // Entity inventories typically don't need special dirty handling
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        if (this.isDead) {
            return false;
        }
        return player.getDistanceSq(this) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer player) {
        // No special handling needed
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        // No special handling needed
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
        // No fields to set
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        for (int i = 0; i < this.inventory.getSlots(); i++) {
            this.inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new ContainerChest(playerInventory, this, playerIn);
    }

    @Override
    public String getGuiID() {
        return "supersymmetry:lander";
    }
    
    /**
     * Gets the IItemHandlerModifiable for this lander.
     * This can be used for capability-based inventory access.
     */
    public IItemHandlerModifiable getInventory() {
        return this.inventory;
    }
}
