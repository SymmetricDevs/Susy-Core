package supersymmetry.common.blocks.rocketry;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import supersymmetry.api.blocks.VariantAxialRotatableBlock;
import supersymmetry.api.blocks.VariantDirectionalRotatableBlock;

public class BlockTurboPump extends VariantDirectionalRotatableBlock<BlockTurboPump.HPPType> {
    public BlockTurboPump() {
        super(Material.IRON);
        setTranslationKey("rocket_turbopump");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
    }
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }
    public enum HPPType implements IStringSerializable, IStateHarvestLevel {
        BASIC("basic",3);

        private String name;
        private int harvestLevel;

        HPPType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }
        @Override
        public int getHarvestLevel(IBlockState iBlockState) {
            return harvestLevel;
        }

        @Override
        public String getHarvestTool(IBlockState state) {
            return "wrench";
        }

        @Override
        public String getName() {
            return this.name;
        }
    }
}
