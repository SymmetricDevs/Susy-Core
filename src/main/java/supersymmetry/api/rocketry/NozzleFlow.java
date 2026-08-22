package supersymmetry.api.rocketry;

/**
 * Choked-flow model for the de Laval nozzles players build out of blocks.
 * <p>
 * A converging-diverging nozzle chokes at the throat, so the mass flow the
 * engine can swallow is set by the throat area and the chamber pressure alone:
 *
 * <pre>
 * mdot = A_t * p_0 * sqrt(gamma / (R_specific * T_0)) * (2 / (gamma + 1))^((gamma + 1) / (2 * (gamma - 1)))
 * </pre>
 *
 * which is usually written {@code mdot = A_t * p_0 / c*}, with the
 * characteristic velocity {@code c*} rolling up everything that depends on the
 * propellant. The diverging section past the throat does not enter here at all;
 * it sets the exhaust velocity, not the flow rate.
 * <p>
 * We do not have per-fuel combustion products, so {@code c*} is a single
 * constant built from a generic hot exhaust ({@link #GAMMA},
 * {@link #CHAMBER_TEMPERATURE}, {@link #EXHAUST_MOLAR_MASS}). It lands at about
 * 1720 m/s, which is roughly where real kerolox and methalox engines sit.
 * <p>
 * Chamber pressure is not free either: it is whatever the turbopumps can hold
 * against the throat. A pump is treated as a constant-power machine, so its
 * pressure rise falls as the flow through it rises
 * ({@code mdot * dp = rated flow * }{@link #PUMP_RATED_PRESSURE}). Solving that
 * against the choked-flow relation gives {@link #equilibriumChamberPressure}. A
 * wider throat therefore buys more flow but at a lower chamber pressure, and
 * flow only grows with the square root of throat area - which is why real
 * engines add pumps rather than just drilling the throat out.
 */
public final class NozzleFlow {

    private NozzleFlow() {}

    /** Universal gas constant, in J/(mol*K). */
    public static final double GAS_CONSTANT = 8.31446;

    /**
     * Ratio of specific heats of the exhaust. Real rocket exhausts run somewhere
     * between 1.14 and 1.26 depending on the mixture; 1.2 is the usual stand-in.
     */
    public static final double GAMMA = 1.2;

    /** Chamber flame temperature, in K. Typical of a decent bipropellant. */
    public static final double CHAMBER_TEMPERATURE = 3300.0;

    /** Mean molar mass of the exhaust, in kg/mol. Water-and-CO2 heavy mixtures. */
    public static final double EXHAUST_MOLAR_MASS = 0.022;

    /**
     * Fraction of the pump's pressure rise that survives the injector plate. Real
     * injectors are sized to eat 15-25% of it, which is what keeps combustion
     * instability from feeding back into the feed system.
     */
    public static final double INJECTOR_RECOVERY = 0.8;

    /**
     * Discharge pressure a turbopump reaches while running at its rated mass flow,
     * in Pa. 10 MPa is an ordinary staged-combustion-era number and, together with
     * the rated flow on the block itself, fixes how much pump power one block is
     * worth.
     */
    public static final double PUMP_RATED_PRESSURE = 1.0e7;

    /**
     * The Vandenkerckhove function, the purely gamma-dependent part of the choked
     * mass flow.
     */
    public static final double VANDENKERCKHOVE = Math.sqrt(GAMMA) *
            Math.pow(2 / (GAMMA + 1), (GAMMA + 1) / (2 * (GAMMA - 1)));

    /** Characteristic velocity c*, in m/s. */
    public static final double CHARACTERISTIC_VELOCITY = Math
            .sqrt(GAS_CONSTANT * CHAMBER_TEMPERATURE / EXHAUST_MOLAR_MASS) / VANDENKERCKHOVE;

    /**
     * Expansion ratio the specific impulse quoted on a fuel is taken at. A nozzle
     * built to exactly this ratio, fired into vacuum, delivers the advertised
     * number; anything else is scaled off it by {@link #nozzleEfficiency}.
     */
    public static final double REFERENCE_AREA_RATIO = 40.0;

    /**
     * Summerfield criterion. Push a nozzle far enough past ambient and the flow
     * tears off the wall rather than following it down; the bell past that point
     * stops counting. Without this an absurdly large bell fired at sea level would
     * produce negative thrust.
     */
    public static final double SEPARATION_PRESSURE_RATIO = 0.4;

    /** Pressure ratio at the throat itself, where the flow goes sonic. */
    public static final double THROAT_PRESSURE_RATIO = Math.pow(2 / (GAMMA + 1), GAMMA / (GAMMA - 1));

    /** The part of the momentum thrust term that only depends on gamma. */
    private static final double MOMENTUM_COEFFICIENT = Math.sqrt(
            (2 * GAMMA * GAMMA / (GAMMA - 1)) * Math.pow(2 / (GAMMA + 1), (GAMMA + 1) / (GAMMA - 1)));

