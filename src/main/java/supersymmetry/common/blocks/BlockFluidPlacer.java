package supersymmetry.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

//appease ICBM
public class BlockFluidPlacer extends VariantBlock<BlockFluidPlacer.FluidType> {

    public BlockFluidPlacer() {
        super(Material.ROCK);
        setTranslationKey("fluid_placer");
        setHardness(0.0f);
        setResistance(0.0f);
    }

    public enum FluidType implements IStringSerializable, IStateHarvestLevel {
        MERCURY("mercury_fluid", 0);

        private final String name;
        private final int harvestLevel;

        FluidType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Override
        public int getHarvestLevel(IBlockState iBlockState) {
            return this.harvestLevel;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state,
                                EntityLivingBase placer, ItemStack stack) {
        if (!world.isRemote) {
            world.setBlockState(pos, SuSyBlocks.MERCURY_FLUID.getDefaultState(), 3);
        }
    }
}
