package supersymmetry.api.capability.impl;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.capability.impl.RecipeLogicEnergy;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.ConfigHolder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import supersymmetry.common.metatileentities.single.rocket.MetaTileEntityComponentScanner;

public class ScannerLogic {
    private int progressTime = 0;
    private int goalTime = 1;
    private boolean isActive;
    private boolean workingEnabled = true;
    private boolean hasNotEnoughEnergy;
    private boolean scanComplete;
    private MetaTileEntityComponentScanner mte;
    private boolean wasActiveAndNeedsUpdate;

    public ScannerLogic(MetaTileEntity mte) {
        this.mte = (MetaTileEntityComponentScanner) mte;
        scanComplete = false;
    }

    public void setGoalTime(float duration) {
        this.goalTime = (int)duration*20;
    }

    public void updateLogic() {
        if (!this.isWorkingEnabled() || !this.isActive) return;
        // drain the energy
        if (consumeEnergy(true)) {
            consumeEnergy(false);
        } else {
            if (progressTime >= 2) {
                if (ConfigHolder.machines.recipeProgressLowEnergy) this.progressTime = 1;
                else this.progressTime = Math.max(1, progressTime - 2);
            }
            hasNotEnoughEnergy = true;

            return;
        }

        // increase progress
        progressTime++;
        if (progressTime % goalTime == 0) {
            scanComplete = true;
            mte.finishScan();
            setActive(false);
        }
    }
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }
    public double getProgressPercent() {
        return ((double)progressTime/goalTime);
    }



    public void invalidate() {
        progressTime = 0;
        goalTime = 1;
        mte = null;
        isActive = false;
    }

    public boolean isActive() {
        return isActive && workingEnabled;
    }

    public void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            this.progressTime = 0;
            this.mte.markDirty();
            World world = this.mte.getWorld();
            if (world != null && !world.isRemote) {
                this.mte.writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(active));
            }
        }
    }
    public void setWorkingEnabled(boolean isEnabled) {
        this.workingEnabled = isEnabled;
    }

    protected boolean consumeEnergy(boolean simulate) {
        return mte.drainEnergy(simulate);
    }

    public boolean isScanComplete() {
        return scanComplete;
    }


    /**
     * writes all needed values to NBT
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#writeToNBT(NBTTagCompound)} method
     */
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound data) {
        data.setBoolean("isActive", this.isActive);
        data.setBoolean("isWorkingEnabled", this.workingEnabled);
        data.setBoolean("wasActiveAndNeedsUpdate", this.wasActiveAndNeedsUpdate);
        data.setInteger("progressTime", progressTime);
        data.setInteger("maxProgress", goalTime);
        return data;
    }

    /**
     * reads all needed values from NBT
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#readFromNBT(NBTTagCompound)}
     * method
     */
    public void readFromNBT(@NotNull NBTTagCompound data) {
        this.isActive = data.getBoolean("isActive");
        this.workingEnabled = data.getBoolean("isWorkingEnabled");
        this.wasActiveAndNeedsUpdate = data.getBoolean("wasActiveAndNeedsUpdate");
        this.progressTime = data.getInteger("progressTime");
        this.goalTime = data.getInteger("maxProgress");
    }

    /**
     * writes all needed values to InitialSyncData
     * This MUST be called and returned in the MetaTileEntity's
     * {@link MetaTileEntity#writeInitialSyncData(PacketBuffer)} method
     */
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        buf.writeBoolean(this.isActive);
        buf.writeBoolean(this.workingEnabled);
        buf.writeBoolean(this.wasActiveAndNeedsUpdate);
        buf.writeInt(this.progressTime);
        buf.writeInt(this.goalTime);
    }

    /**
     * reads all needed values from InitialSyncData
     * This MUST be called and returned in the MetaTileEntity's
     * {@link MetaTileEntity#receiveInitialSyncData(PacketBuffer)} method
     */
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        setActive(buf.readBoolean());
        setWorkingEnabled(buf.readBoolean());
        setWasActiveAndNeedsUpdate(buf.readBoolean());
        this.progressTime = buf.readInt();
        this.goalTime = buf.readInt();
    }

    /**
     * reads all needed values from CustomData
     * This MUST be called and returned in the MetaTileEntity's
     * {@link MetaTileEntity#receiveCustomData(int, PacketBuffer)} method
     */
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == GregtechDataCodes.IS_WORKING) {
            setActive(buf.readBoolean());
            mte.scheduleRenderUpdate();
        }
    }

    /**
     * @return whether the cleanroom was active and needs an update
     */
    public boolean wasActiveAndNeedsUpdate() {
        return this.wasActiveAndNeedsUpdate;
    }

    /**
     * set whether the cleanroom was active and needs an update
     *
     * @param wasActiveAndNeedsUpdate the state to set
     */
    public void setWasActiveAndNeedsUpdate(boolean wasActiveAndNeedsUpdate) {
        this.wasActiveAndNeedsUpdate = wasActiveAndNeedsUpdate;
    }

    public long getMaxProgress() {
        return goalTime;
    }


    public long getProgress() {
        return progressTime;
    }

    public int getInfoProviderEUt() {
        return 1536;
    }
}
