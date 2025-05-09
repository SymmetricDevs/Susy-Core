package supersymmetry.common.world;

import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Planet {

    // Defining variables
    private String planetName;
    private int id;
    private int dimID;

    private boolean isLoaded;

    private WorldGenDetails worldGenDetails;

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
                PlanetBiomeInfo[] pbiomes = worldGenDetails.getBiomeList();
                List<Biome> biomes = new ArrayList<Biome>();
                Arrays.stream(pbiomes).forEach((biome) -> {
                    biome.initiateBiome();
                    biomes.add(biome.getBiome());
                });

                GCSBDimensionManager.addDetailsTolist(dimID, worldGenDetails);

                if (!DimensionManager.isDimensionRegistered(this.dimID)) {
                    DimensionManager.registerDimension(this.dimID, ModDimension.planetType);
                    WorldType worldType = new DummyWorldType(worldGenDetails.getName(), biomes,
                            StringUtil.getBlockfromString(getWorldGenDetails().getStone()),
                            StringUtil.getBlockfromString(getWorldGenDetails().getBedrock()));
                    ModDimension.WORLD_TYPES.add(worldType);
                }
                if (DimensionManager.getWorld(this.dimID) == null) {
                    File chunkDir = new File(DimensionManager.getCurrentSaveRootDirectory(),
                            DimensionManager.createProviderFor(this.dimID).getSaveFolder());
                    if (ForgeChunkManager.savedWorldHasForcedChunkTickets(chunkDir)) {
                        DimensionManager.initDimension(this.dimID);
                    }
                }
            }
            GCSBLog.LOGGER.info("Loaded Planet with ID " + this.getId() + " and name " + this.getPlanetName());
        }
    }

    public void unload() {
        if (isLoaded) {
            isLoaded = false;
            GCSBLog.LOGGER.info("Unloaded Planet with ID " + this.getId() + " and name " + this.getPlanetName());
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

    public void setPlanetName(String planetName) {
        this.planetName = planetName;
    }

    public WorldGenDetails getWorldGenDetails() {
        return worldGenDetails;
    }

    public void setWorldGenDetails(WorldGenDetails worldGenDetails) {
        this.worldGenDetails = worldGenDetails;
    }

    @Override
    public String toString() {
        return "Planet Name: " + this.getPlanetName() + " Planet Id: " + this.getId() + " Planet DIM ID: " +
                this.getDimID();
    }
}
