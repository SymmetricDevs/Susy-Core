package supersymmetry.common.world.weather;

import java.util.Set;

/**
 * Implemented by biome entries that want to declare which weather types are valid for them.
 * SuSyBiomeEntry should implement this.
 */
public interface IWeatherBiome {

    /**
     * Returns the set of weather types that can occur in this biome.
     * CLEAR should always be included unless you want something very exotic.
     */
    Set<WeatherType> getAllowedWeather();
}
