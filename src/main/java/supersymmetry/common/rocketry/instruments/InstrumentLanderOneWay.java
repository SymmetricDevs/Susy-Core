package supersymmetry.common.rocketry.instruments;

import java.util.Collections;

import supersymmetry.common.EventHandlers;
import supersymmetry.common.entities.EntityAbstractRocket;
import supersymmetry.common.event.DimensionRidingSwapData;
import supersymmetry.common.rocketry.RocketConfiguration;

public class InstrumentLanderOneWay extends InstrumentLander {

    @Override
    public void act(int count, EntityAbstractRocket rocket) {
        RocketConfiguration oldConfig = rocket.getRocketConfiguration();
        RocketConfiguration.MissionConfiguration next = getNextLanderConfig(oldConfig);
        if (next == null)
            return;
        RocketConfiguration config = RocketConfiguration.empty();
        if (rocket.getPassengers().isEmpty()) {
            spawnLander(rocket, config, next, true);
            return;
        }

        if (count >= 1) {
            EventHandlers.travellingPassengers
                    .add(new DimensionRidingSwapData(spawnLander(rocket, config, next, true), Collections.emptyList()));
        }
    }
}
