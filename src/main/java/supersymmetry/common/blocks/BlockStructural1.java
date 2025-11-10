package supersymmetry.common.blocks;

import javax.annotation.Nonnull;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlockStructural1 extends VariantBlock<BlockStructural1.StructuralBlock1Type> {

    public BlockStructural1() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("structural_block_1");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 2);
        setDefaultState(getState(StructuralBlock1Type.STRUCTURAL_BLOCK_1_EXPOSED));
    }

    public static enum StructuralBlock1Type implements IStringSerializable, IStateHarvestLevel {

        STRUCTURAL_BLOCK_1_EXPOSED("structural_block_exposed", 1),
        STRUCTURAL_BLOCK_1_EXPOSED_1("structural_block_exposed_1", 1),
        STRUCTURAL_BLOCK_1_EXPOSED_2("structural_block_exposed_2", 1),
        STRUCTURAL_BLOCK_1_DANGER_SIGN("structural_block_danger_sign", 1),
        STRUCTURAL_BLOCK_1_CABLE("structural_block_cable", 1),
        STRUCTURAL_BLOCK_1_CABLE_HORIZONTAL("structural_block_cable_horizontal", 1),
        STRUCTURAL_BLOCK_1_CABLE_JUNCTION("structural_block_cable_junction", 1),
        STRUCTURAL_BLOCK_1_PIPOCALYPSE("structural_block_pipocalypse", 1),
        STRUCTURAL_BLOCK_1_VENT("structural_block_vent", 1),
        STRUCTURAL_BLOCK_1_VENT_BROKEN("structural_block_vent_broken", 1);

        private final String name;
        private final int harvestLevel;

        private StructuralBlock1Type(String name, int harvestLevel) {
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
