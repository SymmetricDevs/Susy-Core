package supersymmetry.common.blocks;

import static net.minecraft.block.material.Material.IRON;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;

public class BlockActiveCoolingCoil extends RedstoneActiveBlock<BlockCoolingCoil.CoolingCoilType> {

    public BlockActiveCoolingCoil() {
        this(false);
    }

    protected BlockActiveCoolingCoil(boolean inverted) {
        super(IRON, inverted);
        setTranslationKey("cooling_coil_active");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(BlockCoolingCoil.CoolingCoilType.MANGANESE_IRON_ARSENIC_PHOSPHIDE));
    }

    @NotNull @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    protected boolean isBloomEnabled(BlockCoolingCoil.CoolingCoilType value) {
        return true;
    }
}
