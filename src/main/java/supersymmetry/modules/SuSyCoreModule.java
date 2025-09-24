package supersymmetry.modules;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import gregtech.api.GregTechAPI;
import gregtech.api.modules.GregTechModule;
import gregtech.api.modules.IGregTechModule;
import supersymmetry.Supersymmetry;
import supersymmetry.api.SusyLog;
import supersymmetry.common.network.SPacketFirstJoin;
import supersymmetry.common.network.SPacketRemoveFluidState;

@GregTechModule(
                moduleID = SuSyModules.MODULE_CORE,
                containerID = Supersymmetry.MODID,
                name = "SuSy Core",
                description = "Core module of SuSy Core, so this should be called SuSy Core Core ngl.",
                coreModule = true)
public class SuSyCoreModule implements IGregTechModule {

    @Override
    public @NotNull Logger getLogger() {
        return SusyLog.logger;
    }

    @Override
    public void registerPackets() {
        GregTechAPI.networkHandler.registerPacket(SPacketRemoveFluidState.class);
        GregTechAPI.networkHandler.registerPacket(SPacketFirstJoin.class);
        getLogger().info("Hey");
    }
}
