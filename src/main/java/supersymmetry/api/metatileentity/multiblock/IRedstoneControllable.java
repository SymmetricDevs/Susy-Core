package supersymmetry.api.metatileentity.multiblock;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;

public interface IRedstoneControllable {

    public default boolean redstoneControlEnabled() {
        if (this instanceof MultiblockControllerBase multi) {
            return multi.isStructureFormed();
        } else {
            return true;
        }
    }

    public int getSignalCeiling();

    public default String getSignalTranslationKey(int sig) {
        if (this instanceof MetaTileEntity mte) {
            return mte.getMetaName() + ".signal." + Integer.toString(sig);
        } else {
            return null;
        }
    }

    public void pulse(int sig);
}
