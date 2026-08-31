package supersymmetry.common.blocks.active;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;

import gregtech.common.blocks.BlockWireCoil;

public class BlockActiveWireCoil extends RedstoneActiveBlock<BlockWireCoil.CoilType> {

    public BlockActiveWireCoil() {
        this(false);
    }

    protected BlockActiveWireCoil(boolean inverted) {
        super(Material.IRON, inverted);
        setTranslationKey("wire_coil_active");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(BlockWireCoil.CoilType.CUPRONICKEL));
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
    protected boolean isBloomEnabled(BlockWireCoil.CoilType value) {
        return true;
    }
}
