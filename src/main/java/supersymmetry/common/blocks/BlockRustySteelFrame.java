package supersymmetry.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public class BlockRustySteelFrame extends VariantBlock<BlockRustySteelFrame.RustySteelFrameVariant> {

    public BlockRustySteelFrame() {
        super(Material.IRON);
        setTranslationKey("rusty_steel_frame");
        setHardness(3.0F);
        setResistance(5.0F);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(RustySteelFrameVariant.RUSTY_STEEL_VARIANT_1));
    }


    public enum RustySteelFrameVariant implements IStringSerializable, IStateHarvestLevel {
        RUSTY_STEEL_VARIANT_1("variant_1", 1),
        RUSTY_STEEL_VARIANT_2("variant_2", 1),
        RUSTY_STEEL_VARIANT_3("variant_3", 1),
        RUSTY_STEEL_VARIANT_4("variant_4", 1),
        RUSTY_STEEL_VARIANT_5("variant_5", 1),
        RUSTY_STEEL_VARIANT_6("variant_6", 1),
        RUSTY_STEEL_VARIANT_7("variant_7", 1),
        RUSTY_STEEL_VARIANT_8("variant_8", 1);

        private final String name;
        private final int harvestLevel;

        RustySteelFrameVariant(String name, int harvestLevel) {
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
