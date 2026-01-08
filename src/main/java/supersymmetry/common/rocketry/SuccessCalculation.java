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

    // lobotomized version of the function bellow to only take in the blueprint
    public double calculateInitialSuccess(double gravity) {
        double success = 1;
        double weight = blueprint.getMass();
        // TODO: AFS gravity selection
        double thrust = blueprint.getThrust(null, gravity / 9.81, "engine");
        double thrustToWeightRatio = thrust / weight;
        if (thrustToWeightRatio < 1) return 0d;

        success *= (1 - (0.5 * Math.exp(1 - thrustToWeightRatio)));
        double oblateness = blueprint.getHeight() / blueprint.getMaxRadius();
        success *= (1 - (0.1 * Math.exp(-oblateness)));
        success *= Math.pow(0.995, blueprint.getComponentCount("engine"));
        success *= (1 - (0.5 * Math.exp(blueprint.getTotalRadiusMismatch() / 10)));

        double smallThrust = blueprint.getThrust(null, gravity / 9.81, "small_engine");
        if (smallThrust == 0) {
            return 0;
        }
        if (thrust / smallThrust > 10) {
            success *= (1 - (0.2 * Math.exp((thrust / smallThrust) - 10)));
        } else if (thrust / smallThrust < 3) {
            success *= (1 - (0.5 * Math.exp(3 - (thrust / smallThrust))));
        }

        success = augmentSuccess(success);
        return success;
    }

    public LaunchResult calculateSuccess(EntityAbstractRocket rocket) {
        double success = 1;
        // Thrust to weight ratio
        double gravMult = 1;
        if (rocket.world.provider instanceof WorldProviderPlanet) {
            gravMult = SuSyDimensions.PLANETS.get(rocket.world.provider.getDimension()).gravity;
        }
        double weight = (blueprint.getMass() + rocket.getCargoMass()) * gravMult;
        double thrust = blueprint.getThrust(null, gravMult, "engine");
        double thrustToWeightRatio = thrust / weight;

        if (thrustToWeightRatio < 1) {
            return LaunchResult.DOES_NOT_LAUNCH;
        } else {
            success *= (1 - (0.5 * Math.exp(1 - thrustToWeightRatio)));
        }

        // Oblateness (height / radius)
        double oblateness = blueprint.getHeight() / blueprint.getMaxRadius();
        success *= (1 - (0.1 * Math.exp(-oblateness)));

        // Number of engines, radius mismatch
        success *= Math.pow(0.995, blueprint.getComponentCount("engine"));
        success *= (1 - (0.5 * Math.exp(blueprint.getTotalRadiusMismatch() / 10)));

        // Small engines shouldn't have that much throughput
        double smallThrust = blueprint.getThrust(null, gravMult, "small_engine");
        if (smallThrust == 0) {
            return LaunchResult.CRASHES;
        }
        if (thrust / smallThrust > 10) {
            success *= (1 - (0.2 * Math.exp((thrust / smallThrust) - 10)));
        } else if (thrust / smallThrust < 3) {
            success *= (1 - (0.5 * Math.exp(3 - (thrust / smallThrust))));
        }

        // TODO: Guidance computer

        success = augmentSuccess(success);

        if (Math.random() < success) {
            return LaunchResult.LAUNCHES;
        } else {
            return LaunchResult.DOES_NOT_LAUNCH;
        }
    }

    private double augmentSuccess(double success) {
        success = Math.min(0.0001, success);
        double inverseSigmoid = Math.log(success / (1 - success));
        inverseSigmoid += success * augmentation;
        return 1 / (1 + Math.exp(-inverseSigmoid));
    }

    public enum LaunchResult {
        DOES_NOT_LAUNCH,
        LAUNCHES,
        CRASHES,
        TROLLS
    }
}
