package supersymmetry.api.rocketry.components;

import supersymmetry.api.rocketry.NozzleFlow;

public interface RocketEngine {

    /** Choked mass flow through the throat, in kg/s. */
    double getFuelThroughput();

    /** Exit area over throat area, set by how far the bell was allowed to open up. */
    double getAreaRatio();

    /** Chamber stagnation pressure the pumps hold against the throat, in Pa. */
    double getChamberPressure();

    /**
     * Wall half-angle at the lip, in radians. Summarised out of the layer-by-layer
     * scan so the engine does not have to carry its whole contour around.
     */
    double getExitHalfAngle();

    /** Wetted wall area over throat area, the drag half of that summary. */
    double getWettedAreaRatio();

    /** Total wall slope change down the bell, the smoothness half of it. */
    double getContourTurning();

    /** Other factors that multiply the final thrust without affecting throughput. */
    double getEfficiency();

    /**
     * Fraction of the fuel's rated specific impulse this particular nozzle
     * delivers against the given ambient pressure: how far it expands, and how
     * well it was shaped getting there. See {@link NozzleFlow} for what the
     * reference nozzle is.
     */
    default double getNozzleEfficiency(double ambientPressure) {
        return NozzleFlow.nozzleEfficiency(getAreaRatio(), getChamberPressure(), ambientPressure) *
                NozzleFlow.contourEfficiency(getExitHalfAngle(), getWettedAreaRatio(), getContourTurning());
    }
}
