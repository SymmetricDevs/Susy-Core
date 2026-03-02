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
        int i = 0;
        for (Entity passenger : rocket.getPassengers()) {
            i++;
            if (i > count) break;
            EntityLander dropPod = new EntityLander(rocket.world, passenger.posX, passenger.posY, passenger.posZ);
            if (i == 0) {
                dropPod.setInventory(rocket.getInventory());
            }

            // Pop the next mission from the rocket configuration
            RocketConfiguration config = rocket.getRocketConfiguration();
            config.popFront();
            dropPod.getEntityData().setTag(EntityAbstractRocket.ROCKET_CONFIG_KEY, config.serialize());

            TeleportHandler.teleport(dropPod, 800, new DropPodTeleporter(), rocket.posX, rocket.posY, rocket.posZ);

            EventHandlers.travellingPassengers.add(new DimensionRidingSwapData(dropPod, passenger));
        }
    }
}
