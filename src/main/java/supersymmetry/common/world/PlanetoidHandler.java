package supersymmetry.common.world;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;

import supersymmetry.api.space.Planetoid;
import supersymmetry.common.world.biome.SuSyBiomeEntry;

public class PlanetoidHandler {

    private static final Map<Integer, PlanetoidHandler> HANDLERS = new HashMap<>();

    private final Planetoid planetoid;
    private int biomeSize = 5;
    private boolean isLoaded;

    public List<BiomeManager.BiomeEntry> biomeList;
    public IBlockState stone;
    public IBlockState bedrock;
    public double gravity;
    public double dragMultiplier = 0.98;
    public boolean supportsFire;

    private IRenderHandler customSkyRenderer = null;

    public PlanetoidHandler(Planetoid planetoid) {
        this.planetoid = planetoid;
    }

    public static PlanetoidHandler get(int dimId) {
        return HANDLERS.get(dimId);
    }

    public void load() {
        if (isLoaded) return;
        isLoaded = true;

        int dimId = planetoid.getDimension();
        if (dimId == 0) return;

        HANDLERS.put(dimId, this);

        if (!DimensionManager.isDimensionRegistered(dimId)) {
            DimensionManager.registerDimension(dimId, SuSyDimensions.planetType);
        }
        if (DimensionManager.getWorld(dimId) == null) {
            File chunkDir = new File(DimensionManager.getCurrentSaveRootDirectory(),
                    DimensionManager.createProviderFor(dimId).getSaveFolder());
            if (ForgeChunkManager.savedWorldHasForcedChunkTickets(chunkDir)) {
                DimensionManager.initDimension(dimId);
            }
        }

        applySkyRenderer(dimId);
    }

    private void applySkyRenderer(int dimId) {
        World world = DimensionManager.getWorld(dimId);
        if (world != null && world.provider != null) {
            IRenderHandler renderer = getSkyRenderer();
            if (renderer != null) {
                world.provider.setSkyRenderer(renderer);
            }
        }
    }

    public PlanetoidHandler setBiomeList(List<BiomeManager.BiomeEntry> biomeList) {
        this.biomeList = biomeList;
        return this;
    }

    public PlanetoidHandler setBiomeList(BiomeManager.BiomeEntry... biomeList) {
        this.biomeList = Arrays.asList(biomeList);
        return this;
    }

    public PlanetoidHandler setBiomeList(SuSyBiomeEntry... biomeList) {
        this.biomeList = Arrays.stream(biomeList)
                .collect(Collectors.toList());
        return this;
    }

    public PlanetoidHandler setBiomeSize(int biomeSize) {
        this.biomeSize = biomeSize;
        return this;
    }

    public int getBiomeSize() {
        return this.biomeSize;
    }

    public IBlockState getStone() {
        return this.stone;
    }

    public PlanetoidHandler setStone(IBlockState stone) {
        this.stone = stone;
        return this;
    }

    public IBlockState getBedrock() {
        return this.bedrock;
    }

    public PlanetoidHandler setGravity(double gravity) {
        this.gravity = gravity;
        return this;
    }

    public PlanetoidHandler setCustomSkyRenderer(IRenderHandler renderer) {
        this.customSkyRenderer = renderer;
        return this;
    }

    public IRenderHandler getSkyRenderer() {
        return this.customSkyRenderer;
    }

    private IBlockState breccia;

    public boolean hasCraterMaterials() {
        return breccia != null;
    }

    public IBlockState getBreccia() {
        return breccia != null ? breccia : getStone();
    }
}
