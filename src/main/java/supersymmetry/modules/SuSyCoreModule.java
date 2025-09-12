package supersymmetry.modules;

import gregtech.api.GregTechAPI;
import gregtech.api.modules.GregTechModule;
import gregtech.api.modules.IGregTechModule;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import supersymmetry.Supersymmetry;
import supersymmetry.api.SusyLog;
import supersymmetry.common.network.CPacketRocketInteract;
import supersymmetry.common.network.SPacketFirstJoin;
import supersymmetry.common.network.SPacketRemoveFluidState;
import supersymmetry.common.network.SPacketUpdateBlockRendering;
import supersymmetry.common.network.SPacketUpdateRenderMask;

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
        GregTechAPI.networkHandler.registerPacket(SPacketUpdateBlockRendering.class);
        GregTechAPI.networkHandler.registerPacket(CPacketRocketInteract.class);
        GregTechAPI.networkHandler.registerPacket(SPacketUpdateRenderMask.class);
    }
}
