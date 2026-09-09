package supersymmetry.common.blocks.active;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;

import supersymmetry.common.blocks.BlockElectrodeAssembly;

public class BlockActiveElectrodeAssembly extends RedstoneActiveBlock<BlockElectrodeAssembly.ElectrodeAssemblyType> {

    public BlockActiveElectrodeAssembly() {
        this(false);
    }

    protected BlockActiveElectrodeAssembly(boolean inverted) {
        super(Material.IRON, inverted);
        setTranslationKey("electrode_assembly_active");
        setHardness(3.0f);
        setResistance(3.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 1);
        setDefaultState(getState(BlockElectrodeAssembly.ElectrodeAssemblyType.CARBON));
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Override
    public boolean canCreatureSpawn(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                    @NotNull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    protected boolean isBloomEnabled(BlockElectrodeAssembly.ElectrodeAssemblyType value) {
        return true;
    }
}
