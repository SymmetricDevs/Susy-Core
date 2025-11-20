package supersymmetry.api.capability;

public interface IRotationSpeedHandler {

    /**
     * @returns the rotation speed in RPM
     */
    int getRotationSpeed();

    /**
     * @returns the maximum rotation speed in RPM
     */
    int getMaxRotationSpeed();

    /**
     * @returns the fraction of the max rotation speed for power generation [0.0 - 1.0]
     */
    default long getRotationSpeedFraction() {
        return getRotationSpeed() / (long) (getMaxRotationSpeed());
    }

    /**
     * @returns the acceleration rate in RPM/t.
     */
    int getRotationAcceleration();

    /**
     * @returns the deceleration rate in RPM/t.
     */
    int getRotationDeceleration();
}
