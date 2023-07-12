package supersymmetry.api.stockinteraction;

import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.util.math.Vec3d;

public interface IStockInteractor
{
    //x is width, y is not used, z is depth. no setter since final
    Vec3d getInteractionArea();

    void cycleFilter(boolean up);
    void cycleFilterUp();
    byte getFilterIndex();
    boolean setFilterIndex(byte index);
    Class<?> getFilter();

    MetaTileEntity GetMetaTileEntity();
}
