package supersymmetry.common.world.biome;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeManager.BiomeEntry;

import supersymmetry.common.world.SuSyBiomes;
import supersymmetry.common.world.weather.IWeatherBiome;
import supersymmetry.common.world.weather.WeatherType;

/**
 * Extended BiomeEntry that allows setting crater-specific materials inline.
 * Provides a fluent API for configuring biome properties during planet setup.
 */
public class SuSyBiomeEntry extends BiomeEntry implements IWeatherBiome {

    private IBlockState craterBlock = null;
    private Set<WeatherType> allowedWeather = EnumSet.of(WeatherType.CLEAR);

    public SuSyBiomeEntry(Biome biome, int weight) {
        super(biome, weight);
    }

    /**
     * Sets the crater/regolith block for this biome entry.
     * This allows inline configuration during planet setup.
     *
     * @param block The IBlockState to use for crater ejecta in this biome
     * @return this for method chaining
     */
    public SuSyBiomeEntry setCraterBlock(IBlockState block) {
        this.craterBlock = block;
        // Also register it in the global biome map
        SuSyBiomes.setCraterBlock(this.biome, block);
        return this;
    }

    /**
     * Gets the crater block configured for this entry.
     *
     * @return The configured crater block, or null if not set
     */
    public IBlockState getCraterBlock() {
        return this.craterBlock;
    }

    /**
     * Checks if this entry has a crater block configured.
     *
     * @return true if crater block is set, false otherwise
     */
    public boolean hasCraterBlock() {
        return this.craterBlock != null;
    }

    public SuSyBiomeEntry setAllowedWeather(WeatherType... types) {
        allowedWeather = EnumSet.copyOf(java.util.Arrays.asList(types));
        // Always ensure CLEAR is available
        allowedWeather.add(WeatherType.CLEAR);
        return this;
    }

    @Override
    public Set<WeatherType> getAllowedWeather() {
        return allowedWeather;
    }
}
