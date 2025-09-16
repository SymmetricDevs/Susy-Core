package supersymmetry.common.rocketry.instruments;

import gregtech.api.util.TeleportHandler;
import net.minecraft.entity.Entity;
import supersymmetry.api.rocketry.components.Instrument;
import supersymmetry.common.EventHandlers;
import supersymmetry.common.entities.EntityDropPod;
import supersymmetry.common.entities.EntityRocket;
import supersymmetry.common.entities.teleporters.DropPodTeleporter;
import supersymmetry.common.event.DimensionRidingSwapData;

public class InstrumentLander implements Instrument {
    public void act(int count, EntityRocket rocket) {
        int i = 0;
        for (Entity passenger : rocket.getPassengers()) {
            i++;
            if (i > count) break;
            EntityDropPod dropPod = new EntityDropPod(rocket.world, passenger.posX, passenger.posY, passenger.posZ);
            TeleportHandler.teleport(dropPod, 800, new DropPodTeleporter(), rocket.posX, rocket.posY, rocket.posZ);

            EventHandlers.travellingPassengers.add(new DimensionRidingSwapData(dropPod, passenger));
        }
    }
}
