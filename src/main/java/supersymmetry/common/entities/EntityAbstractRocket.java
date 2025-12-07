package supersymmetry.common.entities;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHandSide;
import net.minecraft.world.World;

import supersymmetry.api.rocketry.fuels.RocketFuelEntry;
import supersymmetry.common.blocks.rocketry.BlockSpacecraftInstrument;
import supersymmetry.common.rocketry.RocketConfiguration;

public abstract class EntityAbstractRocket extends EntityLivingBase {

    public static final String ROCKET_CONFIG_KEY = "config";

    protected static final DataParameter<Boolean> LAUNCHED = EntityDataManager.<Boolean>createKey(
            EntityAbstractRocket.class,
            DataSerializers.BOOLEAN);
    protected static final DataParameter<Boolean> COUNTDOWN_STARTED = EntityDataManager
            .<Boolean>createKey(EntityAbstractRocket.class, DataSerializers.BOOLEAN);

    protected static final DataParameter<Integer> AGE = EntityDataManager.<Integer>createKey(EntityAbstractRocket.class,
            DataSerializers.VARINT);
    protected static final DataParameter<Integer> LAUNCH_TIME = EntityDataManager.<Integer>createKey(
            EntityAbstractRocket.class,
            DataSerializers.VARINT);
    protected static final DataParameter<Integer> FLIGHT_TIME = EntityDataManager.<Integer>createKey(
            EntityAbstractRocket.class,
            DataSerializers.VARINT);

    protected static final DataParameter<Float> START_POS = EntityDataManager.<Float>createKey(
            EntityAbstractRocket.class,
            DataSerializers.FLOAT);
    protected static final DataParameter<Boolean> ACTED = EntityDataManager.<Boolean>createKey(
            EntityAbstractRocket.class,
            DataSerializers.BOOLEAN);

    public EntityAbstractRocket(World worldIn) {
        super(worldIn);
    }

    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(LAUNCHED, false);
        this.dataManager.register(COUNTDOWN_STARTED, false);
        this.dataManager.register(AGE, 0);
        this.dataManager.register(LAUNCH_TIME, 0);
        this.dataManager.register(FLIGHT_TIME, 0);
        this.dataManager.register(START_POS, 0.F);
        this.dataManager.register(ACTED, false);
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

    public boolean hasActed() {
        return this.dataManager.get(ACTED);
    }

    public void setActed(boolean acted) {
        this.dataManager.set(ACTED, acted);
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

    public void startCountdown(int length) {
        this.setCountdownStarted(true);
        this.setLaunchTime(this.getAge() + length);
        this.setStartPos((float) this.posY);
    }

    public void launchRocket() {
        this.setLaunched(true);
        this.setActed(false);
        this.isAirBorne = true;
    }

    public void explode() {
        this.world.newExplosion(this, this.posX, this.posY, this.posZ, this.getExplosionStrength(), true, true);
        this.setDead();
    }

    protected abstract float getExplosionStrength();

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (this.posY > 600 && this.isLaunched()) {
            if (this.hasActed() && this.getPassengers().isEmpty()) {
                this.setDead();
            } else {
                act();
                this.setActed(true);
            }
        }
    }

    protected void act() {
        NBTTagCompound instruments = this.getEntityData().getCompoundTag("rocket").getCompoundTag("instruments");
        for (String key : instruments.getKeySet()) {
            BlockSpacecraftInstrument.Type instrument = BlockSpacecraftInstrument.Type.valueOf(key);
            int count = instruments.getInteger(key);
            instrument.act(count, this);
        }
    }

    public RocketConfiguration getRocketConfiguration() {
        return new RocketConfiguration(this.getEntityData().getCompoundTag(ROCKET_CONFIG_KEY));
    }

    @Override
    public Iterable<ItemStack> getArmorInventoryList() {
        return null;
    }

    @Override
    public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {}

    @Override
    public EnumHandSide getPrimaryHand() {
        return EnumHandSide.RIGHT;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (amount < 30.0F) {
            return false;
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    public abstract RocketFuelEntry getFuel();

    public abstract double getCargoMass();
}
