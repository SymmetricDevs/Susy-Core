package supersymmetry.common.blocks;

import gregicality.multiblocks.common.block.blocks.BlockUniqueCasing;
import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantActiveBlock;
import gregtech.api.block.VariantBlock;
import gregtech.client.utils.BloomEffectUtil;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public class BlockMultiblockTank extends VariantActiveBlock<BlockMultiblockTank.MultiblockTankType> {

    public BlockMultiblockTank() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("multiblock_tank");
        setHardness(3.0f);
        setResistance(3.0f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 1);
        setDefaultState(getState(MultiblockTankType.CLARIFIER));
    }

    @Override
    public boolean canRenderInLayer(@Nonnull IBlockState state, @Nonnull BlockRenderLayer layer) {
        MultiblockTankType type = getState(state);

        if (layer == BlockRenderLayer.CUTOUT) return true;

        return false;
    }

    public static enum MultiblockTankType implements IStringSerializable, IStateHarvestLevel {
        CLARIFIER("clarifier", 1),
        FLOTATION("flotation", 1);

        private final String name;
        private final int harvestLevel;

        private MultiblockTankType(String name, int harvestLevel) {
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
