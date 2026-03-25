package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlocksBMRF extends VariantBlock<BlocksBMRF.BMRFBlockType> {

    public BlocksBMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("bmrf_blocks");
    }

    public static enum BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        BMRF1("bmrf1", 2),
        BMRF2("bmrf2", 2),
        BMRF3("bmrf3", 2),
        BMRF4("bmrf4", 2),
        BMRF5("bmrf5", 2),
        BMRF6("bmrf6", 2),
        BMRF7("bmrf7", 2),
        BMRF8("bmrf8", 2),
        BMRF9("bmrf9", 2);

        private final String name;
        private final int harvestLevel;

        private BMRFBlockType(String name, int harvestLevel) {
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
