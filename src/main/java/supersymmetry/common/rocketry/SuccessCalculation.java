package supersymmetry.common.rocketry;

import supersymmetry.api.rocketry.fuels.RocketFuelEntry;
import supersymmetry.api.space.Planetoid;
import supersymmetry.common.entities.EntityAbstractRocket;
import supersymmetry.common.rocketry.rockets.SimpleStagedRocketBlueprint;
import supersymmetry.common.world.SuSyDimensions;
import supersymmetry.common.world.WorldProviderPlanet;

public class SuccessCalculation {

    private double augmentation = 0;
    private static final double k = 1.0;

    public static double getSuccessProbability(double f0, double progress) {
        double xh = -1000.0 / k * Math.log(1.0 - f0);
        return 1.0 - Math.exp(-k * (progress + xh) / 1000.0);
    }



    public static double augmentSuccess(double success, long augmentation) {
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
