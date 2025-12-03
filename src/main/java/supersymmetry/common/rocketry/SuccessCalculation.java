package supersymmetry.common.rocketry;

import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.common.entities.EntityAbstractRocket;
import supersymmetry.common.world.SuSyDimensions;
import supersymmetry.common.world.WorldProviderPlanet;

public class SuccessCalculation {

    private final AbstractRocketBlueprint blueprint;
    private double augmentation = 0;

    public SuccessCalculation(AbstractRocketBlueprint blueprint) {
        this.blueprint = blueprint;
    }

    public LaunchResult calculateSuccess(EntityAbstractRocket rocket) {
        double success = 1;
        // Thrust to weight ratio
        double gravMult = 1;
        if (rocket.world.provider instanceof WorldProviderPlanet) {
            gravMult = SuSyDimensions.PLANETS.get(rocket.world.provider.getDimension()).gravity;
        }
        double weight = blueprint.getMass() * gravMult;
        double thrust = blueprint.getThrust(null, gravMult);
        double thrustToWeightRatio = thrust / weight;

        if (thrustToWeightRatio < 1) {
            return LaunchResult.DOES_NOT_LAUNCH;
        } else {
            success *= (1 - (0.5 * Math.exp(1 - thrustToWeightRatio)));
        }

        // Oblateness (height / radius)
        //blueprint.getMaxRadius() / blueprint.getHeight();


        if (Math.random() < success) {
            return LaunchResult.LAUNCHES;
        } else {
            return LaunchResult.DOES_NOT_LAUNCH;
        }
    }

    public enum LaunchResult {
        DOES_NOT_LAUNCH,
        LAUNCHES,
        CRASHES,
        TROLLS
    }
}
