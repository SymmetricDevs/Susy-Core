package supersymmetry.modules;

import gregtech.api.modules.GregTechModule;
import gregtech.api.modules.IGregTechModule;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import supersymmetry.Supersymmetry;
import supersymmetry.api.SusyLog;

@GregTechModule(
        moduleID = SuSyModules.MODULE_CORE,
        containerID = Supersymmetry.MODID,
        name = "SuSy Core",
        description = "Core module of SuSy Core, so this should call SuSy Core Core ngl.",
        coreModule = true)
public class SuSyCoreModule implements IGregTechModule {

    @Override
    public @NotNull Logger getLogger() {
        return SusyLog.logger;
    }

    @Override
    public void registerPackets() {
    }
}
