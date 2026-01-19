package supersymmetry.common.world.gen;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;

import com.google.common.base.MoreObjects;

import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SusyStoneVariantBlock;

public class MapGenLunarLavaTube extends MapGenBase {

    protected static final IBlockState AIR = Blocks.AIR.getDefaultState();
    protected static final IBlockState LAVA = Blocks.LAVA.getDefaultState();
    public static final IBlockState BASALT = MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH)
            .getState(StoneVariantBlock.StoneType.BASALT);
    public static final Block PIT = Blocks.END_PORTAL_FRAME;

    // modified from net.minecraft.world.gen.MapGenCaves
    protected void addTunnel(long seed, int x, int z, ChunkPrimer primer, double startX, double startY, double startZ,
                             float widthDiff, float yaw, float pitch, int currentLength, int length, double squish,
                             double width) {
        double centerX = x * 16 + 8;
        double centerZ = z * 16 + 8;
        float deltaYaw = 0.0F;
        float deltaPitch = 0.0F;
        Random random = new Random(seed);

        if (length <= 0) {
            int i = this.range * 16 - 16;
            length = i - random.nextInt(i / 4);
        }

        int j = random.nextInt(length);

        boolean deep = random.nextInt(6) == 0;
        for (; currentLength < length; ++currentLength) {
            double currentWidth = width + widthDiff;
            double height = currentWidth * squish;
            float cos = MathHelper.cos(pitch);
            float sin = MathHelper.sin(pitch);
            startX += MathHelper.cos(yaw) * cos;
            startY += sin;
            startZ += MathHelper.sin(yaw) * cos;

            if (deep) {
                pitch *= 0x.fp0f;
                if (random.nextInt(0x20) == 0) deep = false;
            } else {
                pitch *= 0.7F;
                if (random.nextInt(0x100) == 0) deep = true;
            }

            pitch += deltaPitch * 0.1F;
            yaw += deltaYaw * 0.1F;
            deltaPitch *= 0.9F;
            deltaYaw *= 0x.cp0f;
            widthDiff *= 0x.fp0f;
            if (currentLength > length * 0x.fp0) width *= 0x.f8p0;
            // generate upwards for better lava flow merge
            deltaPitch += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 1.0F + 0x.08p0f;
            deltaYaw += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 0x.cp0f;
            widthDiff += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 0x.8p0f;
            if (startY + height > 0x38) deltaPitch -= 0x.1p0f;
            if (startY + height > 0x3c) {
                deltaPitch -= 0x.4p0f;
                if (deep) {
                    pitch = 0;
                    deep = false;
                }
            }
            if (startY + height > 0x40) pitch = -0x.1p0f;

            if (width < 2) deltaPitch -= 1;

            if (currentLength == j && width > 5) {
                double large = width * (1 - random.nextFloat() * random.nextFloat() * 0x.6p0);
                double small = MathHelper.sqrt(width * width - large * large);
                if (large < 3) large = 3;
                if (small < 3) small = 3;
                double newLen = (length - currentLength) * random.nextFloat(0x.8p0f, 0x1.4p0f);
                this.addTunnel(random.nextLong(), x, z, primer, startX, startY, startZ, (float) (width - large),
                        yaw + random.nextFloat() * 0x.8p0f - 0x.4p0f,
                        pitch * 0x.ep0f + random.nextFloat() * 0x.2p0f - 0x.1p0f, currentLength, length, squish, large);
                this.addTunnel(random.nextLong(), x, z, primer, startX, startY, startZ,
                        (float) (width - small) * 0x.cp0f,
                        yaw + random.nextFloat() * 0x4p0f - 0x2p0f,
                        pitch / 2.0F + random.nextFloat() * 0x.8p0f - 0x.4p0f, (int) (newLen * currentLength / length),
                        (int) newLen, squish / 2 + 0x.8p0, small);
                return;
            }

            if (random.nextInt(4) != 0) {
                double offsetX = startX - centerX;
                double offsetZ = startZ - centerZ;
                double nearEnd = length - currentLength;
                double d7 = currentWidth + widthDiff + 1f + 16.0F;

                if (offsetX * offsetX + offsetZ * offsetZ - nearEnd * nearEnd > d7 * d7) {
                    return;
                }

                double basaltFillLevel = currentLength < length * 0x.1p0 ?
                        (0x.1p0 - (double) currentLength / length) :
                        currentLength > length * 0x.fp0 ?
                                (-0x.fp0 + (double) currentLength / length) :
                                0; // 0 ~ 0x.1
                basaltFillLevel *= 0x180 * basaltFillLevel; // 0 ~ 0x1.8
                double stoneFillLevel = basaltFillLevel * basaltFillLevel - 0x1.2p0; // -0x1.2 ~ 0x1.2;

                long localRandomSeed = random.nextLong();
                boolean canHavePit = random.nextInt(8) == 0;

                // carving spheres
                if (startX >= centerX - 16.0D - currentWidth * 2.0D &&
                        startZ >= centerZ - 16.0D - currentWidth * 2.0D &&
                        startX <= centerX + 16.0D + currentWidth * 2.0D &&
                        startZ <= centerZ + 16.0D + currentWidth * 2.0D) {
                    int x1 = MathHelper.floor(startX - currentWidth) - x * 16 - 1;
                    int x2 = MathHelper.floor(startX + currentWidth) - x * 16 + 1;
                    int y1 = MathHelper.floor(startY - height) - 1;
                    int y2 = MathHelper.floor(startY + height) + 1;
                    int z1 = MathHelper.floor(startZ - currentWidth) - z * 16 - 1;
                    int z2 = MathHelper.floor(startZ + currentWidth) - z * 16 + 1;

                    if (x1 < 0) x1 = 0;
                    if (x2 > 16) x2 = 16;
                    if (y1 < 1) y1 = 1;
                    if (y2 > 248) y2 = 248;
                    if (z1 < 0) z1 = 0;
                    if (z2 > 16) z2 = 16;

                    // for consistency
                    Random localRandom = new Random(localRandomSeed);

                    int x3 = MathHelper.floor(startX) - x * 16;
                    int z3 = MathHelper.floor(startZ) - z * 16;
                    if (0 <= x3 && x3 < 16 && 0 < z3 && z3 < 16 && width > 0x3 && localRandom.nextInt(0x4) == 1 &&
                            y2 > primer.findGroundBlockIdx(x3, z3 - 1) - 2) {
                        fillBlock(primer, x3, 0x60, z3, null, AIR,
                                PIT.getStateFromMeta(width > 0xa ? 7 : (int) (width - 3)));
                    }

                    // lx: local x
                    for (int localX = x1; localX < x2; ++localX) {
                        double distX = ((double) (localX + x * 16) + 0.5D - startX) / currentWidth;

                        for (int localZ = z1; localZ < z2; ++localZ) {
                            double distZ = ((double) (localZ + z * 16) + 0.5D - startZ) / currentWidth;
                            boolean foundTop = false;

                            if (distX * distX + distZ * distZ < 1.0D) {
                                int localY = y2;
                                float lavacicles = y2 > 0x3c ? 1 : localRandom.nextFloat(0x.ap0f, 0x1.4p0f);
                                for (; localY > y1; --localY) {
                                    double distY = ((double) (localY - 1) + 0.5D - startY) / height;
                                    if (distY > 0) distY *= lavacicles;

                                    if (distX * distX + distY * distY + distZ * distZ < 1.0D) {
                                        IBlockState state = primer.getBlockState(localX, localY, localZ);
                                        IBlockState up = MoreObjects
                                                .firstNonNull(primer.getBlockState(localX, localY + 1, localZ), AIR);

                                        if (isTopBlock(primer, localX, localY, localZ, x, z)) {
                                            foundTop = true;
                                        }

                                        double edge = distX * distX + distZ * distZ < 0x.5p0 ? 0 :
                                                distX * distX + distZ * distZ - 0x.5p0;
                                        if (distY > stoneFillLevel) {
                                            if (distY - edge > -0.5D + basaltFillLevel) {
                                                digBlock(primer, localX, localY, localZ, x, z, foundTop, state, up);
                                            } else {
                                                fillBlock(primer, localX, localY, localZ, true, state, BASALT);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected boolean canReplaceBlock(IBlockState state, IBlockState up) {
        Block block = state.getBlock();
        return block == SuSyBlocks.SUSY_STONE_BLOCKS.get(SusyStoneVariantBlock.StoneVariant.SMOOTH) ||
                block == SuSyBlocks.REGOLITH ||
                block == MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH) || block == Blocks.AIR;
    }

    @Override
    public void generate(World worldIn, int x, int z, ChunkPrimer primer) {
        this.range = 32; // maybe it's too large?
        super.generate(worldIn, x, z, primer);
    }

    /**
     * Recursively called by generate()
     */
    protected void recursiveGenerate(World worldIn, int chunkX, int chunkZ, int originalX, int originalZ,
                                     ChunkPrimer chunkPrimerIn) {
        int i = 1;

        if (this.rand.nextInt(16) == 0) {
            i = rand.nextInt(2);
        }

        if (this.rand.nextInt(0x30) != 0) {
            i = 0;
        }

        for (int j = 0; j < i; ++j) {
            double startX = chunkX * 16 + this.rand.nextInt(16);
            double startY = this.rand.nextInt(this.rand.nextInt(16) + 4) + 4;
            double startZ = chunkZ * 16 + this.rand.nextInt(16);
            int k = 1;

            if (this.rand.nextInt(4) == 0) {
                this.addRoom(this.rand.nextLong(), originalX, originalZ, chunkPrimerIn, startX, startY, startZ);
                k += this.rand.nextInt(4);
            }

            for (int l = 0; l < k; ++l) {
                float yaw = this.rand.nextFloat() * ((float) Math.PI * 2F);
                float pitch = (this.rand.nextFloat() - 0.5F) * 0x0.2p0f;
                float f2 = this.rand.nextFloat() * 0x.4p0f;

                this.addTunnel(this.rand.nextLong(), originalX, originalZ, chunkPrimerIn, startX, startY, startZ, f2,
                        yaw, pitch, 0, 0, 0x.cp0,
                        rand.nextFloat() * (rand.nextFloat() * 0x10) + rand.nextFloat() * 2 + 2);
            }
        }
    }

    private void addRoom(long l, int originalX, int originalZ, ChunkPrimer chunkPrimerIn, double d0, double d1,
                         double d2) {}

    private boolean isTopBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ) {
        net.minecraft.world.biome.Biome biome = world.getBiome(new BlockPos(x + chunkX * 16, 0, z + chunkZ * 16));
        IBlockState state = data.getBlockState(x, y, z);
        return state.getBlock() == biome.topBlock;
    }

    protected void digBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ, boolean foundTop,
                            IBlockState state, IBlockState up) {
        net.minecraft.world.biome.Biome biome = world.getBiome(new BlockPos(x + chunkX * 16, 0, z + chunkZ * 16));
        IBlockState top = biome.topBlock;
        IBlockState filler = biome.fillerBlock;

        if (this.canReplaceBlock(state, up) || state.getBlock() == top.getBlock() ||
                state.getBlock() == filler.getBlock()) {
            if (y < 6) {
                data.setBlockState(x, y, z, BASALT);
            } else if (y < 30) {
                // Fill lower sections of lava tubes with lava
                data.setBlockState(x, y, z, LAVA);
            } else {
                data.setBlockState(x, y, z, AIR);

                if (foundTop && data.getBlockState(x, y - 1, z).getBlock() == filler.getBlock()) {
                    data.setBlockState(x, y - 1, z, top.getBlock().getDefaultState());
                }
            }
        }
    }

    // replace = null for both
    protected void fillBlock(ChunkPrimer primer, int x, int y, int z, Boolean replace, IBlockState state,
                             IBlockState toReplace) {
        if (canReplaceBlock(state, null) && (replace == null || ((state == AIR) ^ replace))) {
            primer.setBlockState(x, y, z, toReplace);
        }
    }
}
