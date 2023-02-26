package supersymmetry.common.metatileentities;

import gregtech.api.GTValues;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.common.metatileentities.multi.electric.MetaTileEntityMagneticRefrigerator;
import supersymmetry.common.metatileentities.multi.electric.MetaTileEntitySinteringOven;

import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;

public class SuSyMetaTileEntities {

    public static MetaTileEntityMagneticRefrigerator MAGNETIC_REFRIGERATOR;
    public static MetaTileEntitySinteringOven SINTERING_OVEN;

    public static void init() {
        MAGNETIC_REFRIGERATOR = registerMetaTileEntity(14500, new MetaTileEntityMagneticRefrigerator(susyId("magnetic_refrigerator")));
        SINTERING_OVEN = registerMetaTileEntity(14501, new MetaTileEntitySinteringOven(susyId("sintering_oven")));
    }

    private static @NotNull ResourceLocation susyId(@NotNull String name) {
        return new ResourceLocation(GTValues.MODID, name);
    }
}
