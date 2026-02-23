package supersymmetry.common.world.weather;

/**
 * All possible weather states for SuSy planet dimensions.
 * Each type defines its visual, gameplay, and world effects.
 */
public enum WeatherType {

    // @formatter:off
    //         name            weight  minDur   maxDur   moveM  dmg/t  particles            placesBlocks erodes
    CLEAR      ("clear",        60,    12000,   36000,   1.0f,  0,     ParticleProfile.NONE,       false, false),
    FOG        ("fog",          15,    6000,    18000,   0.85f, 0,     ParticleProfile.FOG,        false, false),
    RAIN       ("rain",         20,    4000,    12000,   0.9f,  0,     ParticleProfile.RAIN,       false, false),
    SNOWSTORM  ("snowstorm",    10,    4000,    10000,   0.7f,  0,     ParticleProfile.SNOW,       true,  false),
    HAILSTORM  ("hailstorm",    8,     2000,    8000,    0.65f, 1,     ParticleProfile.HAIL,       false, false),
    DUST_STORM ("dust_storm",   12,    3000,    10000,   0.6f,  0,     ParticleProfile.DUST,       false, true),
    THUNDERSTORM("thunderstorm",10,    3000,    9000,    0.75f, 0,     ParticleProfile.THUNDER,    false, false),
    HURRICANE  ("hurricane",    4,     2000,    6000,    0.4f,  2,     ParticleProfile.HURRICANE,  false, true),
    TORNADO    ("tornado",      2,     1000,    4000,    0.0f,  4,     ParticleProfile.TORNADO,    false, true);
    // @formatter:on

    public final String id;
    public final int weight;
    public final int minDurationTicks;
    public final int maxDurationTicks;
    /** Multiplier applied to player walk/swim speed. */
    public final float movementMultiplier;
    /** Half-hearts of damage per tick while outdoors. 0 = no damage. */
    public final int damagePerTick;
    public final ParticleProfile particles;
    /** If true, this weather can place blocks (e.g. snow layers). */
    public final boolean placesBlocks;
    /** If true, this weather can erode/remove surface blocks. */
    public final boolean erodeSurface;

    WeatherType(String id, int weight, int minDurationTicks, int maxDurationTicks,
                float movementMultiplier, int damagePerTick,
                ParticleProfile particles, boolean placesBlocks, boolean erodeSurface) {
        this.id = id;
        this.weight = weight;
        this.minDurationTicks = minDurationTicks;
        this.maxDurationTicks = maxDurationTicks;
        this.movementMultiplier = movementMultiplier;
        this.damagePerTick = damagePerTick;
        this.particles = particles;
        this.placesBlocks = placesBlocks;
        this.erodeSurface = erodeSurface;
    }

    /**
     * Roll a random duration for this weather type.
     */
    public int rollDuration(java.util.Random rand) {
        return minDurationTicks + rand.nextInt(Math.max(1, maxDurationTicks - minDurationTicks));
    }

    /**
     * Whether this weather deals damage to unprotected players outdoors.
     */
    public boolean dealsDamage() {
        return damagePerTick > 0;
    }

    /**
     * Whether players are impeded by this weather.
     */
    public boolean impedesMovement() {
        return movementMultiplier < 1.0f;
    }

    public static WeatherType fromId(String id) {
        for (WeatherType type : values()) {
            if (type.id.equals(id)) return type;
        }
        return CLEAR;
    }

    /**
     * Particle/visual profiles for each weather type.
     * Actual particle spawning is handled by WeatherEventHandler.
     */
    public enum ParticleProfile {
        NONE,
        FOG,
        RAIN,
        SNOW,
        HAIL,
        DUST,
        THUNDER,
        HURRICANE,
        TORNADO
    }
}
