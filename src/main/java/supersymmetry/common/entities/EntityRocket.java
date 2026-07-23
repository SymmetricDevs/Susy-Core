package supersymmetry.common.entities;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import cam72cam.mod.entity.boundingbox.BoundingBox;
import gregtech.api.GregTechAPI;
import gregtech.modules.ModuleManager;
import supersymmetry.Supersymmetry;
import supersymmetry.api.items.CargoItemStackHandler;
import supersymmetry.api.rocketry.fuels.RocketFuelEntry;
import supersymmetry.api.rocketry.rockets.AFSRendered;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.client.audio.MovingSoundRocket;
import supersymmetry.client.renderer.handler.IAlwaysRender;
import supersymmetry.client.renderer.particles.SusyParticleFlameLarge;
import supersymmetry.client.renderer.particles.SusyParticleSmokeLarge;
import supersymmetry.common.advancement.SusyCriteriaTriggers;
import supersymmetry.common.network.CPacketRocketInteract;
import supersymmetry.common.rocketry.SuccessCalculation.LaunchResult;
import supersymmetry.integration.baubles.BaublesModule;
import supersymmetry.modules.SuSyModules;

public class EntityRocket extends EntityAbstractRocket implements IAlwaysRender, AFSRendered {

    private static final Random rnd = new Random();
    protected static final float jerk = 0.0001F;

    protected static final DataParameter<String> FUEL = EntityDataManager.createKey(EntityRocket.class,
            DataSerializers.STRING);
    private int maxFuelVolume;

    // Troll mode - rocket curves back towards launch pad
    protected static final DataParameter<Integer> LAUNCH_RESULT = EntityDataManager.createKey(EntityRocket.class,
            DataSerializers.VARINT);
    protected static final DataParameter<BlockPos> CRASH_POSITION = EntityDataManager.createKey(EntityRocket.class,
            DataSerializers.BLOCK_POS);

    @SideOnly(Side.CLIENT)
    private MovingSoundRocket soundRocket;

    public EntityRocket(World worldIn) {
        super(worldIn);
        this.setSize(3F, 46F);
        rideCooldown = -1;
        ignoreFrustumCheck = true;
        isImmuneToFire = true;
    }

    public EntityRocket(World worldIn, double x, double y, double z, float rotationYaw) {
        this(worldIn);
        this.setLocationAndAngles(x, y, z, rotationYaw, 0);
        this.setEntityBoundingBox(new AxisAlignedBB(x - 5, y + 0.1, z - 5, x + 5, y + 46, z + 5));
    }

    public EntityRocket(World worldIn, BlockPos pos, float rotationYaw) {
        this(worldIn, (float) pos.getX() + 0.5F, pos.getY(), (float) pos.getZ() + 0.5F, rotationYaw);
    }

    public EntityRocket(World worldIn, Vec3d pos, float rotationYaw) {
        this(worldIn, pos.x, pos.y, pos.z, rotationYaw);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(FUEL, "");
        this.dataManager.register(LAUNCH_RESULT, LaunchResult.LAUNCHES.ordinal());
        this.dataManager.register(CRASH_POSITION, BlockPos.ORIGIN);
    }

    public void launchRocket() {
        if (this.getFuel() == null) {
            setLaunchTime(-1);
            setCountdownStarted(false);
            return;
        }
        if (world.isRemote) {
            setupRocketSound();
            soundRocket.startPlaying();
        } else {
            if (this.getEntityData().hasKey("rocket")) {
                NBTTagCompound rocketNBT = this.getEntityData().getCompoundTag("rocket");
                AbstractRocketBlueprint blueprint = AbstractRocketBlueprint
                        .getCopyOf(rocketNBT.getString("name"));
                blueprint.readFromNBT(rocketNBT);
                BlockPos assemblerPosition = BlockPos.fromLong(this.getEntityData().getLong("assemblerPosition"));
                if (!assemblerPosition.equals(BlockPos.NULL_VECTOR) &&
                        this.getPosition().distanceSq(assemblerPosition) < 100) {
                    this.setCrashPosition(assemblerPosition);
                    this.setLaunchResult(LaunchResult.CRASHES);
                } else {
                    long augmentation = rocketNBT.getLong("AFSimprovement");
                    if (this.getPassengers().stream()
                            .noneMatch((entity -> entity instanceof EntityPlayer player && player.isCreative()))) {
                        this.setLaunchResult(blueprint.calculateSuccess(this, augmentation));
                    } else {
                        this.setLaunchResult(LaunchResult.LAUNCHES);
                    }
                }
            } else {
                this.setLaunchResult(LaunchResult.EXPLODES);
            }
        }
        super.launchRocket();
    }