    /** Thrust coefficient of the reference nozzle in vacuum. */
    public static final double REFERENCE_THRUST_COEFFICIENT = thrustCoefficient(REFERENCE_AREA_RATIO, 1.0, 0.0);

    /**
     * Skin friction charged per unit of wall area, as a fraction of throat area.
     * Set so a conventional nozzle gives up about 1.5% of its thrust to drag on
     * the bell, which is where real ones sit.
     */
    public static final double FRICTION_LOSS_COEFFICIENT = 1.0e-4;

    /**
     * Charged per unit of total wall turning. Supersonic flow only turns outward
     * for free; every time the wall bends back toward the axis it throws a
     * compression wave, and a contour that does that in a few violent steps pays
     * far more than one that eases round. Set so a properly drawn bell loses about
     * 1%.
     */
    public static final double TURNING_LOSS_COEFFICIENT = 0.02;

    /** Lip angle of the reference bell, in radians. */
    public static final double REFERENCE_EXIT_HALF_ANGLE = Math.toRadians(8);

    /** Wall area over throat area for the reference bell. */
    public static final double REFERENCE_WETTED_AREA_RATIO = 150.0;

    /** Total wall turning of the reference bell: about 35 degrees down to 8. */
    public static final double REFERENCE_CONTOUR_TURNING = 0.56;

    /** What the reference contour scores before anything is normalised against it. */
    public static final double REFERENCE_CONTOUR_EFFICIENCY = rawContourEfficiency(REFERENCE_EXIT_HALF_ANGLE,
            REFERENCE_WETTED_AREA_RATIO, REFERENCE_CONTOUR_TURNING);

    /**
     * Physical radius of a nozzle station, in m. Welzl measures between block
     * centres, so the half block of clearance on either side is added back: a
     * single air block is a 1 m bore.
     */
    public static double wallRadius(double welzlRadius) {
        return welzlRadius + 0.5;
    }

    /** Physical area of a nozzle cross-section, in m^2. */
    public static double crossSectionArea(double welzlRadius) {
        double radius = wallRadius(welzlRadius);
        return Math.PI * radius * radius;
    }

    /**
     * Half-angle the wall is still turning the flow through as it reaches the lip,
     * in radians, averaged over the last third of the bell so that a single
     * parallel course of blocks tacked onto a wild flare does not read as a
     * finished nozzle.
     * <p>
     * This is the statistic that makes the middle of the nozzle matter. Two bells
     * with the same throat, the same exit and the same length score differently
     * depending on where the widening happened: get it done early and straighten
     * out, and the gas leaves pointing down the axis. Save it for the end and a
     * good part of the exhaust leaves sideways.
     *
     * @param wallRadii station radii in m, throat first, exit last
     */
    public static double contourExitHalfAngle(double[] wallRadii) {
        int steps = wallRadii.length - 1;
        if (steps < 1) {
            return 0;
        }
        int window = Math.min(steps, Math.max(1, (steps + 2) / 3));
        double rise = wallRadii[steps] - wallRadii[steps - window];
        return Math.atan(rise / window);
    }

    /**
     * Wetted wall area over throat area: what the boundary layer gets to rub
     * against. This is the term that stops "longer is always better" - past a
     * point the extra bell is only drag and mass.
     *
     * @param wallRadii station radii in m, one per 1 m course of blocks
     */
    public static double wettedAreaRatio(double[] wallRadii, double throatArea) {
        double wall = 0;
        for (int i = 0; i + 1 < wallRadii.length; i++) {
            double rise = wallRadii[i + 1] - wallRadii[i];
            // lateral area of a frustum one block tall
            wall += Math.PI * (wallRadii[i] + wallRadii[i + 1]) * Math.sqrt(1 + rise * rise);
        }
        return wall / throatArea;
    }

    /**
     * Total change in wall slope down the length of the bell. Zero for a straight
     * cone, small for a contour that eases round, large for one built out of
     * abrupt steps - a staircase reaches the same exit having turned the flow
     * several times as far, and pays for every one of them.
     *
     * @param wallRadii station radii in m, one per 1 m course of blocks
     */
    public static double contourTurning(double[] wallRadii) {
        double turning = 0;
        for (int i = 0; i + 2 < wallRadii.length; i++) {
            double before = wallRadii[i + 1] - wallRadii[i];
            double after = wallRadii[i + 2] - wallRadii[i + 1];
            turning += Math.abs(after - before);
        }
        return turning;
    }

    private static double rawContourEfficiency(double exitHalfAngle, double wettedAreaRatio, double turning) {
        double divergence = 0.5 * (1 + Math.cos(exitHalfAngle));
        double friction = Math.exp(-FRICTION_LOSS_COEFFICIENT * wettedAreaRatio);
        double shock = Math.exp(-TURNING_LOSS_COEFFICIENT * turning);
        return divergence * friction * shock;
    }

