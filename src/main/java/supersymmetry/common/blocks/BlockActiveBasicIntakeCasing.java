package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;

public class BlockActiveBasicIntakeCasing extends RedstoneActiveBlock<BlocksActiveCasing.ActiveBlockType> {

    public BlockActiveBasicIntakeCasing() {
        this(false);
    }

    protected BlockActiveBasicIntakeCasing(boolean inverted) {
        super(Material.IRON, inverted);
        setTranslationKey("basic_intake_casing_active");
        setHardness(0.5f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(BlocksActiveCasing.ActiveBlockType.BASIC_INTAKE_CASING));
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Override
    protected boolean isBloomEnabled(BlocksActiveCasing.ActiveBlockType value) {
        return false;
    }
}
