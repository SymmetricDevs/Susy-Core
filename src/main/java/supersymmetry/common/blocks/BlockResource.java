package supersymmetry.common.blocks;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public class BlockResource extends VariantBlock<BlockResource.ResourceBlockType> {

    public BlockResource() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("resource_block");
        setHardness(5.0f);
        setResistance(10.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", 1);
        setDefaultState(getState(ResourceBlockType.RESOURCE_BLOCK_BAUXITE));
    }

    public static enum ResourceBlockType implements IStringSerializable, IStateHarvestLevel {
        RESOURCE_BLOCK_BAUXITE("resource_block_bauxite", 1),
        RESOURCE_BLOCK_CALICHE("resource_block_caliche", 1),
        RESOURCE_BLOCK_ANTHRACITE("resource_block_anthracite", 1),
        RESOURCE_BLOCK_LIGNITE("resource_block_lignite", 1);

        private final String name;
        private final int harvestLevel;

        private ResourceBlockType(String name, int harvestLevel) {
            this.name = name;
            this.harvestLevel = harvestLevel;
        }

        @Nonnull
        public String getName() {
            return this.name;
        }

        public int getHarvestLevel(IBlockState state) {
            return this.harvestLevel;
        }

        public String getHarvestTool(IBlockState state) {
            return "wrench";
        }
    }
}
