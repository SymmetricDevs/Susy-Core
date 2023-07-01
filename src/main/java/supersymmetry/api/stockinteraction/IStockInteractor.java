package supersymmetry.api.stockinteraction;

import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.util.math.Vec3d;

public interface IStockInteractor
{
    //x is width, y is not used, z is depth;
    public void SetInteractionArea(Vec3d area);
    public Vec3d GetInteractionArea();

    public void SetFilterClass(String clazz);
    public String GetFilterClass();

    public void SetUsingFilter(boolean usingFilter);
    public boolean GetUsingFilter();
    public MetaTileEntity GetMetaTileEntity();
}
