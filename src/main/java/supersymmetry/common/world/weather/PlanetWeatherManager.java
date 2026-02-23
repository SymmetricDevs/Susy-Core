package supersymmetry.common.world.weather;

import java.util.*;

import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import supersymmetry.common.world.SuSyBiomes;
import supersymmetry.common.world.biome.SuSyBiomeEntry;

/**
 * Core weather management for a planet dimension.
 * Call {@link #tick(World)} every world tick from WorldProviderPlanet.updateWeather().
 *
 * Weather selection flow:
 * 1. Sample biomes across all online players in the dimension
 * 2. Intersect their allowed weather sets (IWeatherBiome)
 * 3. Weighted-random roll from the intersection
 * 4. Fallback to CLEAR if intersection is empty
 */
public class PlanetWeatherManager {

    /** How often (ticks) to sample biomes and re-evaluate the allowed weather pool. */
    private static final int BIOME_SAMPLE_INTERVAL = 200;

    private final Random rand;
    private PlanetWeatherSavedData savedData;
    private int biomeSampleCooldown = 0;

    // Last computed allowed set — cached between samples
    private Set<WeatherType> allowedWeatherCache = EnumSet.of(WeatherType.CLEAR);

    public PlanetWeatherManager() {
        this.rand = new Random();
    }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    /**
     * Must be called every tick from the WorldProvider.
     */
    public void tick(World world) {
        if (savedData == null) {
            savedData = PlanetWeatherSavedData.getOrCreate(world);
        }

        // Periodically refresh which weather types are valid
        if (--biomeSampleCooldown <= 0) {
            biomeSampleCooldown = BIOME_SAMPLE_INTERVAL;
            allowedWeatherCache = computeAllowedWeather(world);
        }

        // Count down current weather
        savedData.tickDown();

        // Transition to next weather when timer expires
        if (savedData.getTicksRemaining() <= 0) {
            WeatherType next = rollNextWeather(savedData.getCurrentWeather());
            int duration = next.rollDuration(rand);
            savedData.setWeather(next, duration);
        }
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    public WeatherType getCurrentWeather() {
        return savedData != null ? savedData.getCurrentWeather() : WeatherType.CLEAR;
    }

    public int getTicksRemaining() {
        return savedData != null ? savedData.getTicksRemaining() : 0;
    }

    /**
     * Force a specific weather type, e.g. from a command.
     * 
     * @param duration ticks, or -1 to use the type's default random duration
     */
    public void forceWeather(World world, WeatherType type, int duration) {
        if (savedData == null) savedData = PlanetWeatherSavedData.getOrCreate(world);
        int ticks = duration < 0 ? type.rollDuration(rand) : duration;
        savedData.setWeather(type, ticks);
    }

    // -------------------------------------------------------------------------
    // Internals
    // -------------------------------------------------------------------------

    /**
     * Weighted random selection from the allowed pool.
     * Will not immediately re-select the same type unless it's the only option.
     */
    private WeatherType rollNextWeather(WeatherType current) {
        Set<WeatherType> pool = new HashSet<>(allowedWeatherCache);

        // Avoid immediate repetition if there are alternatives
        if (pool.size() > 1) pool.remove(current);

        int totalWeight = pool.stream().mapToInt(t -> t.weight).sum();
        if (totalWeight <= 0) return WeatherType.CLEAR;

        int roll = rand.nextInt(totalWeight);
        int cumulative = 0;
        for (WeatherType type : pool) {
            cumulative += type.weight;
            if (roll < cumulative) return type;
        }
        return WeatherType.CLEAR;
    }

    /**
     * Samples biomes from player positions and returns the intersection
     * of their allowed weather sets.
     */
    private Set<WeatherType> computeAllowedWeather(World world) {
        List<net.minecraft.entity.player.EntityPlayer> players = world.playerEntities;

        if (players.isEmpty()) {
            // No players — return full set so weather still advances
            return EnumSet.allOf(WeatherType.class);
        }

        Set<WeatherType> intersection = null;

        for (net.minecraft.entity.player.EntityPlayer player : players) {
            Biome biome = world.getBiome(player.getPosition());
            Set<WeatherType> biomeAllowed = getAllowedWeatherForBiome(biome);

            if (intersection == null) {
                intersection = new HashSet<>(biomeAllowed);
            } else {
                intersection.retainAll(biomeAllowed);
            }
        }

        // Fallback: always keep CLEAR available
        if (intersection == null || intersection.isEmpty()) {
            return EnumSet.of(WeatherType.CLEAR);
        }
        intersection.add(WeatherType.CLEAR);
        return intersection;
    }

    /**
     * Gets the allowed weather for a biome via IWeatherBiome if available,
     * otherwise returns a sensible default set.
     */
    private Set<WeatherType> getAllowedWeatherForBiome(Biome biome) {
        SuSyBiomeEntry entry = SuSyBiomes.getEntry(biome);
        if (entry instanceof IWeatherBiome) {
            return ((IWeatherBiome) entry).getAllowedWeather();
        }
        // Default: everything except the most extreme types
        return EnumSet.of(
                WeatherType.CLEAR,
                WeatherType.FOG,
                WeatherType.RAIN,
                WeatherType.DUST_STORM,
                WeatherType.THUNDERSTORM);
    }
}
