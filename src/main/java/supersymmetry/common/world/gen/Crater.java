package supersymmetry.common.world.gen;

import static supersymmetry.common.blocks.SuSyBlocks.DEPOSIT_BLOCK;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;

import supersymmetry.common.blocks.BlockDeposit;

/**
 * Generates impact craters on planetary surfaces.
 * Moved from PlanetChunkGenerator for better modularity.
 */
public class Crater extends MapGenBase {

    public static final Block CRATER_DEPOSIT = DEPOSIT_BLOCK.getState(BlockDeposit.DepositBlockType.LUNAR_CRATER)
            .getBlock();
    private static final IBlockState AIR = Blocks.AIR.getDefaultState();

    private final IBlockState stone;
    private final IBlockState breccia;
    private final IBlockState impactMelt;
    private final IBlockState impactEjecta;

    /**
     * Constructor for crater generation with custom materials.
     *
     * @param stone        The base stone material for the planet
     * @param breccia      The brecciated material found in craters
     * @param impactMelt   The impact melt material
     * @param impactEjecta The ejecta material
     */
    public Crater(IBlockState stone, IBlockState breccia, IBlockState impactMelt, IBlockState impactEjecta) {
        this.stone = stone;
        this.breccia = breccia;
        this.impactMelt = impactMelt;
        this.impactEjecta = impactEjecta;
    }

    /**
     * Generates craters in the given chunk.
     * Checks surrounding chunks for crater centers that might affect this chunk.
     *
     * @param worldIn The world
     * @param chunkX  Chunk X coordinate
     * @param chunkZ  Chunk Z coordinate
     * @param primer  The chunk primer to modify
     */
    @Override
    public void generate(World worldIn, int chunkX, int chunkZ, ChunkPrimer primer) {
        // Check surrounding chunks for crater centers
        for (int cx = chunkX - 2; cx <= chunkX + 2; cx++) {
            for (int cz = chunkZ - 2; cz <= chunkZ + 2; cz++) {
                Random craterRand = new Random(worldIn.getSeed() +
                        (long) cx * 341873128712L + (long) cz * 132897987541L);

                // Low probability of crater per chunk
                if (craterRand.nextDouble() < 0.015) {
                    // Random position within the chunk
                    int centerX = cx * 16 + craterRand.nextInt(16);
                    int centerZ = cz * 16 + craterRand.nextInt(16);
                    int diameter = 8 + craterRand.nextInt(35);

                    applyCraterToChunk(primer, chunkX, chunkZ, centerX, centerZ, diameter, craterRand);
                }
            }
        }
    }

