package supersymmetry.common.covers;

import net.minecraft.util.ResourceLocation;
import supersymmetry.api.SusyLog;
import supersymmetry.common.item.SuSyMetaItems;

import static gregtech.common.covers.CoverBehaviors.registerBehavior;

public class SuSyCoverBehaviors {

    public static void init() {
        SusyLog.logger.info("Registering cover behaviors ...");

        registerBehavior(new ResourceLocation("gregtech", "conveyor.steam"), SuSyMetaItems.CONVEYOR_STEAM, (tile, side) -> {
            return new SteamCoverConveyor(tile, side, 4);
        });
        registerBehavior(new ResourceLocation("gregtech", "pump.steam"), SuSyMetaItems.PUMP_STEAM, (tile, side) -> {
            return new SteamCoverPump(tile, side, 640);
        });
    }

}
