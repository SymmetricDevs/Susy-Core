package supersymmetry.common.rocketry.instruments;

import net.minecraft.entity.Entity;

import org.jetbrains.annotations.Nullable;

import supersymmetry.api.rocketry.components.Instrument;
import supersymmetry.common.EventHandlers;
import supersymmetry.common.entities.EntityAbstractRocket;
import supersymmetry.common.entities.EntityLander;
import supersymmetry.common.entities.teleporters.DropPodTeleporter;
import supersymmetry.common.event.DimensionRidingSwapData;
import supersymmetry.common.rocketry.RocketConfiguration;

public class InstrumentLander implements Instrument {

    public void act(int count, EntityAbstractRocket rocket) {
        RocketConfiguration.MissionConfiguration next = getMissionConfiguration(rocket);
        if (next == null) return;
        if (rocket.getPassengers().isEmpty()) {
            spawnLander(rocket, next, true);
            return;
        }

        int i = 0;
        for (Entity passenger : rocket.getPassengers()) {
            i++;
            if (EventHandlers.isEntityTravelling(passenger)) continue;
            if (i > count) break;

            EventHandlers.travellingPassengers.add(new DimensionRidingSwapData(
                    spawnLander(rocket, next, i == 0), passenger));
        }
    }

    public static RocketConfiguration.@Nullable MissionConfiguration getMissionConfiguration(EntityAbstractRocket rocket) {
        RocketConfiguration config = rocket.getRocketConfiguration();
        if (config.isEmpty()) {
            return null;
        }
        RocketConfiguration.MissionConfiguration next = config.popFront();
        while (!config.isEmpty() && next.missionType == RocketConfiguration.MissionType.UnmannedCollection) {
            next = config.popFront();
        }
        if (next.missionType == RocketConfiguration.MissionType.UnmannedCollection) {
            return null;
        }
        return next;
    }

    public static Entity spawnLander(EntityAbstractRocket rocket, RocketConfiguration.MissionConfiguration next,
                                     boolean withCargo) {
        EntityLander dropPod = new EntityLander(rocket.world, next.landingPos.getX(), 350, next.landingPos.getZ());

        // Use the config with a popped mission
        // Cannot use TeleportHandler here because it doesn't get the new entity
        Entity teleported = dropPod.changeDimension(next.dimension, new DropPodTeleporter());
        teleported.forceSpawn = true;
        if (withCargo && teleported instanceof EntityLander lander) {
            lander.setInventory(rocket.getInventory());
        }
        teleported.getEntityData().setTag(EntityAbstractRocket.ROCKET_CONFIG_KEY, next.serialize());
        return teleported;
    }
}
