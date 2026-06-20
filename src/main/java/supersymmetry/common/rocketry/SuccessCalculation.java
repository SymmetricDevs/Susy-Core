package supersymmetry.common.rocketry;

public class SuccessCalculation {

    public static final double ESCAPE_VELOCITY_CONSTANT = 1138.92;

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
        EXPLODES
    }
}
