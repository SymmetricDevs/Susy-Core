package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlocksS2BMRF extends VariantBlock<BlocksS2BMRF.S2BMRFBlockType> {

    public BlocksS2BMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("s2bmrf_blocks");
    }

    public static enum S2BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        S2BMRF1("s2bmrf1", 2),
        S2BMRF2("s2bmrf2", 2),
        S2BMRF3("s2bmrf3", 2),
        S2BMRF4("s2bmrf4", 2),
        S2BMRF5("s2bmrf5", 2),
        S2BMRF6("s2bmrf6", 2),
        S2BMRF7("s2bmrf7", 2),
        S2BMRF8("s2bmrf8", 2),
        S2BMRF9("s2bmrf9", 2),
        S2BMRF10("s2bmrf10", 2),
        S2BMRF11("s2bmrf11", 2),
        S2BMRF12("s2bmrf12", 2),
        S2BMRF13("s2bmrf13", 2),
        S2BMRF14("s2bmrf14", 2),
        S2BMRF15("s2bmrf15", 2),
        S2BMRF16("s2bmrf16", 2);

        private final String name;
        private final int harvestLevel;

        private S2BMRFBlockType(String name, int harvestLevel) {
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
