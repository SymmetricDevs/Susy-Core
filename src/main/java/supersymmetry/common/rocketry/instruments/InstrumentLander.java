package supersymmetry.common.rocketry.instruments;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;

import supersymmetry.SuSyValues;
import supersymmetry.api.rocketry.components.Instrument;
import supersymmetry.api.space.Planetoid;
import supersymmetry.common.EventHandlers;
import supersymmetry.common.entities.EntityAbstractRocket;
import supersymmetry.common.entities.EntityEarthLandingSystem;
import supersymmetry.common.entities.EntityLander;
import supersymmetry.common.entities.teleporters.DropPodTeleporter;
import supersymmetry.common.event.DimensionRidingSwapData;
import supersymmetry.common.rocketry.RocketConfiguration;
import supersymmetry.common.world.PlanetoidHandler;

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
        List<Entity> passengersQueued = new ArrayList<>();
        for (Entity passenger : rocket.getPassengers()) {
            i++;
            if (EventHandlers.isEntityTravelling(passenger))
                continue;
            if (i > count)
                return;

            passengersQueued.add(passenger);
            if (passengersQueued.size() == 4) {
                i++;
                EventHandlers.travellingPassengers
                        .add(new DimensionRidingSwapData(spawnLander(rocket, config, next, i == 0), passengersQueued));
                passengersQueued.clear();
            }
        }
        if (!passengersQueued.isEmpty())
            EventHandlers.travellingPassengers
                    .add(new DimensionRidingSwapData(spawnLander(rocket, config, next, i == 0), passengersQueued));
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
        Entity dropPod;
        Planetoid p = Planetoid.PLANETOIDS.inverse().get(next.dimension);
        double g = PlanetoidHandler.get(next.dimension) == null ? SuSyValues.G0 :
                PlanetoidHandler.get(next.dimension).gravity * SuSyValues.G0;
        if (p.getSurfacePressure() > 10000 && g > 0.4) {
            dropPod = new EntityEarthLandingSystem(rocket.world, next.landingPos.getX(), 350, next.landingPos.getZ());
        } else {
            dropPod = new EntityLander(rocket.world, next.landingPos.getX(), 350, next.landingPos.getZ());
        }
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
