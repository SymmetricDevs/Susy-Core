package supersymmetry.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;
import supersymmetry.api.blocks.VariantRotatableBlock;

import javax.annotation.Nonnull;

public class BlockTurbineRotor extends VariantRotatableBlock<BlockTurbineRotor.BlockTurbineRotorType> {
    public BlockTurbineRotor() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("turbine_rotor");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
    }

    public enum BlockTurbineRotorType implements IStringSerializable, IStateHarvestLevel {
        STEEL("steel", 1);

        private final String name;
        private final int harvestLevel;

        BlockTurbineRotorType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Nonnull
        public String getName() {
            return this.name;
        }

        public int getHarvestLevel(IBlockState state) {
            return this.harvestLevel;
        }

        public String getHarvestTool(IBlockState state) {
            return "wrench";
        }
    }
}
