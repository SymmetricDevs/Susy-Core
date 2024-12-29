package supersymmetry.api.capability;


import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supersymmetry.api.capability.impl.LockableFluidTank;
import supersymmetry.api.nuclear.fission.ICoolantStats;

public interface ICoolantHandler extends ILockableHandler<Fluid> {

    @Nullable
    ICoolantStats getCoolant();

    void setCoolant(@Nullable ICoolantStats prop);

    @NotNull
    LockableFluidTank getFluidTank();

    @NotNull
    EnumFacing getFrontFacing();
}
