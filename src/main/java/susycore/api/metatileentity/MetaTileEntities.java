package susycore.api.metatileentity;

import gregtech.api.GTValues;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityElectricBlastFurnace;
import net.minecraft.util.ResourceLocation;
import susycore.common.metatileentities.multi.electric.MetaTileEntityMagneticRefrigerator;

import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;

public class MetaTileEntities {

    public static MetaTileEntityMagneticRefrigerator MAGNETIC_REFRIGERATOR;

    public static void init() {

        MAGNETIC_REFRIGERATOR = registerMetaTileEntity(14500, new MetaTileEntityMagneticRefrigerator(susyId("magnetic_refrigerator")));

    }

    private static ResourceLocation susyId(String name) {
        return new ResourceLocation(GTValues.MODID, name);
    }

}
