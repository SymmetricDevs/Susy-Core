package supersymmetry.api.stockinteraction;

import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.util.math.Vec3d;

public interface IStockInteractor
{
    //x is width, y is not used, z is depth. no setter since final
    public Vec3d getInteractionArea();

    public void cycleFilter(boolean up);
    public void cycleFilterUp();
    public byte getFilterIndex();
    public Class getFilter();

    public MetaTileEntity GetMetaTileEntity();
}
