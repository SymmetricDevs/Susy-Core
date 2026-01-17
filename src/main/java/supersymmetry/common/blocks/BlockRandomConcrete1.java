package supersymmetry.common.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlockRandomConcrete1 extends VariantBlock<BlockRandomConcrete1.BlockRandomConcreteType> {

    public BlockRandomConcrete1() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey("random_concrete1");
    }

    public static enum BlockRandomConcreteType implements IStringSerializable, IStateHarvestLevel {

        SMOOTH_INDUSTRIAL_CONCRETE_WHITE("smoothindustrialconcretewhite", 2);

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
