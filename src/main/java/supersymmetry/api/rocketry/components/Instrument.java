package supersymmetry.api.rocketry.components;

import supersymmetry.common.entities.EntityAbstractRocket;

public interface Instrument {

    void act(int count, EntityAbstractRocket rocket);
}
