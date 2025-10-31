package supersymmetry.common.world;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.*;

public class PlanetChunkGenerator implements IChunkGenerator {

    private final Random rand;
    private final World world;
    private final boolean mapFeaturesEnabled;
    private final WorldType terrainType;
    private final double[] heightMap;
    private final float[] biomeWeights;
    public NoiseGeneratorOctaves scaleNoise;
    public NoiseGeneratorOctaves depthNoise;
    public NoiseGeneratorOctaves forestNoise;
    double[] mainNoiseRegion;
    double[] minLimitRegion;
    double[] maxLimitRegion;
    double[] depthRegion;
    private NoiseGeneratorOctaves minLimitPerlinNoise;
    private NoiseGeneratorOctaves maxLimitPerlinNoise;
    private NoiseGeneratorOctaves mainPerlinNoise;
    private NoiseGeneratorPerlin surfaceNoise;
    private double[] depthBuffer = new double[256];
    private final MapGenBase caveGenerator = new MapGenCaves();
    private Biome[] biomesForGeneration;
    private final double depthNoiseScaleX = 200.0D;
    private final double depthNoiseScaleZ = 200.0D;
    private final double depthNoiseScaleExponent = 0.5D;
    private final int coordScale = 684;
    private final int mainNoiseScaleX = 80;
    private final int mainNoiseScaleY = 160;
    private final int mainNoiseScaleZ = 80;
    private final int heightScale = 684;
    private final int biomeDepthOffSet = 0;
    private final int biomeScaleOffset = 0;
    private final double heightStretch = 12;
    private final double baseSize = 8.5D;
    private final double lowerLimitScale = 512D;
    private final double upperLimitScale = 512D;
    private final float biomeDepthWeight = 1.0F;
    private final float biomeScaleWeight = 1.0F;
    private final int seaLevel = 25;

    private final IBlockState stone;
    private final IBlockState bedrock;

