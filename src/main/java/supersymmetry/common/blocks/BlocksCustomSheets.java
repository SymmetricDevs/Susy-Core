package supersymmetry.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

public class BlocksCustomSheets extends VariantBlock<BlocksCustomSheets.MetalDecorationBlockType> {

    public BlocksCustomSheets() {
        super(Material.IRON);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.METAL);
        this.setTranslationKey("custom_sheets");
    }

    public static enum MetalDecorationBlockType implements IStringSerializable, IStateHarvestLevel {
        DARKWHITEMETALSHEET("darkwhitemetalsheet", 2),
        LIGHTERGRAYMETALSHEET("lightergraymetalsheet", 2);
        private final String name;
        private final int harvestLevel;

        private MetalDecorationBlockType(String name, int harvestLevel) {
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
}

