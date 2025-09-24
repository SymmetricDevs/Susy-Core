package supersymmetry.api.rocketry.components;

import supersymmetry.common.entities.EntityRocket;

public interface Instrument {

    void act(int count, EntityRocket rocket);
}
