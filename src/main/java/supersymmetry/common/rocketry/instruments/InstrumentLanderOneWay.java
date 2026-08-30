package supersymmetry.common.rocketry.instruments;

import net.minecraft.entity.Entity;

import supersymmetry.common.EventHandlers;
import supersymmetry.common.entities.EntityAbstractRocket;
import supersymmetry.common.event.DimensionRidingSwapData;
import supersymmetry.common.rocketry.RocketConfiguration;

public class InstrumentLanderOneWay extends InstrumentLander {

    @Override
    public void act(int count, EntityAbstractRocket rocket) {
        RocketConfiguration config = getMissionConfiguration(rocket);
        if (config.isEmpty())
            return;
        config.truncate();
        if (rocket.getPassengers().isEmpty()) {
            spawnLander(rocket, config, true);
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
                    .add(new DimensionRidingSwapData(spawnLander(rocket, config, i == 0), passenger));
        }
    }
}
