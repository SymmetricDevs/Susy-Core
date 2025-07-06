package supersymmetry.integration.pyrotech;

import gregtech.api.modules.GregTechModule;
import gregtech.integration.IntegrationSubmodule;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import supersymmetry.Supersymmetry;
import supersymmetry.api.SusyLog;
import supersymmetry.common.metatileentities.multi.primitive.MetaTileEntityPrimitiveSmelter;
import supersymmetry.common.metatileentities.multiblockpart.MetaTileEntityPrimitiveItemBus;
import supersymmetry.modules.SuSyModules;

import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;
import static supersymmetry.common.metatileentities.SuSyMetaTileEntities.*;

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
        PRIMITIVE_SMELTER = registerMetaTileEntity(14800, new MetaTileEntityPrimitiveSmelter(susyId("primitive_smelter")));
        PRIMITIVE_ITEM_IMPORT = registerMetaTileEntity(14801, new MetaTileEntityPrimitiveItemBus(susyId("primitive_item_import"), false));
        PRIMITIVE_ITEM_EXPORT = registerMetaTileEntity(14802, new MetaTileEntityPrimitiveItemBus(susyId("primitive_item_export"), true));
    }
}
