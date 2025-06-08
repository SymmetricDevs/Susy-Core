package supersymmetry.common.world;

import net.minecraft.block.state.IBlockState;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import supersymmetry.api.SusyLog;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Planet {

    // Defining variables
    private String planetName;
    private int id;
    private int dimID;

    private boolean isLoaded;

    public int averageGroundLevel;
    public List<BiomeManager.BiomeEntry> biomeList;
    public IBlockState stone;
    public IBlockState bedrock;
    public IRenderHandler skyRenderer;
    public double gravity;


    // Atmosphere

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
            // TODO: add check if dim already exists, if it does load it/or idk
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
            }
            SusyLog.logger.info("Loaded Planet with ID " + this.getId() + " and name " + this.getPlanetName());
        }
    }

    public void unload() {
        if (isLoaded) {
            isLoaded = false;
            SusyLog.logger.info("Unloaded Planet with ID " + this.getId() + " and name " + this.getPlanetName());
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
}
