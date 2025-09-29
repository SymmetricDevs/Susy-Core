package supersymmetry.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

public class BlockRandomConcrete extends VariantBlock<BlockRandomConcrete.BlockRandomConcreteType> {
    public BlockRandomConcrete() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("random_concrete");
    }

    public static enum BlockRandomConcreteType implements IStringSerializable, IStateHarvestLevel {

        GREYINDUSTRIALCONCRETE("greyindustrialconcrete", 2),
        MOSSYINDUSTRIALCONCRETE("mossyindustrialconcrete", 2),
        SILVERINDUSTRIALCONCRETE("silverindustrialconcrete", 2),
        WHITEINDUSTRIALCONCRETE("whiteindustrialconcrete", 2);

        private final String name;
        private final int harvestLevel;

        private BlockRandomConcreteType(String name, int harvestLevel) {
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
