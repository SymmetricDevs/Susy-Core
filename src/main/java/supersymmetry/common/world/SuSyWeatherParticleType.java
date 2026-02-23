package supersymmetry.common.world;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

public enum SuSyWeatherParticleType {

    SNOW("snow", 67, true),
    DUST("dust", 68, true),
    HAIL("hail", 69, true),
    TORNADO("tornado", 70, true),
    HURRICANE("hurricane", 71, true);

    private final String particleName;
    private final int particleID;
    private final boolean shouldIgnoreRange;
    private final int argumentCount;
    private static final Map<Integer, SuSyWeatherParticleType> PARTICLES = Maps
            .<Integer, SuSyWeatherParticleType>newHashMap();
    private static final Map<String, SuSyWeatherParticleType> BY_NAME = Maps
            .<String, SuSyWeatherParticleType>newHashMap();

    private SuSyWeatherParticleType(String particleNameIn, int particleIDIn, boolean shouldIgnoreRangeIn,
                                    int argumentCountIn) {
        this.particleName = particleNameIn;
        this.particleID = particleIDIn;
        this.shouldIgnoreRange = shouldIgnoreRangeIn;
        this.argumentCount = argumentCountIn;
    }

    private SuSyWeatherParticleType(String particleNameIn, int particleIDIn, boolean shouldIgnoreRangeIn) {
        this(particleNameIn, particleIDIn, shouldIgnoreRangeIn, 0);
    }

    public static Set<String> getParticleNames() {
        return BY_NAME.keySet();
    }

    public String getParticleName() {
        return this.particleName;
    }

    public int getParticleID() {
        return this.particleID;
    }

    public int getArgumentCount() {
        return this.argumentCount;
    }

    public boolean getShouldIgnoreRange() {
        return this.shouldIgnoreRange;
    }

    /**
     * Gets the relative EnumParticleTypes by id.
     */
    @Nullable
    public static SuSyWeatherParticleType getParticleFromId(int particleId) {
        return PARTICLES.get(Integer.valueOf(particleId));
    }

    @Nullable
    public static SuSyWeatherParticleType getByName(String nameIn) {
        return BY_NAME.get(nameIn);
    }

    static {
        for (SuSyWeatherParticleType enumparticletypes : values()) {
            PARTICLES.put(Integer.valueOf(enumparticletypes.getParticleID()), enumparticletypes);
            BY_NAME.put(enumparticletypes.getParticleName(), enumparticletypes);
        }
    }
}
