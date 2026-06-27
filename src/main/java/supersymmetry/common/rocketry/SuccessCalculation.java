package supersymmetry.common.rocketry;

public class SuccessCalculation {

    public static final double ESCAPE_VELOCITY_CONSTANT = 1138.92;
    public static final double AUGMENTATION_CONSTANT = 19480;
    // A 50% success blueprint takes just under 5 hours at this rate to bring to 80% for a starting-tier AFS

    public static double augmentSuccess(double success, long augmentation) {
        success = Math.max(0.0001, success);
        double inverseSigmoid = Math.log(success / (1 - success));
        inverseSigmoid += success * Math.log(augmentation / AUGMENTATION_CONSTANT + 1);
        return 1 / (1 + Math.exp(-inverseSigmoid));
    }

    public enum LaunchResult {
        DOES_NOT_LAUNCH,
        LAUNCHES,
        CRASHES,
        EXPLODES
    }

    public record AFSStats(double successCalculation, long augmentation,
                           double mass, double deltaV, double escapeVelocity, double cargoCapacity,
                           double radialInstability, double thrust) {

    }
}
