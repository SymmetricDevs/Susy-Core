package supersymmetry.common.world;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.block.state.IBlockState;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;

import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.world.biome.SuSyBiomeEntry;

public class Planet {

    // Defining variables
    private String planetName;
    private int id;
    private int dimID;

    private int biomeSize = 5;

    private boolean isLoaded;

    public int averageGroundLevel;
    public List<BiomeManager.BiomeEntry> biomeList;
    public IBlockState stone;
    public IBlockState bedrock;
    public IRenderHandler skyRenderer;
    public double gravity;
    public double dragMultiplier = 0.98;
    public boolean supportsFire;

    // Custom sky renderer (optional override)
    private SuSySkyRenderer SuSySkyRenderer;

    // Time
    private float dayLength = 1.0f; // Default: normal Earth-like day (1.0 = 24000 ticks)
    private float timeOffset = 0.0f; // Offset in celestial angle (0.0 to 1.0, where 0.5 = 12 hours)
    private IRenderHandler customSkyRenderer = null;

    // Atmosphere

    private long ticksPerDay = 24000L;

    public Planet(int id, String planetName) {
        this.id = id;
        this.planetName = planetName;
    }

    public Planet(int id, int dimID, String planetName) {
        this.id = id;
        this.dimID = dimID;
        this.planetName = planetName;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void load() {
        if (!isLoaded) {
            isLoaded = true;
            if (this.dimID != 0) {
                SuSyDimensions.PLANETS.put(dimID, this);

                if (!DimensionManager.isDimensionRegistered(this.dimID)) {
                    DimensionManager.registerDimension(this.dimID, SuSyDimensions.planetType);
                }
                if (DimensionManager.getWorld(this.dimID) == null) {
                    File chunkDir = new File(DimensionManager.getCurrentSaveRootDirectory(),
                            DimensionManager.createProviderFor(this.dimID).getSaveFolder());
                    if (ForgeChunkManager.savedWorldHasForcedChunkTickets(chunkDir)) {
                        DimensionManager.initDimension(this.dimID);
                    }
                }

                // Apply sky renderer - try immediately and log result
                applySkyRenderer();
            }
        }
    }

    // Add this helper method
    private void applySkyRenderer() {
        net.minecraft.world.World world = DimensionManager.getWorld(this.dimID);
        if (world != null && world.provider != null) {
            IRenderHandler renderer = getEffectiveSkyRenderer();
            if (renderer != null) {
                world.provider.setSkyRenderer(renderer);
            }
        }
    }

    public void unload() {
        if (isLoaded) {
            isLoaded = false;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDimID() {
        return dimID;
    }

    public void setDimID(int dimID) {
        this.dimID = dimID;
    }

    public String getPlanetName() {
        return planetName;
    }

    public Planet setPlanetName(String planetName) {
        this.planetName = planetName;
        return this;
    }

    public int getAverageGroundLevel() {
        return averageGroundLevel;
    }

    public void setAverageGroundLevel(int averageGroundLevel) {
        this.averageGroundLevel = averageGroundLevel;
    }

    public List<BiomeManager.BiomeEntry> getBiomeList() {
        return this.biomeList;
    }

    public Planet setBiomeList(List<BiomeManager.BiomeEntry> biomeList) {
        this.biomeList = biomeList;
        return this;
    }

    public Planet setBiomeList(BiomeManager.BiomeEntry... biomeList) {
        // List.of() does not exist in 1.8
        this.biomeList = Arrays.asList(biomeList);
        return this;
    }

    public Planet setBiomeList(SuSyBiomeEntry... biomeList) {
        // Convert to standard BiomeEntry list
        this.biomeList = Arrays.stream(biomeList)
                .collect(Collectors.toList());
        return this;
    }

    public Planet setBiomeSize(int biomeSize) {
        this.biomeSize = biomeSize;
        return this;
    }

    public IBlockState getStone() {
        return this.stone;
    }

    public Planet setStone(IBlockState stone) {
        this.stone = stone;
        return this;
    }

    public IBlockState getBedrock() {
        return this.bedrock;
    }

    public Planet setBedrock(IBlockState bedrock) {
        this.bedrock = bedrock;
        return this;
    }

    @Override
    public String toString() {
        return "Planet Name: " + this.getPlanetName() + " Planet Id: " + this.getId() + " Planet DIM ID: " +
                this.getDimID();
    }

    public Planet setSkyRenderer(IRenderHandler skyRenderer) {
        this.skyRenderer = skyRenderer;
        return this;
    }

    public Planet setGravity(double gravity) {
        this.gravity = gravity;
        return this;
    }

    public Planet setDragMultiplier(double dragMultiplier) {
        this.dragMultiplier = dragMultiplier;
        return this;
    }

    public Planet setSupportsFire(boolean supportsFire) {
        this.supportsFire = supportsFire;
        return this;
    }

    public int getBiomeSize() {
        return this.biomeSize;
    }

    private IBlockState breccia = null;
    private IBlockState impactMelt = null;
    private IBlockState impactEjecta = null;

    // Add these methods to Planet class
    public boolean hasCraterMaterials() {
        return breccia != null && impactMelt != null && impactEjecta != null;
    }

    public IBlockState getBreccia() {
        return breccia != null ? breccia : getStone();
    }

    public IBlockState getImpactMelt() {
        return impactMelt != null ? impactMelt : getStone();
    }

    // Add setter methods for builder pattern (if Planet uses builder)
    public Planet setBreccia(IBlockState breccia) {
        this.breccia = breccia;
        return this;
    }

    public Planet setImpactMelt(IBlockState impactMelt) {
        this.impactMelt = impactMelt;
        return this;
    }

    public Planet setImpactEjecta(IBlockState impactEjecta) {
        this.impactEjecta = impactEjecta;
        return this;
    }

    // Custom sky renderer methods
    public boolean hasCustomSky() {
        return SuSySkyRenderer != null;
    }

    public SuSySkyRenderer getSuSySkyRenderer() {
        return SuSySkyRenderer;
    }

    public Planet setSuSySkyRenderer(SuSySkyRenderer renderer) {
        this.SuSySkyRenderer = renderer;
        this.customSkyRenderer = renderer;
        return this;
    }

    public Planet setDayLength(float dayLength) {
        this.dayLength = dayLength;
        return this;
    }

    public float getDayLength() {
        return this.dayLength;
    }

    public Planet setTimeOffset(float timeOffset) {
        this.timeOffset = timeOffset;
        return this;
    }

    public float getTimeOffset() {
        return this.timeOffset;
    }

    public Planet setCustomSkyRenderer(IRenderHandler renderer) {
        this.customSkyRenderer = renderer;
        return this;
    }

    public IRenderHandler getEffectiveSkyRenderer() {
        // Return the custom renderer if set (SuSySkyRenderer)
        if (this.customSkyRenderer != null) {
            return this.customSkyRenderer;
        }
        // Fallback to the generic skyRenderer
        if (this.skyRenderer != null) {
            return this.skyRenderer;
        }
        return null;
    }

    public Planet setTicksPerDay(long ticks) {
        this.ticksPerDay = ticks;
        return this;
    }

    public long getTicksPerDay() {
        return this.ticksPerDay;
    }

    public IBlockState getImpactEjecta() {
        return SuSyBlocks.REGOLITH.getDefaultState();
    }
}
