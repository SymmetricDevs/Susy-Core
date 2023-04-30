package supersymmetry.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public class BlockStructural extends VariantBlock<BlockStructural.StructuralBlockType> {

    public BlockStructural() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("structural_block");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(StructuralBlockType.BASE_STRUCTURAL_BLOCK));
    }

    public static enum StructuralBlockType implements IStringSerializable, IStateHarvestLevel {
        BASE_STRUCTURAL_BLOCK("base_structural_block", 1),
        STRUCTURAL_BLOCK_LOW("structural_block_low", 1),
        STRUCTURAL_BLOCK_LOWLIGHT("structural_block_lowlight", 1);

        private final String name;
        private final int harvestLevel;

        private StructuralBlockType(String name, int harvestLevel) {
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
