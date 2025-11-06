package supersymmetry.api.metatileentity.multiblock;

import java.util.List;

import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;

public interface IRedstoneControllable {

    public default boolean redstoneControlEnabled() {
        if (this instanceof MultiblockControllerBase multi) {
            return multi.isStructureFormed();
        } else {
            return true;
        }
    }

    public default int getSignalCeiling() {
        return this.getSignals().size() - 1;
    }

    public String getSignalName(int sig);

    public List<String> getSignals();

    public void pulse(int sig);
}
