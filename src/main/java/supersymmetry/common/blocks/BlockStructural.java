package supersymmetry.common.blocks;

import javax.annotation.Nonnull;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

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
        STRUCTURAL_BLOCK_LOWLIGHT("structural_block_lowlight", 1),
        STRUCTURAL_BLOCK_DANGER_A("structural_block_danger_a", 1),
        STRUCTURAL_BLOCK_DANGER_B("structural_block_danger_b", 1),
        STRUCTURAL_BLOCK_DANGER_C("structural_block_danger_c", 1),
        STRUCTURAL_BLOCK_DANGER_D("structural_block_danger_d", 1),
        STRUCTURAL_BLOCK_COLUMN("structural_block_column", 1),
        STRUCTURAL_BLOCK_COLUMN_OLD("structural_block_column_old", 1),
        STRUCTURAL_BLOCK_LIGHT("structural_block_light", 1),
        STRUCTURAL_BLOCK_LIGHT_BROKEN("structural_block_light_broken", 1),
        STRUCTURAL_BLOCK_LIGHT_CABLE("structural_block_light_cable", 1),
        STRUCTURAL_BLOCK_INSTRUMENTS("structural_block_instruments", 1),
        STRUCTURAL_BLOCK_SIGN_0("structural_block_sign_0", 1),
        STRUCTURAL_BLOCK_SIGN_1("structural_block_sign_1", 1),
        STRUCTURAL_BLOCK_SIGN_2("structural_block_sign_2", 1);

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
