package supersymmetry.common.world.gen;

import static supersymmetry.common.blocks.SuSyBlocks.DEPOSIT_BLOCK;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;

import supersymmetry.common.blocks.BlockDeposit;
import supersymmetry.common.world.SuSyBiomes;

public abstract class CraterBase extends MapGenBase {

    public static final Block CRATER_DEPOSIT = DEPOSIT_BLOCK.getState(BlockDeposit.DepositBlockType.LUNAR_CRATER)
            .getBlock();
    protected static final IBlockState AIR = Blocks.AIR.getDefaultState();

    protected final IBlockState stone;
    protected final IBlockState breccia;
    protected final IBlockState impactMelt;
    protected final IBlockState defaultImpactEjecta;

    protected Biome[] cachedBiomes;
    private final long seedSalt;

    public CraterBase(IBlockState stone, IBlockState breccia, IBlockState impactMelt, IBlockState impactEjecta,
                      long seedSalt) {
        this.stone = stone;
        this.breccia = breccia;
        this.impactMelt = impactMelt;
        this.defaultImpactEjecta = impactEjecta;
        this.seedSalt = seedSalt;
    }

    // CraterBase.java
    @Override
    public void generate(World worldIn, int chunkX, int chunkZ, ChunkPrimer primer) {
        this.cachedBiomes = worldIn.getBiomeProvider().getBiomes(null, chunkX * 16, chunkZ * 16, 16, 16);

        // Search radius must cover ejecta blanket: (maxRadius * 2) / 16 chunks + 1
        int maxRadius = (getMinCraterDiameter() + getMaxAdditionalDiameter()) / 2;
        int searchRadius = (maxRadius * 2 / 16) + 2;

        for (int cx = chunkX - searchRadius; cx <= chunkX + searchRadius; cx++) {
            for (int cz = chunkZ - searchRadius; cz <= chunkZ + searchRadius; cz++) {
                Random craterRand = new Random(worldIn.getSeed() + seedSalt +
                        (long) cx * 341873128712L + (long) cz * 132897987541L);

                if (craterRand.nextDouble() < getCraterProbability()) {
                    int centerX = cx * 16 + craterRand.nextInt(16);
                    int centerZ = cz * 16 + craterRand.nextInt(16);
                    int diameter = getMinCraterDiameter() + craterRand.nextInt(getMaxAdditionalDiameter());

                    applyCraterToChunk(primer, chunkX, chunkZ, centerX, centerZ, diameter, craterRand);
                }
            }
        }
    }

    protected void applyCraterToChunk(ChunkPrimer primer, int chunkX, int chunkZ,
                                      int craterCenterX, int craterCenterZ,
                                      int diameter, Random craterRand) {
        int radius = diameter / 2;
        int depth = computeDepth(radius);
        if (depth < 2) depth = 2;

        int chunkStartX = chunkX * 16;
        int chunkStartZ = chunkZ * 16;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkStartX + x;
                int worldZ = chunkStartZ + z;

                double distance = Math.sqrt(
                        (worldX - craterCenterX) * (worldX - craterCenterX) +
                                (worldZ - craterCenterZ) * (worldZ - craterCenterZ));

                int surfaceY = findSurfaceY(primer, x, z);
                if (surfaceY < 0) continue;

                IBlockState biomeEjecta = getBiomeEjecta(x, z);

                if (distance <= radius) {
                    excavateCrater(primer, x, z, surfaceY, distance, radius, depth, craterRand, biomeEjecta);
                } else if (distance < radius * 2) {
                    applyEjectaBlanket(primer, x, z, surfaceY, distance, radius, biomeEjecta);
                }
            }
        }
    }

    /**
     * Computes the maximum excavation depth for this crater type.
     */
    protected abstract int computeDepth(int radius);

    /**
     * Computes the crater floor depth at a given normalized distance from center.
     */
    protected abstract int computeFloorDepth(int maxDepth, double normalizedDist);

    protected void excavateCrater(ChunkPrimer primer, int x, int z, int surfaceY,
                                  double distance, int radius, int depth,
                                  Random rand, IBlockState biomeEjecta) {
        double normalizedDist = distance / radius;
        int craterDepth = Math.max(1, computeFloorDepth(depth, normalizedDist));
        int floorY = Math.max(3, surfaceY - craterDepth);

        for (int y = surfaceY; y > floorY; y--) {
            primer.setBlockState(x, y, z, AIR);
        }

        primer.setBlockState(x, floorY, z, biomeEjecta);

        IBlockState subsurfaceMaterial;
        int subsurfaceDepth;

        if (distance < radius * 0.3) {
            subsurfaceMaterial = impactMelt;
            subsurfaceDepth = 3;
        } else if (normalizedDist < 0.8) {
            subsurfaceMaterial = breccia;
            subsurfaceDepth = 3;
        } else {
            subsurfaceMaterial = stone;
            subsurfaceDepth = 2;
        }

        for (int y = floorY - 1; y > floorY - 1 - subsurfaceDepth && y > 2; y--) {
            primer.setBlockState(x, y, z, subsurfaceMaterial);
        }

        if (rand.nextDouble() < 0.4 && floorY > 6) {
            int fractureDepth = 3 + rand.nextInt(5);
            int fractureStart = floorY - 1 - subsurfaceDepth;
            for (int y = fractureStart; y > fractureStart - fractureDepth && y > 2; y--) {
                if (rand.nextDouble() < 0.6) {
                    primer.setBlockState(x, y, z, stone);
                }
            }
        }
    }

    protected void applyEjectaBlanket(ChunkPrimer primer, int x, int z, int surfaceY,
                                      double distance, int radius, IBlockState biomeEjecta) {
        int ejectaBlocks = (int) ((radius * 2 - distance) / radius * 3);
        for (int y = 0; y < ejectaBlocks && surfaceY + y < 255; y++) {
            primer.setBlockState(x, surfaceY + y + 1, z, biomeEjecta);
        }
    }

    protected IBlockState getBiomeEjecta(int x, int z) {
        if (cachedBiomes != null && cachedBiomes.length > 0) {
            int index = z * 16 + x;
            if (index >= 0 && index < cachedBiomes.length) {
                Biome biome = cachedBiomes[index];
                if (biome != null) {
                    return SuSyBiomes.getCraterBlock(biome);
                }
            }
        }
        return defaultImpactEjecta;
    }

    protected int findSurfaceY(ChunkPrimer primer, int x, int z) {
        for (int y = 255; y >= 0; y--) {
            IBlockState state = primer.getBlockState(x, y, z);
            if (state != AIR && state != null) return y;
        }
        return -1;
    }

    protected double getCraterProbability() {
        return 0.015;
    }

    protected int getMinCraterDiameter() {
        return 8;
    }

    protected int getMaxAdditionalDiameter() {
        return 35;
    }

    public IBlockState getBreccia() {
        return breccia;
    }

    public IBlockState getImpactMelt() {
        return impactMelt;
    }

    public IBlockState getImpactEjecta() {
        return defaultImpactEjecta;
    }
}
