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
        RocketConfiguration oldConfig = rocket.getRocketConfiguration();
        RocketConfiguration.MissionConfiguration next = getNextLanderConfig(oldConfig);
        RocketConfiguration config = oldConfig.clipAt(next);
        if (next == null)
            return;
        if (rocket.getPassengers().isEmpty()) {
            spawnLander(rocket, config, next, true);
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
                    .add(new DimensionRidingSwapData(spawnLander(rocket, config, next, i == 0), passenger));
        }
    }

    public static RocketConfiguration.MissionConfiguration getNextLanderConfig(
                                                                               RocketConfiguration config) {
        for (RocketConfiguration.MissionConfiguration mission : config.getMissions()) {
            if (mission.destinationType == RocketConfiguration.DestinationType.Landing) {
                return mission;
            }
        }
        return null;
    }

    public static Entity spawnLander(EntityAbstractRocket rocket, RocketConfiguration config,
                                     RocketConfiguration.MissionConfiguration next,
                                     boolean withCargo) {
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
