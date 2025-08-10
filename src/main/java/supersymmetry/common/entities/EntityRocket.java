package supersymmetry.common.entities;

import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import supersymmetry.client.audio.MovingSoundRocket;
import supersymmetry.client.renderer.particles.SusyParticleFlameLarge;
import supersymmetry.client.renderer.particles.SusyParticleSmokeLarge;

import java.util.List;
import java.util.Random;

public class EntityRocket extends Entity {

    private static final Random rnd = new Random();
    protected static final float jerk = 0.0001F;

    private static final DataParameter<Boolean> LAUNCHED = EntityDataManager.<Boolean>createKey(EntityRocket.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> COUNTDOWN_STARTED = EntityDataManager.<Boolean>createKey(EntityRocket.class, DataSerializers.BOOLEAN);

    private static final DataParameter<Integer> AGE = EntityDataManager.<Integer>createKey(EntityRocket.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> LAUNCH_TIME = EntityDataManager.<Integer>createKey(EntityRocket.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> FLIGHT_TIME = EntityDataManager.<Integer>createKey(EntityRocket.class, DataSerializers.VARINT);

    private static final DataParameter<Float> START_POS = EntityDataManager.<Float>createKey(EntityRocket.class, DataSerializers.FLOAT);

    @SideOnly(Side.CLIENT)
    private MovingSoundRocket soundRocket;

    public EntityRocket(World worldIn) {
        super(worldIn);
        this.setSize(3F, 31F);
        rideCooldown = -1;
        ignoreFrustumCheck = true;
        isImmuneToFire = true;
    }

    public EntityRocket(World worldIn, double x, double y, double z, float rotationYaw) {
        this(worldIn);
        this.setLocationAndAngles(x, y, z, rotationYaw, 180.0F);
        this.setEntityBoundingBox(new AxisAlignedBB(x - 5, y + 0.1, z - 5, x + 5, y + 46, z + 5));
    }

    public EntityRocket(World worldIn, BlockPos pos, float rotationYaw) {
        this(worldIn, (float) pos.getX() + 0.5F, pos.getY(), (float) pos.getZ() + 0.5F, rotationYaw);
    }

    public EntityRocket(World worldIn, Vec3d pos, float rotationYaw) {
        this(worldIn, pos.x, pos.y, pos.z, rotationYaw);
    }


    protected void entityInit() {
        this.dataManager.register(LAUNCHED, false);
        this.dataManager.register(COUNTDOWN_STARTED, false);
        this.dataManager.register(AGE, 0);
        this.dataManager.register(LAUNCH_TIME, 0);
        this.dataManager.register(FLIGHT_TIME, 0);
        this.dataManager.register(START_POS, 0.F);
    }

    public boolean isLaunched() {
        return this.dataManager.get(LAUNCHED);
    }

    public void setLaunched(boolean launched) {
        this.dataManager.set(LAUNCHED, launched);
    }

    public boolean isCountDownStarted() {
        return this.dataManager.get(COUNTDOWN_STARTED);
    }

    public void setCountdownStarted(boolean countdownStarted) {
        this.dataManager.set(COUNTDOWN_STARTED, countdownStarted);
    }

    public int getAge() {
        return this.dataManager.get(AGE);
    }

    public void setAge(Integer age) {
        this.dataManager.set(AGE, age);
    }

    public int getFlightTime() {
        return this.dataManager.get(FLIGHT_TIME);
    }

    public void setFlightTime(Integer flightTime) {
        this.dataManager.set(FLIGHT_TIME, flightTime);
    }

    public int getLaunchTime() {
        return this.dataManager.get(LAUNCH_TIME);
    }

    public void setLaunchTime(Integer launchTime) {
        this.dataManager.set(LAUNCH_TIME, launchTime);
    }

    public float getStartPos() {
        return this.dataManager.get(START_POS);
    }

    public void setStartPos(Float startPos) {
        this.dataManager.set(START_POS, startPos);
    }

    public void startCountdown() {
        this.setCountdownStarted(true);
        this.setLaunchTime(this.getAge() + 200);
        this.setStartPos((float) this.posY);
    }

    public void launchRocket() {
        this.setLaunched(true);
        if (world.isRemote) {
            setupRocketSound();
            soundRocket.startPlaying();
        }
        this.isAirBorne = true;
    }

    @Override
    public void setDead() {
        super.setDead();
        if (world.isRemote && soundRocket != null) soundRocket.stopPlaying();
    }

    public void explode() {
        this.world.newExplosion(this, this.posX, this.posY, this.posZ, 24, true, true);
        this.setDead();
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        this.setLaunched(compound.getBoolean("Launched"));
        this.setCountdownStarted(compound.getBoolean("CountdownStarted"));
        this.setAge(compound.getInteger("Age"));
        this.setLaunchTime(compound.getInteger("LaunchTime"));
        this.setFlightTime(compound.getInteger("FlightTime"));
        this.setStartPos(compound.getFloat("StartPos"));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        compound.setBoolean("Launched", this.isLaunched());
        compound.setBoolean("CountdownStarted", this.isCountDownStarted());
        compound.setInteger("Age", this.getAge());
        compound.setInteger("LaunchTime", this.getLaunchTime());
        compound.setInteger("FlightTime", this.getFlightTime());
        compound.setFloat("StartPos", this.getStartPos());
    }

    @SideOnly(Side.CLIENT)
    protected void spawnFlightParticles() {
        // Main engine
        SusyParticleFlameLarge flame_0 = new SusyParticleFlameLarge(this.world, this.posX, this.posY, this.posZ, 1.5 * (rnd.nextFloat() - 0.5) * 0.08, -1.5, 1.5 * (rnd.nextFloat() - 0.5) * 0.08);
        SusyParticleSmokeLarge smoke_0 = new SusyParticleSmokeLarge(this.world, this.posX, this.posY, this.posZ, 1.5 * (rnd.nextFloat() - 0.5) * 0.16, -1.5, 1.5 * (rnd.nextFloat() - 0.5) * 0.16);
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke_0);
        Minecraft.getMinecraft().effectRenderer.addEffect(flame_0);

        // Main engine
        SusyParticleFlameLarge flame_1 = new SusyParticleFlameLarge(this.world, this.posX + 3, this.posY, this.posZ, 1.5 * (rnd.nextFloat() - 0.5) * 0.08, -1.5, 1.5 * (rnd.nextFloat() - 0.5) * 0.08);
        SusyParticleSmokeLarge smoke_1 = new SusyParticleSmokeLarge(this.world, this.posX + 3, this.posY, this.posZ, 1.5 * (rnd.nextFloat() - 0.5) * 0.16, -1.5, 1.5 * (rnd.nextFloat() - 0.5) * 0.16);
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke_1);
        Minecraft.getMinecraft().effectRenderer.addEffect(flame_1);

        // Main engine
        SusyParticleFlameLarge flame_2 = new SusyParticleFlameLarge(this.world, this.posX, this.posY, this.posZ + 3, 1.5 * (rnd.nextFloat() - 0.5) * 0.08, -1.5, 1.5 * (rnd.nextFloat() - 0.5) * 0.08);
        SusyParticleSmokeLarge smoke_2 = new SusyParticleSmokeLarge(this.world, this.posX, this.posY, this.posZ + 3, 1.5 * (rnd.nextFloat() - 0.5) * 0.16, -1.5, 1.5 * (rnd.nextFloat() - 0.5) * 0.16);
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke_2);
        Minecraft.getMinecraft().effectRenderer.addEffect(flame_2);

        // Main engine
        SusyParticleFlameLarge flame_3 = new SusyParticleFlameLarge(this.world, this.posX - 3, this.posY, this.posZ, 1.5 * (rnd.nextFloat() - 0.5) * 0.08, -1.5, 1.5 * (rnd.nextFloat() - 0.5) * 0.08);
        SusyParticleSmokeLarge smoke_3 = new SusyParticleSmokeLarge(this.world, this.posX - 3, this.posY, this.posZ, 1.5 * (rnd.nextFloat() - 0.5) * 0.16, -1.5, 1.5 * (rnd.nextFloat() - 0.5) * 0.16);
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke_3);
        Minecraft.getMinecraft().effectRenderer.addEffect(flame_3);