    public PlanetChunkGenerator(World worldIn, long seed) {
        world = worldIn;
        mapFeaturesEnabled = true;
        terrainType = worldIn.getWorldInfo().getTerrainType();
        rand = new Random(seed);
        minLimitPerlinNoise = new NoiseGeneratorOctaves(this.rand, 16);
        maxLimitPerlinNoise = new NoiseGeneratorOctaves(this.rand, 16);
        mainPerlinNoise = new NoiseGeneratorOctaves(this.rand, 8);
        surfaceNoise = new NoiseGeneratorPerlin(this.rand, 4);
        scaleNoise = new NoiseGeneratorOctaves(this.rand, 10);
        depthNoise = new NoiseGeneratorOctaves(this.rand, 16);
        heightMap = new double[825];
        biomeWeights = new float[25];

        Planet planet = SuSyDimensions.PLANETS.get(world.provider.getDimension());
        this.stone = planet.getStone();
        this.bedrock = planet.getBedrock();

        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                float f = 10.0F / MathHelper.sqrt((float) (i * i + j * j) + 0.2F);
                this.biomeWeights[i + 2 + (j + 2) * 5] = f;
            }
        }

        worldIn.setSeaLevel(this.seaLevel);

        net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextOverworld ctx = new net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextOverworld(
                minLimitPerlinNoise, maxLimitPerlinNoise, mainPerlinNoise, surfaceNoise, scaleNoise, depthNoise,
                forestNoise);
        ctx = net.minecraftforge.event.terraingen.TerrainGen.getModdedNoiseGenerators(worldIn, this.rand, ctx);
        this.minLimitPerlinNoise = ctx.getLPerlin1();
        this.maxLimitPerlinNoise = ctx.getLPerlin2();
        this.mainPerlinNoise = ctx.getPerlin();
        this.surfaceNoise = ctx.getHeight();
        this.scaleNoise = ctx.getScale();
        this.depthNoise = ctx.getDepth();
        this.forestNoise = ctx.getForest();
    }

    /**
     * Creates base terrain (air and stone) using heightmaps.
     *
     * @param chunkX
     * @param chunkZ
     * @param primer
     */
    public void generateTerrain(int chunkX, int chunkZ, ChunkPrimer primer) {
        // Get biomes being used in this chunk
        this.biomesForGeneration = this.world.getBiomeProvider().getBiomesForGeneration(this.biomesForGeneration,
                chunkX * 4 - 2, chunkZ * 4 - 2, 10, 10);

        // Get heightmap for this chunk
        this.generateHeightmap(chunkX * 4, 0, chunkZ * 4);

        // Divide chunk along x axis
        for (int iX = 0; iX < 4; ++iX) {
            int j = iX * 5;
            int k = (iX + 1) * 5;

            // Divide chunk along z axis
            for (int iZ = 0; iZ < 4; ++iZ) {
                int i1 = (j + iZ) * 33;
                int j1 = (j + iZ + 1) * 33;
                int k1 = (k + iZ) * 33;
                int l1 = (k + iZ + 1) * 33;

                // Divide chunk along y axis.
                // Now have a 4 x 8 x 4 subchunk to work with.
                for (int iY = 0; iY < 32; ++iY) {
                    // Get noise values from heightmap.
                    double d1 = this.heightMap[i1 + iY];
                    double d2 = this.heightMap[j1 + iY];
                    double d3 = this.heightMap[k1 + iY];
                    double d4 = this.heightMap[l1 + iY];

                    // Lerp values.
                    double d5 = (this.heightMap[i1 + iY + 1] - d1) * 0.125D;
                    double d6 = (this.heightMap[j1 + iY + 1] - d2) * 0.125D;
                    double d7 = (this.heightMap[k1 + iY + 1] - d3) * 0.125D;
                    double d8 = (this.heightMap[l1 + iY + 1] - d4) * 0.125D;

                    // Loop through each Y level in the subchunk
                    for (int jY = 0; jY < 8; ++jY) {
                        double d9 = 0.25D;
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * 0.25D;
                        double d13 = (d4 - d2) * 0.25D;

                        int height = (iY * 8) + jY;
                        // Loop through x axis
                        for (int jX = 0; jX < 4; ++jX) {
                            double d14 = 0.25D;
                            double d16 = (d11 - d10) * 0.25D;
                            double zVariation = d10 - d16;

                            // Loop through z axis
                            for (int jZ = 0; jZ < 4; ++jZ) {
                                // If the noiseLevel is above 0, set block to stone.
                                if (height < 2) {
                                    primer.setBlockState(iX * 4 + jX, iY * 8 + jY, iZ * 4 + jZ, bedrock);
                                } else if ((zVariation += d16) > 0.0D) {
                                    primer.setBlockState(iX * 4 + jX, iY * 8 + jY, iZ * 4 + jZ, stone);
                                }

                            }

                            d10 += d12;
                            d11 += d13;
                        }

                        d1 += d5;
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                    }
                }
            }
        }
    }

    public void replaceBiomeBlocks(int x, int z, ChunkPrimer primer, Biome[] biomesIn) {
        if (!net.minecraftforge.event.ForgeEventFactory.onReplaceBiomeBlocks(this, x, z, primer, this.world)) return;
        double d0 = 0.03125D;
        this.depthBuffer = this.surfaceNoise.getRegion(this.depthBuffer, x * 16, z * 16, 16, 16,
                0.0625D, 0.0625D, 1.0D);

        // Loop through each x row.
        for (int iX = 0; iX < 16; ++iX) {
            // Loop through each y row.
            for (int iZ = 0; iZ < 16; ++iZ) {
                // Get the biome at this x,z coord?
                Biome biome = biomesIn[iZ + iX * 16];

                // Use the biome to replace blocks at this x, z coord (full y column).
                biome.genTerrainBlocks(this.world, this.rand, primer, x * 16 + iX, z * 16 + iZ,
                        this.depthBuffer[iZ + iX * 16]);
            }
        }
    }

    /**
     * Generates the chunk at the specified position, from scratch
     */
    public Chunk generateChunk(int x, int z) {
        this.rand.setSeed((long) x * 341873128712L + (long) z * 132897987541L);
        ChunkPrimer chunkprimer = new ChunkPrimer();
        this.generateTerrain(x, z, chunkprimer);
        this.biomesForGeneration = this.world.getBiomeProvider().getBiomes(this.biomesForGeneration, x * 16, z * 16, 16,
                16);
        this.replaceBiomeBlocks(x, z, chunkprimer, this.biomesForGeneration);

        Chunk chunk = new Chunk(this.world, chunkprimer, x, z);
        byte[] abyte = chunk.getBiomeArray();

        for (int i = 0; i < abyte.length; ++i) {
            abyte[i] = (byte) Biome.getIdForBiome(this.biomesForGeneration[i]);
        }

        chunk.generateSkylightMap();
        return chunk;
    }

    private void generateHeightmap(int xOffset, int yOffset, int zOffset) {
        this.depthRegion = this.depthNoise.generateNoiseOctaves(this.depthRegion, xOffset, zOffset, 5, 5,
                this.depthNoiseScaleX, this.depthNoiseScaleZ, this.depthNoiseScaleExponent);
        float coordF = this.coordScale;
        float heightF = this.heightScale;
        this.mainNoiseRegion = this.mainPerlinNoise.generateNoiseOctaves(this.mainNoiseRegion, xOffset, yOffset,
                zOffset, 5, 33, 5, coordF / this.mainNoiseScaleX, heightF / this.mainNoiseScaleY,
                coordF / this.mainNoiseScaleZ);
        this.minLimitRegion = this.minLimitPerlinNoise.generateNoiseOctaves(this.minLimitRegion, xOffset,
                yOffset, zOffset, 5, 33, 5, coordF, heightF, coordF);
        this.maxLimitRegion = this.maxLimitPerlinNoise.generateNoiseOctaves(this.maxLimitRegion, xOffset,
                yOffset, zOffset, 5, 33, 5, coordF, heightF, coordF);
        int heightMapPosition = 0;
        int depthRegionPosition = 0;

        for (int k = 0; k < 5; ++k) {
            for (int l = 0; l < 5; ++l) {
                float heightVariation = 0.0F;
                float baseHeight = 0.0F;
                float totalEffect = 0.0F;
                int i1 = 2;
                Biome biome = this.biomesForGeneration[k + 2 + (l + 2) * 10];

                for (int chunkXOffset = -2; chunkXOffset <= 2; ++chunkXOffset) {
                    for (int chunkZOffset = -2; chunkZOffset <= 2; ++chunkZOffset) {
                        Biome otherBiome = this.biomesForGeneration[k + chunkXOffset + 2 + (l + chunkZOffset + 2) * 10];
                        float otherBiomeBaseHeight = this.biomeDepthOffSet +
                                otherBiome.getBaseHeight() * this.biomeDepthWeight;
                        float otherBiomeHeightVariation = this.biomeScaleOffset +
                                otherBiome.getHeightVariation() * this.biomeScaleWeight;

                        if (this.terrainType == WorldType.AMPLIFIED && otherBiomeBaseHeight > 0.0F) {
                            otherBiomeBaseHeight = 1.0F + otherBiomeBaseHeight * 2.0F;
                            otherBiomeHeightVariation = 1.0F + otherBiomeHeightVariation * 4.0F;
                        }

                        float heightEffect = this.biomeWeights[chunkXOffset + 2 + (chunkZOffset + 2) * 5] /
                                (otherBiomeBaseHeight + 2.0F);

                        if (otherBiome.getBaseHeight() > biome.getBaseHeight()) {
                            heightEffect /= 2.0F;
                        }

                        heightVariation += otherBiomeHeightVariation * heightEffect;
                        baseHeight += otherBiomeBaseHeight * heightEffect;
                        totalEffect += heightEffect;
                    }
                }

                heightVariation = heightVariation / totalEffect;
                baseHeight = baseHeight / totalEffect;
                heightVariation = heightVariation * 0.9F + 0.1F;
                baseHeight = (baseHeight * 4.0F - 1.0F) / 8.0F;
                double localDepthNoise = this.depthRegion[depthRegionPosition] / 8000.0D;
                ++depthRegionPosition;

                // Lots of renormalization
                if (localDepthNoise < 0.0D) {
                    localDepthNoise *= -0.3D;
                }

                localDepthNoise = localDepthNoise * 3.0D - 2.0D;

                if (localDepthNoise < 0.0D) {
                    localDepthNoise /= 2.0D;

                    if (localDepthNoise < -1.0D) {
                        localDepthNoise = -1.0D;
                    }

                    localDepthNoise /= 2.8D;
                } else {
                    if (localDepthNoise > 1.0D) {
                        localDepthNoise = 1.0D;
                    }

                    localDepthNoise /= 8.0D;
                }

                double localHeight = baseHeight;
                double localHeightVariation = heightVariation;
                localHeight = localHeight + localDepthNoise * 0.2D;
                localHeight = localHeight * this.baseSize / 8.0D;
                double d0 = this.baseSize + localHeight * 4.0D;

                for (int l1 = 0; l1 < 33; ++l1) {
                    double d1 = ((double) l1 - d0) * this.heightStretch * 128.0D / 256.0D / localHeightVariation;

                    if (d1 < 0.0D) {
                        d1 *= 4.0D;
                    }

                    double min = this.minLimitRegion[heightMapPosition] / this.lowerLimitScale;
                    double max = this.maxLimitRegion[heightMapPosition] / this.upperLimitScale;
                    double ratio = (this.mainNoiseRegion[heightMapPosition] / 10.0D + 1.0D) / 2.0D;
                    double height = MathHelper.clampedLerp(min, max, ratio) - d1;

                    if (l1 > 29) {
                        double d6 = (float) (l1 - 29) / 3.0F;
                        height = height * (1.0D - d6) + -10.0D * d6;
                    }

                    this.heightMap[heightMapPosition] = height;
                    ++heightMapPosition;
                }
            }
        }
    }

    @Override
    public void populate(int x, int z) {
        int i = x * 16;
        int j = z * 16;
        BlockPos blockpos = new BlockPos(i, 0, j);
        Biome biome = this.world.getBiome(blockpos.add(16, 0, 16));

        biome.decorate(this.world, this.rand, blockpos);
    }

    /**
     * Called to generate additional structures after initial worldgen, used by ocean monuments
     */
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        return false;
    }

    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        Biome biome = this.world.getBiome(pos);

        return biome.getSpawnableList(creatureType);
    }

    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
        return false;
    }

    @Nullable
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position,
                                           boolean findUnexplored) {
        return null;
    }

    /**
     * Recreates data about structures intersecting given chunk (used for example by getPossibleCreatures), without
     * placing any blocks. When called for the first time before any chunk is generated - also initializes the internal
     * state needed by getPossibleCreatures.
     */
    public void recreateStructures(Chunk chunkIn, int x, int z) {}
}
