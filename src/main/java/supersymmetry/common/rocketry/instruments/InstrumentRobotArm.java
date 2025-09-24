package supersymmetry.common.rocketry.instruments;

import supersymmetry.api.rocketry.components.Instrument;
import supersymmetry.common.entities.EntityRocket;

public class InstrumentRobotArm implements Instrument {

    @Override
    public void act(int count, EntityRocket rocket) {
        rocket.getEntityData().getCompoundTag("rocket").getCompoundTag("instruments").setInteger("robotArm",
                count);
    }
}
