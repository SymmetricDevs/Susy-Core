package supersymmetry.common.rocketry.instruments;

import net.minecraft.entity.Entity;

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
            spawnLander(rocket, config, next,true);
            return;
        }

        int i = 0;
        for (Entity passenger : rocket.getPassengers()) {
            i++;
            if (EventHandlers.isEntityTravelling(passenger))
                continue;
            if (i > count)
                break;

            EventHandlers.travellingPassengers
                    .add(new DimensionRidingSwapData(spawnLander(rocket, config, next,i == 0), passenger));
        }
    }
}
