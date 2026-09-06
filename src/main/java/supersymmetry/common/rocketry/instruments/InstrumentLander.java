package supersymmetry.common.rocketry.instruments;

import net.minecraft.entity.Entity;

import supersymmetry.api.rocketry.components.Instrument;
import supersymmetry.common.EventHandlers;
import supersymmetry.common.entities.EntityAbstractRocket;
import supersymmetry.common.entities.EntityLander;
import supersymmetry.common.entities.teleporters.DropPodTeleporter;
import supersymmetry.common.event.DimensionRidingSwapData;
import supersymmetry.common.rocketry.RocketConfiguration;

public class InstrumentLander implements Instrument {

    public void act(int count, EntityAbstractRocket rocket) {
        RocketConfiguration config = getMissionConfiguration(rocket);
        if (config.isEmpty())
            return;
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

    public static RocketConfiguration getMissionConfiguration(
                                                              EntityAbstractRocket rocket) {
        RocketConfiguration config = rocket.getRocketConfiguration();
        if (config.isEmpty()) {
            return config;
        }
        RocketConfiguration.MissionConfiguration next = config.popFront();
        while (!config.isEmpty() && next.destinationType != RocketConfiguration.DestinationType.Landing) {
            next = config.popFront();
        }
        return config;
    }

    public static Entity spawnLander(EntityAbstractRocket rocket, RocketConfiguration config,
                                     boolean withCargo) {
        RocketConfiguration.MissionConfiguration next = config.popFront();
        EntityLander dropPod = new EntityLander(rocket.world, next.landingPos.getX(), 350, next.landingPos.getZ());

        // Use the config with a popped mission
        // Cannot use TeleportHandler here because it doesn't get the new entity
        Entity teleported = dropPod.changeDimension(next.dimension, new DropPodTeleporter());
        teleported.forceSpawn = true;
        if (withCargo && teleported instanceof EntityLander lander) {
            lander.setInventory(rocket.getInventory());
        }
        teleported.getEntityData().setTag(EntityAbstractRocket.ROCKET_CONFIG_KEY, config.serialize()); // Rest
        return teleported;
    }
}
