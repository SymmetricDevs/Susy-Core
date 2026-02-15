package supersymmetry.common.rocketry;

import supersymmetry.api.rocketry.fuels.RocketFuelEntry;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.api.space.Planetoid;
import supersymmetry.common.entities.EntityAbstractRocket;
import supersymmetry.common.world.SuSyDimensions;
import supersymmetry.common.world.WorldProviderPlanet;

public class SuccessCalculation {

    private final AbstractRocketBlueprint blueprint;
    private double augmentation = 0;

    public SuccessCalculation(AbstractRocketBlueprint blueprint) {
        this.blueprint = blueprint;
    }

    // lobotomized version of the function below to only take in the blueprint
    public double calculateInitialSuccess(double gravity, RocketFuelEntry fuel) {
        double success = 1;
        double weight = blueprint.getMass();
        double thrust = blueprint.getThrust(null, gravity / 9.81, "engine");
        double thrustToWeightRatio = thrust / weight;
        if (thrustToWeightRatio < 1) return 0d;

        // TODO: somehow incorporate cargo mass in a fair way
        double velocitySpeedup = blueprint.getEffectiveFuelVelocity(null, gravity / 9.81, "engine") *
                Math.log((fuel.getDensity() * blueprint.getFuelVolume() + blueprint.getMass())
                        / blueprint.getMass());

        // Very approximate, assuming constant density rho = 5515 kg/m^3
        // g = GM / R^2
        // g = GR * 4/3pi * rho
        // R = 3/(4G * rho * pi) * g
        // escape velocity = sqrt(2gR) = g * sqrt(3/2Grhopi)
        double escapeVelocity = 1138 * gravity;

        if (velocitySpeedup < escapeVelocity) {
            return 0;
        } else {
            success *= (1 - (0.1 * Math.exp(10 * (escapeVelocity - velocitySpeedup) / escapeVelocity)));
        }

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
        success *= 0.9;

        success = augmentSuccess(success);
        return success;
    }

    public LaunchResult calculateSuccess(EntityAbstractRocket rocket) {
        double success = 1;
        // Thrust to weight ratio
        double gravMult = 1;
        double escapeVelocity = 11000;
        if (rocket.world.provider instanceof WorldProviderPlanet) {
            gravMult = SuSyDimensions.PLANETS.get(rocket.world.provider.getDimension()).gravity;
        }
        if (Planetoid.PLANETOIDS.containsValue(rocket.world.provider.getDimension())) {
            escapeVelocity = Planetoid.PLANETOIDS.inverse().get(rocket.world.provider.getDimension()).getEscapeVelocity();
        }
        double weight = blueprint.getMass() * gravMult;
        double thrust = blueprint.getThrust(rocket.getFuel(), gravMult, "engine");
        double thrustToWeightRatio = thrust / weight;

        if (thrustToWeightRatio < 1) {
            return LaunchResult.DOES_NOT_LAUNCH;
        } else {
            success *= (1 - (0.5 * Math.exp(1 - thrustToWeightRatio)));
        }

        // Tsiolkovsky rocket equation
        double velocitySpeedup = blueprint.getEffectiveFuelVelocity(rocket.getFuel(), gravMult, "engine") *
                Math.log((rocket.getFuel().getDensity() * blueprint.getFuelVolume() + blueprint.getMass())
                    / (blueprint.getMass()));

        if (velocitySpeedup < escapeVelocity) {
            return LaunchResult.CRASHES;
        } else {
            success *= (1 - (0.1 * Math.exp(10 * (escapeVelocity - velocitySpeedup) / escapeVelocity)));
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

        // Guidance system TODO: make this more complex when more guidance systems are added
        success *= 0.9;

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
