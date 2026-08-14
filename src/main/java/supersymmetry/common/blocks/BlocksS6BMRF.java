package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlocksS6BMRF extends VariantBlock<BlocksS6BMRF.S6BMRFBlockType> {

    public BlocksS6BMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("s6bmrf_blocks");
    }

    public static enum S6BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        S6BMRF1("s6bmrf1", 2),
        S6BMRF2("s6bmrf2", 2),
        S6BMRF3("s6bmrf3", 2),
        S6BMRF4("s6bmrf4", 2),
        S6BMRF5("s6bmrf5",
                2),
        S6BMRF6("s6bmrf6", 2),
        S6BMRF7("s6bmrf7", 2),
        S6BMRF8("s6bmrf8", 2),
        S6BMRF9("s6bmrf9",
                2),
        S6BMRF10("s6bmrf10", 2),
        S6BMRF11("s6bmrf11", 2),
        S6BMRF12("s6bmrf12", 2),
        S6BMRF13("s6bmrf13", 2),
        S6BMRF14("s6bmrf14", 2),
        S6BMRF15("s6bmrf15", 2),
        S6BMRF16("s6bmrf16", 2);

        private final String name;
        private final int harvestLevel;

        private S6BMRFBlockType(String name, int harvestLevel) {
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
