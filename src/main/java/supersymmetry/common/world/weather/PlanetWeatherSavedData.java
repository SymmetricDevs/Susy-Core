package supersymmetry.common.world.weather;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

/**
 * Persists weather state (current type + ticks remaining) for a planet dimension.
 * Stored per-dimension via MapStorage so each planet has independent weather.
 */
public class PlanetWeatherSavedData extends WorldSavedData {

    private static final String KEY_WEATHER_TYPE = "weatherType";
    private static final String KEY_TICKS_REMAINING = "ticksRemaining";

    private static final String DATA_NAME_PREFIX = "susy_weather_";

    private WeatherType currentWeather = WeatherType.CLEAR;
    private int ticksRemaining = WeatherType.CLEAR.rollDuration(new java.util.Random());

    public PlanetWeatherSavedData(String name) {
        super(name);
    }

    // Required no-arg-style constructor Forge uses via reflection
    @SuppressWarnings("unused")
    public PlanetWeatherSavedData() {
        super(DATA_NAME_PREFIX + "default");
    }

    // -------------------------------------------------------------------------
    // WorldSavedData
    // -------------------------------------------------------------------------

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        currentWeather = WeatherType.fromId(nbt.getString(KEY_WEATHER_TYPE));
        ticksRemaining = nbt.getInteger(KEY_TICKS_REMAINING);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setString(KEY_WEATHER_TYPE, currentWeather.id);
        nbt.setInteger(KEY_TICKS_REMAINING, ticksRemaining);
        return nbt;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public WeatherType getCurrentWeather() {
        return currentWeather;
    }

    public int getTicksRemaining() {
        return ticksRemaining;
    }

    public void setWeather(WeatherType type, int ticks) {
        this.currentWeather = type;
        this.ticksRemaining = ticks;
        markDirty();
    }

    public void tickDown() {
        if (ticksRemaining > 0) {
            ticksRemaining--;
            markDirty();
        }
    }

    // -------------------------------------------------------------------------
    // Static helpers
    // -------------------------------------------------------------------------

    /**
     * Load or create weather data for the given world/dimension.
     */
    public static PlanetWeatherSavedData getOrCreate(World world) {
        String name = DATA_NAME_PREFIX + world.provider.getDimension();
        MapStorage storage = world.getMapStorage();
        if (storage == null) return new PlanetWeatherSavedData(name);

        PlanetWeatherSavedData data = (PlanetWeatherSavedData) storage.getOrLoadData(
                PlanetWeatherSavedData.class, name);

        if (data == null) {
            data = new PlanetWeatherSavedData(name);
            storage.setData(name, data);
        }
        return data;
    }
}
