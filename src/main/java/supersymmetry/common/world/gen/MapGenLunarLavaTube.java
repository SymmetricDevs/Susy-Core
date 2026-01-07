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
import supersymmetry.common.world.SuSyBiomes;

public class MapGenLunarLavaTube extends MapGenBase {

    protected static final IBlockState AIR = Blocks.AIR.getDefaultState();
    protected static final IBlockState BASALT = MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH)
            .getState(StoneVariantBlock.StoneType.BASALT);
    // protected static final IBlockState PIT = Blocks.BEDROCK.getDefaultState();
    protected static final IBlockState PIT = Blocks.END_PORTAL_FRAME.getDefaultState();

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
            double currentWidth = width +
                    (double) (MathHelper.sin((float) currentLength * (float) Math.PI / (float) length) * widthDiff);
            double height = currentWidth * squish;
            float cos = MathHelper.cos(pitch);
            float sin = MathHelper.sin(pitch);
            startX += MathHelper.cos(yaw) * cos;
            startY += sin;
            startZ += MathHelper.sin(yaw) * cos;

            if (deep) {
                pitch *= 0x.fp0f;
            } else {
                pitch *= 0.7F;
            }

            pitch += deltaPitch * 0.1F;
            yaw += deltaYaw * 0.1F;
            deltaPitch *= 0.9F;
            deltaYaw *= 0x.cp0f;
            deltaPitch += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 1.0F + 0x.08p0f;
            deltaYaw += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 0x.cp0f;
            if (startY + height > 0x38) deltaPitch -= 0x.1p0f;
            if (startY + height > 0x3c) {
                deltaPitch -= 0x.4p0f;
                if (deep) {
                    pitch = 0;
                    deep = false;
                }
            }
            if (startY + height > 0x40) pitch = -0x.1p0f;

            if (width > 2) width *= 1 - 0x0.001p0d * currentLength / length;
            else deltaPitch -= 0x.18p0f;

            if (currentLength == j && width > 4.0F) {
                double large = width * (1 - random.nextFloat() * random.nextFloat() * 0x.6p0);
                double small = MathHelper.sqrt(width * width - large * large);
                if (large < 3) large = 3;
                if (small < 2) small = 2;
                this.addTunnel(random.nextLong(), x, z, primer, startX, startY, startZ, random.nextFloat() * 0x.2p0f,
                        yaw + random.nextFloat() * 0x.8p0f - 0x.4p0f,
                        pitch / 2.0F + random.nextFloat() * 0x.4p0f - 0x.2p0f, currentLength, length, 1.0D, large);
                this.addTunnel(random.nextLong(), x, z, primer, startX, startY, startZ, random.nextFloat() * 0x.2p0f,
                        yaw + random.nextFloat() * 0x4p0f - 0x2p0f,
                        pitch / 2.0F + random.nextFloat() * 0x.4p0f - 0x.2p0f, currentLength, length, 1.0D, small);
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

                // carving spheres?
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

                    boolean pit = false;

                    // lx: local x
                    for (int localX = x1; localX < x2; ++localX) {
                        double distX = ((double) (localX + x * 16) + 0.5D - startX) / currentWidth;

                        for (int localZ = z1; localZ < z2; ++localZ) {
                            double distZ = ((double) (localZ + z * 16) + 0.5D - startZ) / currentWidth;
                            boolean foundTop = false;

                            if (distX * distX + distZ * distZ < 1.0D) {
                                for (int localY = y2; localY > y1; --localY) {
                                    double distY = ((double) (localY - 1) + 0.5D - startY) / height;

                                    if (!pit && y2 > 0x3d &&
                                            world.getBiome(new BlockPos(localX + x * 16, localY, localZ + z * 16)) ==
                                                    SuSyBiomes.LUNAR_MARIA) {
                                        int x_ = (int) startX - x * 16;
                                        int z_ = (int) startZ - z * 16;
                                        if (0 <= x_ && x_ < 16 && 0 <= z_ && z_ < 16 && width > 3.0)
                                            fillBlock(primer, x_, 0x50, z_, false, AIR, PIT);
                                        pit = true;
                                    }

                                    if (distX * distX + distY * distY + distZ * distZ < 1.0D) {
                                        IBlockState state = primer.getBlockState(localX, localY, localZ);
                                        IBlockState up = MoreObjects
                                                .firstNonNull(primer.getBlockState(localX, localY + 1, localZ), AIR);

                                        if (isTopBlock(primer, localX, localY, localZ, x, z)) {
                                            foundTop = true;
                                        }

                                        double edge = distX * distX + distZ * distZ < 0x.cp0 ? 0 :
                                                distX * distX + distZ * distZ * 2 - 0x1.8p0;
                                        if (distY - edge > -0.5D) {
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

        if (this.rand.nextInt(32) != 0) {
            i = 0;
        }

        for (int j = 0; j < i; ++j) {
            double startX = chunkX * 16 + this.rand.nextInt(16);
            // double startY = this.rand.nextInt(this.rand.nextInt(112) + 8) + 8;
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
                        yaw, pitch, 0, 0, 1.0D,
                        rand.nextFloat() * (rand.nextFloat() * 4 + 4) + rand.nextFloat() * 2 + 2);
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
            } else {
                data.setBlockState(x, y, z, AIR);

                if (foundTop && data.getBlockState(x, y - 1, z).getBlock() == filler.getBlock()) {
                    data.setBlockState(x, y - 1, z, top.getBlock().getDefaultState());
                }
            }
        }
    }

    protected void fillBlock(ChunkPrimer primer, int x, int y, int z, boolean replace, IBlockState state,
                             IBlockState toReplace) {
        if (canReplaceBlock(state, null) && ((state == AIR) ^ replace)) {
            primer.setBlockState(x, y, z, toReplace);
        }
    }
}