    /**
     * Applies crater modifications to a chunk.
     *
     * @param primer        The chunk primer to modify
     * @param chunkX        The chunk X coordinate
     * @param chunkZ        The chunk Z coordinate
     * @param craterCenterX The crater center X in world coordinates
     * @param craterCenterZ The crater center Z in world coordinates
     * @param diameter      The crater diameter
     * @param craterRand    Random instance for crater variations
     */
    private void applyCraterToChunk(ChunkPrimer primer, int chunkX, int chunkZ,
                                    int craterCenterX, int craterCenterZ,
                                    int diameter, Random craterRand) {
        int radius = diameter / 2;
        boolean isComplex = diameter >= 20;
        int depth = isComplex ? (int) (radius * 0.4) : (int) (radius * 0.6);

        // Ensure minimum depth
        if (depth < 2) depth = 2;

        // Only process blocks within this chunk
        int chunkStartX = chunkX * 16;
        int chunkStartZ = chunkZ * 16;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkStartX + x;
                int worldZ = chunkStartZ + z;

                double distance = Math.sqrt(
                        (worldX - craterCenterX) * (worldX - craterCenterX) +
                                (worldZ - craterCenterZ) * (worldZ - craterCenterZ));

                // Find surface height
                int surfaceY = findSurfaceY(primer, x, z);
                if (surfaceY < 0) continue;

                // Apply crater excavation
                if (distance <= radius) {
                    excavateCrater(primer, x, z, surfaceY, distance, radius, depth, isComplex, craterRand);
                }
                // Apply ejecta blanket
                else if (distance > radius && distance < radius * 2) {
                    applyEjectaBlanket(primer, x, z, surfaceY, distance, radius);
                }
            }
        }
    }

    /**
     * Excavates the crater and fills with appropriate materials.
     *
     * @param primer    The chunk primer
     * @param x         Local X coordinate
     * @param z         Local Z coordinate
     * @param surfaceY  Surface height
     * @param distance  Distance from crater center
     * @param radius    Crater radius
     * @param depth     Maximum crater depth
     * @param isComplex Whether this is a complex crater
     * @param rand      Random instance
     */
    private void excavateCrater(ChunkPrimer primer, int x, int z, int surfaceY,
                                double distance, int radius, int depth,
                                boolean isComplex, Random rand) {
        double normalizedDist = distance / radius;
        int craterDepth;

        // Calculate crater depth based on profile
        if (isComplex && normalizedDist < 0.3) {
            craterDepth = depth; // Flat floor for complex craters
        } else if (isComplex) {
            craterDepth = (int) (depth * (1 - Math.pow((normalizedDist - 0.3) / 0.7, 1.5)));
        } else {
            craterDepth = (int) (depth * (1 - normalizedDist * normalizedDist));
        }

        // Ensure minimum depth of at least 1
        if (craterDepth < 1) craterDepth = 1;

        int floorY = surfaceY - craterDepth;

        // Ensure floor is above bedrock
        if (floorY <= 2) floorY = 3;

        // Excavate crater - remove all blocks from surface down to floor
        for (int y = surfaceY; y > floorY; y--) {
            primer.setBlockState(x, y, z, AIR);
        }

        // CRITICAL: Always place the floor block - this prevents holes
        primer.setBlockState(x, floorY, z, impactEjecta);

        // Determine subsurface material based on distance from center
        IBlockState subsurfaceMaterial;
        int subsurfaceDepth;

        if (distance < radius * 0.3) {
            // Central crater - impact melt
            subsurfaceMaterial = impactMelt;
            subsurfaceDepth = 3; // Increased from 2
        } else if (normalizedDist < 0.8) {
            // Mid crater - breccia
            subsurfaceMaterial = breccia;
            subsurfaceDepth = 3; // Increased from 2
        } else {
            // Outer crater - stone
            subsurfaceMaterial = stone;
            subsurfaceDepth = 2;
        }

        // Fill subsurface layers beneath the floor
        for (int y = floorY - 1; y > floorY - 1 - subsurfaceDepth && y > 2; y--) {
            primer.setBlockState(x, y, z, subsurfaceMaterial);
        }

        // Add fractured bedrock beneath crater floor
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

    /**
     * Applies the ejecta blanket around the crater rim.
     *
     * @param primer   The chunk primer
     * @param x        Local X coordinate
     * @param z        Local Z coordinate
     * @param surfaceY Surface height
     * @param distance Distance from crater center
     * @param radius   Crater radius
     */
    private void applyEjectaBlanket(ChunkPrimer primer, int x, int z, int surfaceY,
                                    double distance, int radius) {
        double ejectaHeight = (radius * 2 - distance) / radius * 3;
        int ejectaBlocks = (int) ejectaHeight;

        for (int y = 0; y < ejectaBlocks && surfaceY + y < 255; y++) {
            primer.setBlockState(x, surfaceY + y + 1, z, impactEjecta);
        }
    }

    /**
     * Finds the surface Y coordinate at the given x, z position.
     *
     * @param primer The chunk primer
     * @param x      Local X coordinate
     * @param z      Local Z coordinate
     * @return Surface Y coordinate, or -1 if not found
     */
    private int findSurfaceY(ChunkPrimer primer, int x, int z) {
        for (int y = 255; y >= 0; y--) {
            IBlockState state = primer.getBlockState(x, y, z);
            if (state != AIR && state != null) {
                return y;
            }
        }
        return -1;
    }

    /**
     * Gets the crater probability per chunk.
     * Can be overridden for different crater densities.
     *
     * @return Probability value between 0.0 and 1.0
     */
    protected double getCraterProbability() {
        return 0.015;
    }

    /**
     * Gets the minimum crater diameter.
     *
     * @return Minimum diameter in blocks
     */
    protected int getMinCraterDiameter() {
        return 8;
    }

    /**
     * Gets the maximum additional crater diameter.
     *
     * @return Maximum additional diameter to add to minimum
     */
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
        return impactEjecta;
    }
}
