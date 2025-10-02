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
        WHITEINDUSTRIALCONCRETE("whiteindustrialconcrete", 2),

        DOTTED_PANEL("dottedpanel", 2),
        DOTTED_PANEL_BORDER("dottedpanelborder", 2),
        DOTTED_PANEL_COMB("dottedpanelcomb", 2),
        DOTTED_PANEL_GRID("dottedpanelgrid", 2),

        INDUSTRIAL_CINDER_BRICKS("industrialcinderbricks", 2),
        INDUSTRIAL_CINDER_BRICKS_CEMENT("industrialcinderbrickscement", 2),
        INDUSTRIAL_CINDER_BRICKS_CEMENT_GREY("industrialcinderbrickscementgray", 2),
        INDUSTRIAL_CINDER_BRICKS_DARK("industrialcinderbricksdark", 2),

        INDUSTRIAL_CINDER_BRICKS_DARK_GREY("industrialcinderbricksdarkgrey", 2),
        INDUSTRIAL_CINDER_BRICKS_GREY("industrialcinderbricksgrey", 2),
        SMOOTH_INDUSTRIAL_CONCRETE("smoothindustrialconcrete", 2),
        SMOOTH_INDUSTRIAL_CONCRETE_GREY("smoothindustrialconcretegrey", 2);

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
