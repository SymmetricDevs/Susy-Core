package supersymmetry.integration.pyrotech;

import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;
import static supersymmetry.api.util.SuSyUtility.susyId;
import static supersymmetry.common.metatileentities.SuSyMetaTileEntities.PRIMITIVE_SMELTER;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import gregtech.api.modules.GregTechModule;
import gregtech.integration.IntegrationSubmodule;
import supersymmetry.Supersymmetry;
import supersymmetry.api.SusyLog;
import supersymmetry.common.metatileentities.multi.primitive.MetaTileEntityPrimitiveSmelter;
import supersymmetry.modules.SuSyModules;

@GregTechModule(
                moduleID = SuSyModules.MODULE_PYROTECH,
                containerID = Supersymmetry.MODID,
                modDependencies = "pyrotech",
                name = "SuSy Pyrotech Integration",
                description = "SuSy Pyrotech Integration Module")
public class PyrotechModule extends IntegrationSubmodule {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        SusyLog.logger.info("Pyrotech found. Enabling integration...");
        PRIMITIVE_SMELTER = registerMetaTileEntity(14800,
                new MetaTileEntityPrimitiveSmelter(susyId("primitive_smelter")));
    }
}
