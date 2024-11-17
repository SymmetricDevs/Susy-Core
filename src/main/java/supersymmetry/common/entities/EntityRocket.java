package supersymmetry.common.entities;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityRocket extends Entity {

    protected static final float jerk = 0.0001F;

    private static final DataParameter<Boolean> LAUNCHED = EntityDataManager.<Boolean>createKey(EntityRocket.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> COUNTDOWN_STARTED = EntityDataManager.<Boolean>createKey(EntityRocket.class, DataSerializers.BOOLEAN);

    private static final DataParameter<Integer> AGE = EntityDataManager.<Integer>createKey(EntityRocket.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> LAUNCH_TIME = EntityDataManager.<Integer>createKey(EntityRocket.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> FLIGHT_TIME = EntityDataManager.<Integer>createKey(EntityRocket.class, DataSerializers.VARINT);

    private static final DataParameter<Float> START_POS = EntityDataManager.<Float>createKey(EntityRocket.class, DataSerializers.FLOAT);

    public EntityRocket(World worldIn) {
        super(worldIn);
        this.setSize(3F, 31F);
        rideCooldown = -1;
        ignoreFrustumCheck = true;
    }

    public EntityRocket(World worldIn, double x, double y, double z) {
        super(worldIn);
        this.setLocationAndAngles(x, y, z, this.rotationYaw, 180.0F);
        this.setSize(11F, 46F);
        rideCooldown = -1;
        ignoreFrustumCheck = true;
        this.setEntityBoundingBox(new AxisAlignedBB(x - 5, y + 0.1, z - 5, x + 5, y + 46, z + 5));
    }

    public EntityRocket(World worldIn, BlockPos pos) {
        this(worldIn, (float)pos.getX() + 0.5F, pos.getY(), (float)pos.getZ() + 0.5F);
    }

    protected void entityInit(){
        this.dataManager.register(LAUNCHED, false);
        this.dataManager.register(COUNTDOWN_STARTED, false);
        this.dataManager.register(AGE, 0);
        this.dataManager.register(LAUNCH_TIME, 0);
        this.dataManager.register(FLIGHT_TIME, 0);
        this.dataManager.register(START_POS, 0.F);
    }

    public boolean isLaunched(){
        return this.dataManager.get(LAUNCHED);
    }

    public void setLaunched(boolean launched){
        this.dataManager.set(LAUNCHED, launched);
    }

    public boolean isCountDownStarted(){
        return this.dataManager.get(COUNTDOWN_STARTED);
    }

    public void setCountdownStarted(boolean countdownStarted){
        this.dataManager.set(COUNTDOWN_STARTED, countdownStarted);
    }

    public int getAge(){
        return this.dataManager.get(AGE);
    }

    public void setAge(Integer age){
        this.dataManager.set(AGE, age);
    }

    public int getFlightTime(){
        return this.dataManager.get(FLIGHT_TIME);
    }

    public void setFlightTime(Integer flightTime){
        this.dataManager.set(FLIGHT_TIME, flightTime);
    }

    public int getLaunchTime(){
        return this.dataManager.get(LAUNCH_TIME);
    }

    public void setLaunchTime(Integer launchTime){
        this.dataManager.set(LAUNCH_TIME, launchTime);
    }

    public float getStartPos(){
        return this.dataManager.get(START_POS);
    }

    public void setStartPos(Float startPos){
        this.dataManager.set(START_POS, startPos);
    }

    public void startCountdown(){
        this.setCountdownStarted(true);
        this.setLaunchTime(this.getAge() + 200);
        // TODO: Play sounds
        this.setStartPos((float)this.posY);
    }

    public void LaunchRocket(){
        this.setLaunched(true);
        this.isAirBorne = true;
    }

    public void explode() {
        this.world.newExplosion(this, this.posX, this.posY, this.posZ, 24, true, true);
        this.setDead();
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos){}

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

}
