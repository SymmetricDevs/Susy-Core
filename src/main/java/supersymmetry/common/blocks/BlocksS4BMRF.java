package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlocksS4BMRF extends VariantBlock<BlocksS4BMRF.S4BMRFBlockType> {

    public BlocksS4BMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("s4bmrf_blocks");
    }

    public static enum S4BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        S4BMRF1("s4bmrf1", 2),
        S4BMRF2("s4bmrf2", 2),
        S4BMRF3("s4bmrf3", 2),
        S4BMRF4("s4bmrf4", 2),
        S4BMRF5("s4bmrf5",
                2),
        S4BMRF6("s4bmrf6", 2),
        S4BMRF7("s4bmrf7", 2),
        S4BMRF8("s4bmrf8", 2),
        S4BMRF9("s4bmrf9",
                2),
        S4BMRF10("s4bmrf10", 2),
        S4BMRF11("s4bmrf11", 2),
        S4BMRF12("s4bmrf12", 2),
        S4BMRF13("s4bmrf13", 2),
        S4BMRF14("s4bmrf14", 2),
        S4BMRF15("s4bmrf15", 2),
        S4BMRF16("s4bmrf16", 2);

        private final String name;
        private final int harvestLevel;

        private S4BMRFBlockType(String name, int harvestLevel) {
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
