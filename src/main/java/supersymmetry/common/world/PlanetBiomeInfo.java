package supersymmetry.common.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;


public class PlanetBiomeInfo {

    private IBlockState filler;
    private IBlockState top;

    private String name;
    private int waterColor;
    private int skyColor;
    private float baseHeight = 0.1F;
    private float heightVariation = 0.2F;
    private float temperature = 0.5F;
    private float rainfall = 0.5F;

    private boolean rainDisabled;

    private transient Biome biome;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IBlockState getFillerBlock() {
        return filler;
    }

    public void setFillerBlock(IBlockState fillerBlock) {
        this.filler = fillerBlock;
    }

    public IBlockState getTopBlock() {
        return top;
    }


    public void setTopBlock(IBlockState topBlock) {
        this.top = topBlock;
    }

    public int getWaterColor() {
        return waterColor;
    }

    public void setWaterColor(int waterColor) {
        this.waterColor = waterColor;
    }

    public int getSkyColor() {
        return skyColor;
    }

    public void setSkyColor(int skyColor) {
        this.skyColor = skyColor;
    }

    public float getBaseHeight() {
        return baseHeight;
    }

    public float getHeightVariation() {
        return this.heightVariation;
    }

    public void setHeightVariation(float heightVariation) {
        this.heightVariation = heightVariation;
    }

    public float getRainfall() {
        return rainfall;
    }

    public void setRainfall(float rainfall) {
        this.rainfall = rainfall;
    }

    public boolean getRainDisabled() {
        return rainDisabled;
    }

    public void setRainDisabled(boolean rainDisabled) {
        this.rainDisabled = rainDisabled;
    }

    public Biome getBiome() {
        return biome;
    }

    public void initiateBiome() {
        Biome.BiomeProperties properties = new Biome.BiomeProperties(this.name);
        if (this.waterColor != -1) {
            properties.setWaterColor(this.waterColor);
        }
        if (this.rainDisabled) {
            properties.setRainDisabled();
        }

        properties.setBaseHeight(this.baseHeight);
        properties.setHeightVariation(this.heightVariation);
        properties.setTemperature(this.temperature);
        properties.setRainfall(this.rainfall);

        this.biome = new PlanetBiome(properties, filler,
                top, this.skyColor);
        SuSyDimensions.BIOMES.add(biome.setRegistryName(new ResourceLocation("gcsb", name)));
    }
}