    @Override
    public void setDead() {
        super.setDead();
        if (world.isRemote && soundRocket != null) soundRocket.stopPlaying();
    }

    @Override
    protected float getExplosionStrength() {
        return 50; // Needs to cover the player
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {}

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setLaunched(compound.getBoolean("Launched"));
        this.setCountdownStarted(compound.getBoolean("CountdownStarted"));
        this.setAge(compound.getInteger("Age"));
        this.setActed(compound.getBoolean("Acted"));
        this.setLaunchTime(compound.getInteger("LaunchTime"));
        this.setFlightTime(compound.getInteger("FlightTime"));
        this.setStartPos(compound.getFloat("StartPos"));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Launched", this.isLaunched());
        compound.setBoolean("CountdownStarted", this.isCountdownStarted());
        compound.setInteger("Age", this.getAge());
        compound.setBoolean("Acted", this.hasActed());
        compound.setInteger("LaunchTime", this.getLaunchTime());
        compound.setInteger("FlightTime", this.getFlightTime());
        compound.setFloat("StartPos", this.getStartPos());
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (!this.getEntityData().hasKey("rocket")) {
            // Testing only
            this.cargo = new CargoItemStackHandler(10000, 10000);
            this.maxFuelVolume = 1;
        } else {
            NBTTagCompound rocketNBT = this.getEntityData().getCompoundTag("rocket");
            AbstractRocketBlueprint blueprint = AbstractRocketBlueprint
                    .getCopyOf(rocketNBT.getString("name"));
            blueprint.readFromNBT(rocketNBT);
            this.cargo = new CargoItemStackHandler((int) blueprint.getCargoVolume(),
                    Integer.MAX_VALUE);
            this.maxFuelVolume = (int) blueprint.getFuelVolume();
        }
    }

    @SideOnly(Side.CLIENT)
    protected void spawnFlightParticles() {
        // Main engine
        SusyParticleFlameLarge flame_0 = new SusyParticleFlameLarge(this.world, this.posX, this.posY, this.posZ,
                1.5 * (rnd.nextFloat() - 0.5) * 0.08, -1.5, 1.5 * (rnd.nextFloat() - 0.5) * 0.08);
        SusyParticleSmokeLarge smoke_0 = new SusyParticleSmokeLarge(this.world, this.posX, this.posY, this.posZ,
                1.5 * (rnd.nextFloat() - 0.5) * 0.16, -1.5, 1.5 * (rnd.nextFloat() - 0.5) * 0.16);
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke_0);
        Minecraft.getMinecraft().effectRenderer.addEffect(flame_0);

