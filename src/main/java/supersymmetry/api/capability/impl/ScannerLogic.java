package supersymmetry.api.capability.impl;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.ConfigHolder;
import net.minecraft.world.World;
import supersymmetry.common.metatileentities.single.rocket.MetaTileEntityComponentScanner;

public class ScannerLogic {
    private int progressTime = 0;
    private int goalTime = 0;
    private boolean isActive;
    private boolean workingEnabled;
    private boolean hasNotEnoughEnergy;
    private MetaTileEntity mte;
    public ScannerLogic(MetaTileEntity mte) {
        this.mte = mte;
    }
    public void updateLogic() {
        if (!this.isWorkingEnabled()) return;
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

        if (!this.isActive) setActive(true);

        // increase progress
        progressTime++;
        if (progressTime % goalTime != 0) return;
        progressTime = 0;
    }
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }
    public int getProgressPercent() {
        return (int) ((1.0F * progressTime/goalTime) * 100);
    }

    public void invalidate() {
        progressTime = 0;
        goalTime = 0;
        isActive = false;
    }

    public boolean isActive() {
        return isActive && workingEnabled;
    }

    public void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
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
        return ((MetaTileEntityComponentScanner) mte).drainEnergy(simulate);
    }
}
