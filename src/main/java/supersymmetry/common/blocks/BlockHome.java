package supersymmetry.common.blocks;

import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class BlockHome extends VariantBlock<BlockHome.HomeType> {

    public BlockHome() {
        super(Material.IRON);
        this.setTranslationKey("home_block");
        this.setHardness(0.5f);
        this.setSoundType(SoundType.METAL);
        this.setHarvestLevel("wrench", 2);
        this.setDefaultState(getState(HomeType.PRIMITIVE));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public boolean isBed(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable Entity player) {
        return true;
    }

    @Override
    public boolean onBlockActivated(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state,
                                    @NotNull EntityPlayer playerIn, @NotNull EnumHand hand, @NotNull EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            if (worldIn.provider.canRespawnHere() && worldIn.getBiome(pos) != net.minecraft.init.Biomes.HELL) {
                playerIn.setSpawnPoint(pos, true);
                playerIn.sendStatusMessage(new TextComponentTranslation("tile.home.allowed"), true);
            } else {
                playerIn.sendStatusMessage(new TextComponentTranslation("tile.home.denied"), true);
            }
        }
        return true;
    }

    @Override
    @Nullable
    public BlockPos getBedSpawnPosition(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos, @Nullable EntityPlayer player) {
        final int posX = pos.getX();
        final int posY = pos.getY();
        final int posZ = pos.getZ();

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                mutablePos.setPos(posX + x, posY, posZ + z);
                if (hasRoomForPlayer(world, mutablePos)) {
                    return mutablePos;
                }
            }
        }
        return null;
    }

    /**
     * @param world the world to check
     * @param pos   the position of the block
     * @return if there is room for the player to spawn at the block
     */
    private static boolean hasRoomForPlayer(@NotNull IBlockAccess world, @NotNull BlockPos pos) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(pos);
        if (world.getBlockState(mutablePos).getMaterial().isSolid()) {
            return false;
        }

        mutablePos.move(EnumFacing.UP);
        if (world.getBlockState(mutablePos).getMaterial().isSolid()) {
            return false;
        }

        mutablePos.move(EnumFacing.DOWN, 2);
        return world.getBlockState(mutablePos).isSideSolid(world, mutablePos, EnumFacing.UP);
    }

    public enum HomeType implements IStringSerializable {
        PRIMITIVE("primitive"),
        GT_BRUTALIST("gt_brutalist"),
        RENEWAL_BRUTALIST("renewal_brutalist"),
        SCIFI("scifi");


        public final String name;

        HomeType(String name) {
            this.name = name;
        }

        @NotNull
        @Override
        public String getName() {
            return this.name;
        }

        public String toString() {
            return this.getName();
        }
    }
}
