package supersymmetry.common.rocketry.instruments;

import net.minecraft.entity.Entity;

import gregtech.api.util.TeleportHandler;
import supersymmetry.api.rocketry.components.Instrument;
import supersymmetry.common.EventHandlers;
import supersymmetry.common.entities.EntityAbstractRocket;
import supersymmetry.common.entities.EntityLander;
import supersymmetry.common.entities.teleporters.DropPodTeleporter;
import supersymmetry.common.event.DimensionRidingSwapData;
import supersymmetry.common.rocketry.RocketConfiguration;

public class InstrumentLander implements Instrument {

    public void act(int count, EntityAbstractRocket rocket) {
        RocketConfiguration config = rocket.getRocketConfiguration();
        RocketConfiguration.MissionConfiguration next = config.popFront();
        while (!config.isEmpty() && next.missionType != RocketConfiguration.MissionType.Manned) {
            next = config.popFront();
        }
        if (next.missionType != RocketConfiguration.MissionType.Manned) {
            return;
        }
        int i = 0;
        for (Entity passenger : rocket.getPassengers()) {
            i++;
            if (EventHandlers.isEntityTravelling(passenger)) continue;
            if (i > count) break;
            EntityLander dropPod = new EntityLander(rocket.world, next.landingPos.getX(), 350, next.landingPos.getZ());
            if (i == 0) {
                dropPod.setInventory(rocket.getInventory());
            }

            // Pop the next mission from the rocket configuration
            dropPod.getEntityData().setTag(EntityAbstractRocket.ROCKET_CONFIG_KEY, config.serialize());
            // Cannot use TeleportHandler here because it doesn't get the new entity
            Entity teleported = dropPod.changeDimension(next.dimension, new DropPodTeleporter());
            teleported.forceSpawn = true;
            EventHandlers.travellingPassengers.add(new DimensionRidingSwapData(teleported, passenger));
        }
    }
}
