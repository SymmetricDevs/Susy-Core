package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlocksFakeWool extends VariantBlock<BlocksFakeWool.FakeWoolBlockType> {

    public BlocksFakeWool() {
        super(Material.ROCK);
        this.setHardness(0.8F);
        this.setResistance(0.8F);
        this.setSoundType(SoundType.CLOTH);
        this.setTranslationKey("fake_wool");
    }

    // probably a better way to do this, but not gonna lie not going to expend energy on that since this works well
    // enough
    // loading time issues? what? :clueless:
    public static enum FakeWoolBlockType implements IStringSerializable, IStateHarvestLevel {

        WHITEFAKEWOOL("whitefakewool", 0),
        ORANGEFAKEWOOL("orangefakewool", 0),
        MAGENTAFAKEWOOL("magentafakewool", 0),
        LIGHTBLUEFAKEWOOL("lightbluefakewool", 0),
        YELLOWFAKEWOOL("yellowfakewool", 0),
        LIMEFAKEWOOL("limefakewool", 0),
        PINKFAKEWOOL("pinkfakewool", 0),
        GRAYFAKEWOOL("grayfakewool", 0),
        LIGHTGRAYFAKEWOOL("lightgrayfakewool", 0),
        CYANFAKEWOOL("cyanfakewool", 0),
        PURPLEFAKEWOOL("purplefakewool", 0),
        BLUEFAKEWOOL("bluefakewool", 0),
        BROWNFAKEWOOL("brownfakewool", 0),
        GREENFAKEWOOL("greenfakewool", 0),
        REDFAKEWOOL("redfakewool", 0),
        BLACKFAKEWOOL("blackfakewool", 0);

        private final String name;
        private final int harvestLevel;

        private FakeWoolBlockType(String name, int harvestLevel) {
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
