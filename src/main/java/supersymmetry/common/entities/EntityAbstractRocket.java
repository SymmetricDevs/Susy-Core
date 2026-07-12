package supersymmetry.common.entities;

import static supersymmetry.api.rocketry.components.AbstractComponent.INSTRUMENTS_KEY;

import java.util.Arrays;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import supersymmetry.api.items.CargoItemStackHandler;
import supersymmetry.api.rocketry.fuels.RocketFuelEntry;
import supersymmetry.api.util.SuSyDamageSources;
import supersymmetry.common.EventHandlers;
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
    protected CargoItemStackHandler cargo;

    public EntityAbstractRocket(World worldIn) {
        super(worldIn);
    }

    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(LAUNCHED, false);
        this.dataManager.register(COUNTDOWN_STARTED, false);
        this.dataManager.register(AGE, 0);
        this.dataManager.register(LAUNCH_TIME, -1);
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

    public boolean isCountdownStarted() {
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
        // it will take six years chillax
        this.setLaunchTime((int) this.world.getTotalWorldTime() + length);
        this.setStartPos((float) this.posY);
    }

    public void launchRocket() {
        if (!this.world.isRemote) {
            this.setLaunched(true);
            this.setActed(false);
        }
        this.isAirBorne = true;
    }

    public void explode() {
        this.world.newExplosion(this, this.posX, this.posY, this.posZ, this.getExplosionStrength(), true, true);
        this.setDead();
    }

    protected abstract float getExplosionStrength();

    protected void act() {
        if (this.world.isRemote) return;
        NBTTagCompound instruments = this.getEntityData().getCompoundTag("rocket").getCompoundTag(INSTRUMENTS_KEY);
        for (String key : instruments.getKeySet()) {
            BlockSpacecraftInstrument.Type instrument = BlockSpacecraftInstrument.Type.getInstrument(key);
            int count = instruments.getInteger(key);
            instrument.act(count, this);
        }
        for (Entity passenger : this.getPassengers()) {
            if (!EventHandlers.isEntityTravelling(passenger)) {
                if (passenger instanceof EntityLivingBase living) {
                    living.attackEntityFrom(SuSyDamageSources.REENTRY, 100000000);
                }
                passenger.setDead();
            }
        }
    }

    public RocketConfiguration getRocketConfiguration() {
        return new RocketConfiguration(this.getEntityData().getCompoundTag(ROCKET_CONFIG_KEY));
    }

    @Override
    public Iterable<ItemStack> getArmorInventoryList() {
        return Arrays.asList();
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

    @Override
    public void writeEntityToNBT(@NotNull NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (this.cargo == null) {
            this.cargo = new CargoItemStackHandler(0, 0);
        }
        compound.setTag("cargo", this.cargo.serializeNBT());
    }

    @Override
    public void readEntityFromNBT(@NotNull NBTTagCompound compound) {
        if (compound == null || compound.tagMap.size() == 0) return;
        super.readEntityFromNBT(compound);
        if (this.cargo == null) {
            this.cargo = new CargoItemStackHandler(0, 0);
        }
        var cargoTag = compound.getCompoundTag("cargo");
        if (cargoTag == null) return;
        try {
            this.cargo.deserializeNBT(cargoTag);
        } catch (Exception e) {
            // shrug
        }
    }

    @Override
    public void updatePassenger(Entity passenger) {
        if (!passenger.world.isRemote && isCountdownStarted() && !isLaunched() &&
                passenger instanceof EntityPlayer player) {
            player.sendStatusMessage(
                    new TextComponentTranslation("susy.rocket.msg.launch",
                            (getLaunchTime() - getAge()) / 20),
                    true);
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        boolean launched = this.isLaunched();
        int launchTime = this.getLaunchTime();

        if (this.isCountdownStarted() && !launched && this.world.getTotalWorldTime() >= launchTime) {
            this.launchRocket();
        }
        this.setAge(this.getAge() + 1);
    }

    public CargoItemStackHandler getInventory() {
        return this.cargo;
    }
}
