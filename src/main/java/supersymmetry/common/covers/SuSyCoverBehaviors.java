package supersymmetry.common.covers;

import supersymmetry.api.SusyLog;
import supersymmetry.api.util.SuSyUtility;
import supersymmetry.common.item.SuSyMetaItems;

import static gregtech.common.covers.CoverBehaviors.registerBehavior;

public final class SuSyCoverBehaviors {

    private SuSyCoverBehaviors() {
    }

    public static void init() {
        SusyLog.logger.info("Registering cover behaviors ...");

        registerBehavior(SuSyUtility.susyId("conveyor.steam"), SuSyMetaItems.CONVEYOR_STEAM,
                (definition, coverableView, attachedSide) -> new CoverSteamConveyor(definition, coverableView, attachedSide, 4));

        registerBehavior(SuSyUtility.susyId("pump.steam"), SuSyMetaItems.PUMP_STEAM,
                (definition, coverableView, attachedSide) -> new CoverSteamPump(definition, coverableView, attachedSide, 640));

        registerBehavior(SuSyUtility.susyId("air_vent"), SuSyMetaItems.AIR_VENT,
                (definition, coverableView, attachedSide) -> new CoverAirVent(definition, coverableView, attachedSide, 100));
    }

}
