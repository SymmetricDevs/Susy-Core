package supersymmetry.common.covers;

import net.minecraft.util.ResourceLocation;
import supersymmetry.api.SusyLog;
import supersymmetry.common.item.SuSyMetaItems;

import static gregtech.common.covers.CoverBehaviors.registerBehavior;

public class SuSyCoverBehaviors {

    public static void init() {
        SusyLog.logger.info("Registering cover behaviors ...");

        registerBehavior(new ResourceLocation("gregtech", "conveyor.steam"), SuSyMetaItems.CONVEYOR_STEAM,
                (tile, side) -> new SteamCoverConveyor(tile, side, 4));
        registerBehavior(new ResourceLocation("gregtech", "pump.steam"), SuSyMetaItems.PUMP_STEAM,
                (tile, side) -> new SteamCoverPump(tile, side, 640));
        registerBehavior(new ResourceLocation("gregtech", "air_vent"), SuSyMetaItems.AIR_VENT,
                (tile, side) -> new AirVentCover(tile, side, 100));

    }

}