        // Main engine
        SusyParticleFlameLarge flame_4 = new SusyParticleFlameLarge(this.world, this.posX, this.posY, this.posZ - 3, 1.5 * (rnd.nextFloat() - 0.5) * 0.08, -1.5, 1.5 * (rnd.nextFloat() - 0.5) * 0.08);
        SusyParticleSmokeLarge smoke_4 = new SusyParticleSmokeLarge(this.world, this.posX, this.posY, this.posZ - 3, 1.5 * (rnd.nextFloat() - 0.5) * 0.16, -1.5, 1.5 * (rnd.nextFloat() - 0.5) * 0.16);
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke_4);
        Minecraft.getMinecraft().effectRenderer.addEffect(flame_4);
    }

    @SideOnly(Side.CLIENT)
    protected void spawnLaunchParticles(double v) {
        float startPos = this.getStartPos();
        float randFloat = rnd.nextFloat();
        float randSpeed = rnd.nextFloat();
        SusyParticleSmokeLarge smoke_x1 = new SusyParticleSmokeLarge(this.world, this.posX, startPos - 3, this.posZ, 0.5 + randSpeed, v * (randFloat - 0.5) * 0.16, v * (randFloat - 0.5) * 0.16);
        SusyParticleSmokeLarge smoke_x2 = new SusyParticleSmokeLarge(this.world, this.posX, startPos - 3, this.posZ, -(0.5 + randSpeed), v * (randFloat - 0.5) * 0.16, v * (randFloat - 0.5) * 0.16);
        SusyParticleSmokeLarge smoke_z1 = new SusyParticleSmokeLarge(this.world, this.posX, startPos - 3, this.posZ, v * (randFloat - 0.5) * 0.16, v * (randFloat - 0.5) * 0.16, 0.5 + randSpeed);
        SusyParticleSmokeLarge smoke_z2 = new SusyParticleSmokeLarge(this.world, this.posX, startPos - 3, this.posZ, v * (randFloat - 0.5) * 0.16, v * (randFloat - 0.5) * 0.16, -(0.5 + randSpeed));
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke_x1);
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke_x2);
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke_z1);
        Minecraft.getMinecraft().effectRenderer.addEffect(smoke_z2);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        boolean launched = this.isLaunched();
        int age = this.getAge();
        int launchTime = this.getLaunchTime();

        if (this.isCountDownStarted() && !launched && age >= launchTime) {
            this.launchRocket();
        }

        if (launched) {
            int flightTime = getFlightTime();
            float startPos = this.getStartPos();
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            this.motionY = jerk * Math.pow(getFlightTime(), 2) / 2;
            this.setPosition(this.posX, startPos + jerk * Math.pow(flightTime, 3) / 6, this.posZ);
            this.setFlightTime(flightTime + 1);

            if (flightTime % 2 == 0 && getEntityWorld().isRemote) {
                this.spawnFlightParticles();
            }

            if (this.posY > 600 || flightTime > 2400) {
                this.setDead();
            }

            if (this.world.collidesWithAnyBlock(this.getEntityBoundingBox())) {
                this.explode();
            }

            List<Entity> collidingEntities = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox());

            if (!collidingEntities.isEmpty()) {
                for (Entity entity : collidingEntities) {
                    entity.attackEntityFrom(DamageSource.FLY_INTO_WALL, (float) this.motionY * 10.f);
                }
            }
        }
/*
        if(age % 2 == 0 && this.isCountDownStarted()) {
            if(launchTime - age < 60 && launchTime - age > 0) {
                this.spawnLaunchParticles(0.025*(age - launchTime + 60));
            }else if(launchTime - age > -100 && launchTime - age < 0) {
                this.spawnLaunchParticles(1.5);
            }else if(launchTime - age > -150 && launchTime - age < -100) {
                this.spawnLaunchParticles(-0.03*(age - launchTime + 150));
            }
        }
*/
        this.setAge(age + 1);
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();

    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false; //note that this prevents it from being seen on theoneprobe, and /gs looking
    }

    @Override
    public EnumPushReaction getPushReaction() {
        return EnumPushReaction.IGNORE; //for pistons
    }

    @SideOnly(Side.CLIENT)
    public void setupRocketSound() {
        this.soundRocket = new MovingSoundRocket(this);
        Minecraft.getMinecraft().getSoundHandler().playSound(this.soundRocket);
    }

}
