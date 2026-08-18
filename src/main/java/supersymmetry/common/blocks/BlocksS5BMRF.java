package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlocksS5BMRF extends VariantBlock<BlocksS5BMRF.S5BMRFBlockType> {

    public BlocksS5BMRF() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("s5bmrf_blocks");
    }

    public static enum S5BMRFBlockType implements IStringSerializable, IStateHarvestLevel {

        S5BMRF1("s5bmrf1", 2),
        S5BMRF2("s5bmrf2", 2),
        S5BMRF3("s5bmrf3", 2),
        S5BMRF4("s5bmrf4", 2),
        S5BMRF5("s5bmrf5", 2),
        S5BMRF6("s5bmrf6", 2),
        S5BMRF7("s5bmrf7", 2),
        S5BMRF8("s5bmrf8", 2),
        S5BMRF9("s5bmrf9", 2),
        S5BMRF10("s5bmrf10", 2),
        S5BMRF11("s5bmrf11", 2),
        S5BMRF12("s5bmrf12", 2),
        S5BMRF13("s5bmrf13", 2),
        S5BMRF14("s5bmrf14", 2),
        S5BMRF15("s5bmrf15", 2),
        S5BMRF16("s5bmrf16", 2);

        private final String name;
        private final int harvestLevel;

        private S5BMRFBlockType(String name, int harvestLevel) {
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