        // Main engine
        SusyParticleFlameLarge flame_1 = new SusyParticleFlameLarge(this.world, this.posX + 3, this.posY, this.posZ,
                1.5 * (rnd.nextFloat() - 0.5) * 0.08, -1.5, 1.5 * (rnd.nextFloat() - 0.5) * 0.08);
        SusyParticleSmokeLarge smoke_1 = new SusyParticleSmokeLarge(this.world, this.posX + 3, this.posY, this.posZ,
                1.5 * (rnd.nextFloat() - 0.5) * 0.16, -1.5, 1.5 * (rnd.nextFloat() - 0.5) * 0.16);
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke_1);
        Minecraft.getMinecraft().effectRenderer.addEffect(flame_1);

        // Main engine
        SusyParticleFlameLarge flame_2 = new SusyParticleFlameLarge(this.world, this.posX, this.posY, this.posZ + 3,
                1.5 * (rnd.nextFloat() - 0.5) * 0.08, -1.5, 1.5 * (rnd.nextFloat() - 0.5) * 0.08);
        SusyParticleSmokeLarge smoke_2 = new SusyParticleSmokeLarge(this.world, this.posX, this.posY, this.posZ + 3,
                1.5 * (rnd.nextFloat() - 0.5) * 0.16, -1.5, 1.5 * (rnd.nextFloat() - 0.5) * 0.16);
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke_2);
        Minecraft.getMinecraft().effectRenderer.addEffect(flame_2);

        // Main engine
        SusyParticleFlameLarge flame_3 = new SusyParticleFlameLarge(this.world, this.posX - 3, this.posY, this.posZ,
                1.5 * (rnd.nextFloat() - 0.5) * 0.08, -1.5, 1.5 * (rnd.nextFloat() - 0.5) * 0.08);
        SusyParticleSmokeLarge smoke_3 = new SusyParticleSmokeLarge(this.world, this.posX - 3, this.posY, this.posZ,
                1.5 * (rnd.nextFloat() - 0.5) * 0.16, -1.5, 1.5 * (rnd.nextFloat() - 0.5) * 0.16);
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke_3);
        Minecraft.getMinecraft().effectRenderer.addEffect(flame_3);

        // Main engine
        SusyParticleFlameLarge flame_4 = new SusyParticleFlameLarge(this.world, this.posX, this.posY, this.posZ - 3,
                1.5 * (rnd.nextFloat() - 0.5) * 0.08, -1.5, 1.5 * (rnd.nextFloat() - 0.5) * 0.08);
        SusyParticleSmokeLarge smoke_4 = new SusyParticleSmokeLarge(this.world, this.posX, this.posY, this.posZ - 3,
                1.5 * (rnd.nextFloat() - 0.5) * 0.16, -1.5, 1.5 * (rnd.nextFloat() - 0.5) * 0.16);
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke_4);
        Minecraft.getMinecraft().effectRenderer.addEffect(flame_4);
    }

    @SideOnly(Side.CLIENT)
    protected void spawnLaunchParticles(double v) {
        float startPos = this.getStartPos();
        float randFloat = rnd.nextFloat();
        float randSpeed = rnd.nextFloat();
        SusyParticleSmokeLarge smoke_x1 = new SusyParticleSmokeLarge(this.world, this.posX, startPos - 3, this.posZ,
                0.5 + randSpeed, v * (randFloat - 0.5) * 0.16, v * (randFloat - 0.5) * 0.16);
        SusyParticleSmokeLarge smoke_x2 = new SusyParticleSmokeLarge(this.world, this.posX, startPos - 3, this.posZ,
                -(0.5 + randSpeed), v * (randFloat - 0.5) * 0.16, v * (randFloat - 0.5) * 0.16);
        SusyParticleSmokeLarge smoke_z1 = new SusyParticleSmokeLarge(this.world, this.posX, startPos - 3, this.posZ,
                v * (randFloat - 0.5) * 0.16, v * (randFloat - 0.5) * 0.16, 0.5 + randSpeed);
        SusyParticleSmokeLarge smoke_z2 = new SusyParticleSmokeLarge(this.world, this.posX, startPos - 3, this.posZ,
                v * (randFloat - 0.5) * 0.16, v * (randFloat - 0.5) * 0.16, -(0.5 + randSpeed));
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke_x1);
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke_x2);
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke_z1);
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke_z2);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        boolean launched = this.isLaunched();

        if (launched) {
            int flightTime = getFlightTime();
            float startPos = this.getStartPos();
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;

            if (this.getLaunchResult() == LaunchResult.CRASHES && this.posY > 256) {
                BlockPos targetPos = this.getPosition().add(((Math.random() * 2) - 1) * 1000, 0,
                        ((Math.random() * 2) - 1) * 1000);
                // Clear out Y
                this.setCrashPosition(targetPos.down(targetPos.getY()));
            }

            if (this.getLaunchResult() == LaunchResult.EXPLODES &&
                    this.posY > 400) {
                this.explode();
            }

            // Troll mode: curve the rocket back towards the launch pad
            if (this.getLaunchResult() == LaunchResult.CRASHES && this.getCrashPosition() != null) {
                // Calculate direction to target
                double dx = this.getCrashPosition().getX() + 0.5 - this.posX;
                double dy = this.getCrashPosition().getY() - this.posY;
                double dz = this.getCrashPosition().getZ() + 0.5 - this.posZ;
                double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

                // Calculate target yaw and pitch
                float targetYaw = 90 + (float) (Math.atan2(dz, dx) * 180.0 / Math.PI);
                float targetPitch = 180 + (float) (-(Math.atan2(dy, horizontalDistance) * 180.0 / Math.PI));

                // Gradually adjust yaw and pitch (semi-realistic curve)
                float yawDiff = targetYaw - this.rotationYaw;
                while (yawDiff > 180.0F) yawDiff -= 360.0F;
                while (yawDiff < -180.0F) yawDiff += 360.0F;

                float pitchDiff = targetPitch - this.rotationPitch;
                while (pitchDiff > 180.0F) pitchDiff -= 360.0F;
                while (pitchDiff < -180.0F) pitchDiff += 360.0F;

                // Curve rate increases with flight time (rocket becomes more unstable)
                float curveRate = Math.min(flightTime * flightTime * 0.000001F, 5.0F);
                this.rotationYaw += yawDiff * curveRate;
                this.rotationPitch += pitchDiff * curveRate * 0.05F;

                // Apply lateral motion based on rotation
                double speed = jerk * Math.pow(flightTime, 2) / 2;
                double yawRad = Math.toRadians(this.rotationYaw);
                double pitchRad = Math.toRadians(this.rotationPitch);

                this.motionX = -Math.sin(yawRad) * Math.sin(pitchRad) * speed;
                this.motionZ = Math.cos(yawRad) * Math.sin(pitchRad) * speed;
                this.motionY = Math.cos(pitchRad) * speed;

                this.setPositionAndRotation(
                        this.posX + this.motionX,
                        this.posY + this.motionY,
                        this.posZ + this.motionZ,
                        this.rotationYaw,
                        this.rotationPitch);
            } else {
                // Normal flight
                this.motionY = jerk * Math.pow(getFlightTime(), 2) / 2;
                this.setPosition(this.posX, startPos + jerk * Math.pow(flightTime, 3) / 6, this.posZ);
            }

            this.setFlightTime(flightTime + 1);

            if (flightTime % 2 == 0 && getEntityWorld().isRemote) {
                this.spawnFlightParticles();
            }

            if (!world.isRemote) {
                for (Entity passenger : this.getPassengers()) {
                    if (passenger instanceof EntityPlayerMP) {
                        SusyCriteriaTriggers.ROCKET_LAUNCH.trigger((EntityPlayerMP) passenger);
                    }
                }
            }

            if (this.world.collidesWithAnyBlock(this.getEntityBoundingBox())) {
                this.explode();
            }

            List<Entity> collidingEntities = this.world.getEntitiesWithinAABBExcludingEntity(this,
                    this.getEntityBoundingBox());

            for (Entity entity : collidingEntities) {
                if (!entity.isRidingSameEntity(this))
                    entity.attackEntityFrom(DamageSource.FLY_INTO_WALL, (float) this.motionY * 10.f);
            }

            if (this.posY > 1000 && this.getLaunchResult() == LaunchResult.LAUNCHES) {
                if (this.hasActed() && this.getPassengers().isEmpty()) {
                    this.setDead();
                } else {
                    act();
                    this.setActed(true);
                }
            }
        }
        /*
         * if(age % 2 == 0 && this.isCountDownStarted()) {
         * if(launchTime - age < 60 && launchTime - age > 0) {
         * this.spawnLaunchParticles(0.025*(age - launchTime + 60));
         * }else if(launchTime - age > -100 && launchTime - age < 0) {
         * this.spawnLaunchParticles(1.5);
         * }else if(launchTime - age > -150 && launchTime - age < -100) {
         * this.spawnLaunchParticles(-0.03*(age - launchTime + 150));
         * }
         * }
         */
    }

    @Override
    public double getCargoMass() {
        double mass = 0;
        for (Entity passenger : getPassengers()) {
            if (passenger instanceof EntityPlayer player) {
                mass += 70;
                for (ItemStack stack : player.inventory.mainInventory) {
                    mass += (double) CargoItemStackHandler.getMassPerItem(stack) / 1000;
                }
                for (ItemStack stack : player.inventory.armorInventory) {
                    mass += (double) CargoItemStackHandler.getMassPerItem(stack) / 1000;
                }
                for (ItemStack stack : player.inventory.offHandInventory) {
                    mass += (double) CargoItemStackHandler.getMassPerItem(stack) / 1000;
                }
                if (ModuleManager.getInstance().isModuleEnabled(Supersymmetry.MODID, SuSyModules.MODULE_BAUBLES)) {
                    mass += (double) BaublesModule.getBaubleMass(player) / 1000;
                }
            }
        }
        return mass + (double) cargo.mass() / 1000;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true; // This needs to be true to allow passengers
    }

    @Override
    protected void collideWithNearbyEntities() {
        // do nothing ourselves, although other entities will still collide with us
    }

    @Override
    public EnumPushReaction getPushReaction() {
        return EnumPushReaction.IGNORE; // for pistons
    }

    @SideOnly(Side.CLIENT)
    public void setupRocketSound() {
        this.soundRocket = new MovingSoundRocket(this);
        Minecraft.getMinecraft().getSoundHandler().playSound(this.soundRocket);
    }

    @Override // The override is about leashing the rocket, which makes it alright to completely ignore
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d hitVec, EnumHand hand) {
        if (player.isRidingSameEntity(this) || hitVec.y < 37 || hitVec.y > 44) return EnumActionResult.PASS;
        if (!this.world.isRemote) {
            player.startRiding(this);
        } else {
            GregTechAPI.networkHandler.sendToServer(new CPacketRocketInteract(this, hand, hitVec));
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public double getMountedYOffset() {
        return 38D;
    }

    public void updatePassenger(Entity passenger) {
        super.updatePassenger(passenger);
        if (this.isPassenger(passenger)) {
            double yawRad = Math.toRadians(this.rotationYaw);
            double pitchRad = Math.toRadians(this.rotationPitch);
            passenger.setPosition(this.posX + this.getMountedYOffset() * -Math.sin(yawRad) * Math.sin(pitchRad),
                    this.posY + this.getMountedYOffset() * Math.cos(pitchRad) + passenger.getYOffset(),
                    this.posZ + this.getMountedYOffset() * Math.cos(yawRad) * Math.sin(pitchRad));
        }
    }

    @Override
    public AxisAlignedBB modelAABB() {
        return new AxisAlignedBB(new Vec3d(4, 46, 4), (new Vec3d(-4, 0, -4)));
    }

    public int getFuelVolume() {
        return this.maxFuelVolume;
    }

    public void setLaunchResult(LaunchResult result) {
        this.dataManager.set(LAUNCH_RESULT, result.ordinal());
    }

    public LaunchResult getLaunchResult() {
        return LaunchResult.values()[this.dataManager.get(LAUNCH_RESULT)];
    }

    public BlockPos getCrashPosition() {
        return this.dataManager.get(CRASH_POSITION);
    }

    public void setCrashPosition(BlockPos pos) {
        this.dataManager.set(CRASH_POSITION, pos);
    }

    public void setFuel(RocketFuelEntry fuelEntry) {
        this.dataManager.set(FUEL, fuelEntry.getRegistryName());
    }

    @Override
    public RocketFuelEntry getFuel() {
        return RocketFuelEntry.getCopyOf(this.dataManager.get(FUEL));
    }

    @Override
    protected boolean canFitPassenger(Entity passenger) {
        return this.getPassengers().size() < 4;
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        AxisAlignedBB aabb = new AxisAlignedBB(passenger.getPosition()).grow(5, 1, 5);
        List<AxisAlignedBB> boxes = this.world.getCollisionBoxes(passenger, aabb);
        if (!boxes.isEmpty()) {
            Vec3d newPos = boxes.get(0).getCenter();
            passenger.setPosition(newPos.x, newPos.y, newPos.z);
            float f = passenger.width / 2.0F;
            float f1 = passenger.height;
            AxisAlignedBB pAABB = new AxisAlignedBB(newPos.x - (double) f, newPos.y, newPos.z - (double) f,
                    newPos.x + (double) f, newPos.y + (double) f1, newPos.z + (double) f);

            // Raise upwards until the passenger no longer intersects anything in the world
            while (world.collidesWithAnyBlock(pAABB)) {
                newPos = newPos.add(0, 0.5, 0);
                pAABB = new AxisAlignedBB(newPos.x - (double) f, newPos.y, newPos.z - (double) f,
                        newPos.x + (double) f, newPos.y + (double) f1, newPos.z + (double) f);
            }
            passenger.setPositionAndUpdate(newPos.x, newPos.y, newPos.z);
        }
    }

    @Override
    public void move(MoverType type, double x, double y, double z) {
        if (this.noClip) {
            this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, y, z));
            this.resetPositionToBB();
        } else {
            if (type == MoverType.PISTON) {
                return;
            }

            this.world.profiler.startSection("move");
            double d10 = this.posX;
            double d11 = this.posY;
            double d1 = this.posZ;

            if (this.isInWeb) {
                this.isInWeb = false;
                x *= 0.25D;
                y *= 0.05000000074505806D;
                z *= 0.25D;
                this.motionX = 0.0D;
                this.motionY = 0.0D;
                this.motionZ = 0.0D;
            }

            double d2 = x;
            double d3 = y;
            double d4 = z;

            List<AxisAlignedBB> list1 = this.world.getCollisionBoxes(this, this.getEntityBoundingBox().expand(x, y, z));
            AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();

            if (y != 0.0D) {
                int k = 0;

                for (int l = list1.size(); k < l; ++k) {
                    if (!(list1.get(k) instanceof BoundingBox)) // Hacky way to fix clipping into the T/E
                        y = list1.get(k).calculateYOffset(this.getEntityBoundingBox(), y);
                }

                this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, y, 0.0D));
            }

            if (x != 0.0D) {
                int j5 = 0;

                for (int l5 = list1.size(); j5 < l5; ++j5) {
                    if (!(list1.get(j5) instanceof BoundingBox))
                        x = list1.get(j5).calculateXOffset(this.getEntityBoundingBox(), x);
                }

                if (x != 0.0D) {
                    if (!(list1 instanceof BoundingBox))
                        this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, 0.0D, 0.0D));
                }
            }

            if (z != 0.0D) {
                int k5 = 0;

                for (int i6 = list1.size(); k5 < i6; ++k5) {
                    if (!(list1.get(k5) instanceof BoundingBox))
                        z = list1.get(k5).calculateZOffset(this.getEntityBoundingBox(), z);
                }

                if (z != 0.0D) {
                    this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, 0.0D, z));
                }
            }

            boolean flag = this.onGround || d3 != y && d3 < 0.0D;

            if (this.stepHeight > 0.0F && flag && (d2 != x || d4 != z)) {
                double d14 = x;
                double d6 = y;
                double d7 = z;
                AxisAlignedBB axisalignedbb1 = this.getEntityBoundingBox();
                this.setEntityBoundingBox(axisalignedbb);
                y = this.stepHeight;
                List<AxisAlignedBB> list = this.world.getCollisionBoxes(this,
                        this.getEntityBoundingBox().expand(d2, y, d4));
                AxisAlignedBB axisalignedbb2 = this.getEntityBoundingBox();
                AxisAlignedBB axisalignedbb3 = axisalignedbb2.expand(d2, 0.0D, d4);
                double d8 = y;
                int j1 = 0;

                for (int k1 = list.size(); j1 < k1; ++j1) {
                    d8 = list.get(j1).calculateYOffset(axisalignedbb3, d8);
                }

                axisalignedbb2 = axisalignedbb2.offset(0.0D, d8, 0.0D);
                double d18 = d2;
                int l1 = 0;

                for (int i2 = list.size(); l1 < i2; ++l1) {
                    d18 = list.get(l1).calculateXOffset(axisalignedbb2, d18);
                }

                axisalignedbb2 = axisalignedbb2.offset(d18, 0.0D, 0.0D);
                double d19 = d4;
                int j2 = 0;

                for (int k2 = list.size(); j2 < k2; ++j2) {
                    d19 = list.get(j2).calculateZOffset(axisalignedbb2, d19);
                }

                axisalignedbb2 = axisalignedbb2.offset(0.0D, 0.0D, d19);
                AxisAlignedBB axisalignedbb4 = this.getEntityBoundingBox();
                double d20 = y;
                int l2 = 0;

                for (int i3 = list.size(); l2 < i3; ++l2) {
                    d20 = list.get(l2).calculateYOffset(axisalignedbb4, d20);
                }

                axisalignedbb4 = axisalignedbb4.offset(0.0D, d20, 0.0D);
                double d21 = d2;
                int j3 = 0;

                for (int k3 = list.size(); j3 < k3; ++j3) {
                    d21 = list.get(j3).calculateXOffset(axisalignedbb4, d21);
                }

                axisalignedbb4 = axisalignedbb4.offset(d21, 0.0D, 0.0D);
                double d22 = d4;
                int l3 = 0;

                for (int i4 = list.size(); l3 < i4; ++l3) {
                    d22 = list.get(l3).calculateZOffset(axisalignedbb4, d22);
                }

                axisalignedbb4 = axisalignedbb4.offset(0.0D, 0.0D, d22);
                double d23 = d18 * d18 + d19 * d19;
                double d9 = d21 * d21 + d22 * d22;

                if (d23 > d9) {
                    x = d18;
                    z = d19;
                    y = -d8;
                    this.setEntityBoundingBox(axisalignedbb2);
                } else {
                    x = d21;
                    z = d22;
                    y = -d20;
                    this.setEntityBoundingBox(axisalignedbb4);
                }

                int j4 = 0;

                for (int k4 = list.size(); j4 < k4; ++j4) {
                    y = list.get(j4).calculateYOffset(this.getEntityBoundingBox(), y);
                }

                this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, y, 0.0D));

                if (d14 * d14 + d7 * d7 >= x * x + z * z) {
                    x = d14;
                    y = d6;
                    z = d7;
                    this.setEntityBoundingBox(axisalignedbb1);
                }
            }

            this.world.profiler.endSection();
            this.world.profiler.startSection("rest");
            this.resetPositionToBB();
            this.collidedHorizontally = d2 != x || d4 != z;
            this.collidedVertically = d3 != y;
            this.onGround = this.collidedVertically && d3 < 0.0D;
            this.collided = this.collidedHorizontally || this.collidedVertically;
            int j6 = MathHelper.floor(this.posX);
            int i1 = MathHelper.floor(this.posY - 0.20000000298023224D);
            int k6 = MathHelper.floor(this.posZ);
            BlockPos blockpos = new BlockPos(j6, i1, k6);
            IBlockState iblockstate = this.world.getBlockState(blockpos);

            if (iblockstate.getMaterial() == Material.AIR) {
                BlockPos blockpos1 = blockpos.down();
                IBlockState iblockstate1 = this.world.getBlockState(blockpos1);
                Block block1 = iblockstate1.getBlock();

                if (block1 instanceof BlockFence || block1 instanceof BlockWall || block1 instanceof BlockFenceGate) {
                    iblockstate = iblockstate1;
                    blockpos = blockpos1;
                }
            }

            this.updateFallState(y, this.onGround, iblockstate, blockpos);

            if (d2 != x) {
                this.motionX = 0.0D;
            }

            if (d4 != z) {
                this.motionZ = 0.0D;
            }

            Block block = iblockstate.getBlock();

            if (d3 != y) {
                block.onLanded(this.world, this);
            }

            try {
                this.doBlockCollisions();
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Checking entity block collision");
                CrashReportCategory crashreportcategory = crashreport
                        .makeCategory("Entity being checked for collision");
                this.addEntityCrashInfo(crashreportcategory);
                throw new ReportedException(crashreport);
            }

            boolean flag1 = this.isWet();

            this.world.profiler.endSection();
        }
    }
}