    /**
     * How well the shape of the bell itself does, against the reference contour.
     * 1.0 is a nozzle built as well as the one the fuel's rated impulse assumes;
     * a cleaner contour can edge slightly above it.
     */
    public static double contourEfficiency(double exitHalfAngle, double wettedAreaRatio, double turning) {
        return rawContourEfficiency(exitHalfAngle, wettedAreaRatio, turning) / REFERENCE_CONTOUR_EFFICIENCY;
    }

    /** Mass flow through a choked throat, in kg/s. */
    public static double chokedMassFlow(double throatArea, double chamberPressure) {
        return throatArea * chamberPressure / CHARACTERISTIC_VELOCITY;
    }

    /** The chamber pressure a given mass flow implies, in Pa. */
    public static double chamberPressureFor(double throatArea, double massFlow) {
        return massFlow * CHARACTERISTIC_VELOCITY / throatArea;
    }

    /**
     * The chamber pressure at which the pumps and the throat agree, in Pa.
     *
     * @param throatArea      throat area in m^2
     * @param pumpThroughput  summed rated mass flow of the attached turbopumps, in
     *                        kg/s
     */
    public static double equilibriumChamberPressure(double throatArea, double pumpThroughput) {
        return Math.sqrt(INJECTOR_RECOVERY * pumpThroughput * PUMP_RATED_PRESSURE * CHARACTERISTIC_VELOCITY /
                throatArea);
    }

    /**
     * The expansion ratio that drops the flow to a given fraction of chamber
     * pressure. This direction is explicit; going the other way is not.
     */
    public static double expansionRatio(double pressureRatio) {
        double massFlux = Math.sqrt((2 * GAMMA / (GAMMA - 1)) * Math.pow(pressureRatio, 2 / GAMMA) *
                (1 - Math.pow(pressureRatio, (GAMMA - 1) / GAMMA)));
        return VANDENKERCKHOVE / massFlux;
    }

    /**
     * Exit pressure as a fraction of chamber pressure, inverting
     * {@link #expansionRatio} by bisection. The relation is monotone between the
     * throat ratio (area ratio 1) and vacuum, so plain bisection is enough and
     * cannot wander.
     */
    public static double exitPressureRatio(double areaRatio) {
        if (areaRatio <= 1) {
            return THROAT_PRESSURE_RATIO;
        }
        double low = 1e-9;
        double high = THROAT_PRESSURE_RATIO;
        for (int i = 0; i < 60; i++) {
            double mid = 0.5 * (low + high);
            if (expansionRatio(mid) > areaRatio) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return 0.5 * (low + high);
    }

    /**
     * Thrust coefficient C_F, the factor by which the diverging section multiplies
     * what the throat alone would give: {@code F = C_F * p_0 * A_t}, equivalently
     * {@code F = mdot * C_F * c*}.
     * <p>
     * The first term is momentum thrust, which climbs as the bell gets longer and
     * the flow keeps expanding. The second is the pressure term, which is a bonus
     * in vacuum and a penalty against an atmosphere - a bell sized for space loses
     * badly at sea level, which is exactly why real vehicles fly two different
     * nozzles.
     *
     * @param areaRatio       exit area over throat area
     * @param chamberPressure chamber stagnation pressure, in Pa
     * @param ambientPressure outside pressure, in Pa; 0 for vacuum
     */
    public static double thrustCoefficient(double areaRatio, double chamberPressure, double ambientPressure) {
        double ratio = Math.max(areaRatio, 1);
        double exitRatio = exitPressureRatio(ratio);
        double ambientRatio = chamberPressure > 0 ? ambientPressure / chamberPressure : 0;

        double separationRatio = SEPARATION_PRESSURE_RATIO * ambientRatio;
        if (separationRatio > exitRatio) {
            // overexpanded past the point where the flow stays attached: the nozzle only
            // behaves as though it ended where separation happens
            exitRatio = Math.min(separationRatio, THROAT_PRESSURE_RATIO);
            ratio = expansionRatio(exitRatio);
        }

        double momentum = MOMENTUM_COEFFICIENT * Math.sqrt(1 - Math.pow(exitRatio, (GAMMA - 1) / GAMMA));
        return momentum + (exitRatio - ambientRatio) * ratio;
    }

    /**
     * How much of a fuel's rated specific impulse a given nozzle actually delivers,
     * as a fraction. The reference nozzle in vacuum is 1.0.
     */
    public static double nozzleEfficiency(double areaRatio, double chamberPressure, double ambientPressure) {
        return thrustCoefficient(areaRatio, chamberPressure, ambientPressure) / REFERENCE_THRUST_COEFFICIENT;
    }
}
